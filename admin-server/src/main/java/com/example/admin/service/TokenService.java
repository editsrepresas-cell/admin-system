package com.example.admin.service;

import com.example.admin.entity.SysUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    private final ObjectMapper objectMapper;

    private final String secret;

    private final long expireSeconds;

    public TokenService(
        ObjectMapper objectMapper,
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expire-seconds}") long expireSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expireSeconds = expireSeconds;
    }

    public String generateToken(SysUser user, String roleCode) {
        return generateToken(user, roleCode, List.of());
    }

    public String generateToken(SysUser user, String roleCode, List<String> permissionCodes) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user.getId());
            payload.put("username", user.getUsername());
            payload.put("roleCode", roleCode);
            payload.put("permissions", permissionCodes);
            payload.put("exp", Instant.now().plusSeconds(expireSeconds).getEpochSecond());

            String headerText = base64Url(objectMapper.writeValueAsBytes(header));
            String payloadText = base64Url(objectMapper.writeValueAsBytes(payload));
            String content = headerText + "." + payloadText;
            String signature = sign(content);

            return content + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("生成 Token 失败", e);
        }
    }

    public Map<String, Object> parseToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                throw new IllegalArgumentException("Token 不能为空");
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Token 格式错误");
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = sign(content);
            if (!expectedSignature.equals(parts[2])) {
                throw new IllegalArgumentException("Token 签名无效");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(
                payloadBytes,
                new TypeReference<Map<String, Object>>() {}
            );

            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("Token 已过期");
            }

            return payload;
        } catch (Exception e) {
            throw new IllegalArgumentException("Token 无效", e);
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return base64Url(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
