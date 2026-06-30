package com.example.admin.config;

import com.example.admin.common.Result;
import com.example.admin.security.TokenAuthenticationFilter;
import com.example.admin.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
        @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();

        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        TokenService tokenService,
        ObjectMapper objectMapper
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/auth/password").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/export")
                    .hasAnyAuthority("user:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/users")
                    .hasAnyAuthority("user:create", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/*/reset-password")
                    .hasAnyAuthority("user:reset-password", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**")
                    .hasAnyAuthority("user:update", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                    .hasAnyAuthority("user:delete", "ROLE_SUPER_ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/depts/tree")
                    .hasAnyAuthority("dept:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/depts")
                    .hasAnyAuthority("dept:create", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/depts/**")
                    .hasAnyAuthority("dept:update", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/depts/**")
                    .hasAnyAuthority("dept:delete", "ROLE_SUPER_ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/posts/options").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/posts")
                    .hasAnyAuthority("post:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/posts")
                    .hasAnyAuthority("post:create", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/posts/**")
                    .hasAnyAuthority("post:update", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/posts/**")
                    .hasAnyAuthority("post:delete", "ROLE_SUPER_ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/roles/options").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/roles")
                    .hasAnyAuthority("role:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/permissions/tree", "/api/roles/*/permissions")
                    .hasAnyAuthority("permission:list", "role:permission", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/permissions")
                    .hasAnyAuthority("permission:create", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/permissions/**")
                    .hasAnyAuthority("permission:update", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/permissions/**")
                    .hasAnyAuthority("permission:delete", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/roles")
                    .hasAnyAuthority("role:create", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/roles/*/permissions")
                    .hasAnyAuthority("role:permission", "ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/roles/**")
                    .hasAnyAuthority("role:update", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/roles/**")
                    .hasAnyAuthority("role:delete", "ROLE_SUPER_ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/operation-logs", "/api/operation-logs/modules", "/api/operation-logs/export")
                    .hasAnyAuthority("operation-log:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/dict-types/options", "/api/dict-data/options").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/dict-types", "/api/dict-data")
                    .hasAnyAuthority("dict:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/dict-types", "/api/dict-data")
                    .hasAnyAuthority("dict:create", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/dict-types/**", "/api/dict-data/**")
                    .hasAnyAuthority("dict:update", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/dict-types/**", "/api/dict-data/**")
                    .hasAnyAuthority("dict:delete", "ROLE_SUPER_ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/notices/unread")
                    .authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/notices/*/read", "/api/notices/read-all")
                    .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/notices", "/api/notices/*")
                    .hasAnyAuthority("notice:list", "ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/notices")
                    .hasAnyAuthority("notice:create", "ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/notices/*/publish", "/api/notices/*/offline")
                    .hasAnyAuthority("notice:publish", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/notices/**")
                    .hasAnyAuthority("notice:update", "ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/notices/**")
                    .hasAnyAuthority("notice:delete", "ROLE_SUPER_ADMIN", "ROLE_ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(response.getWriter(), Result.fail(401, "请先登录"));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    objectMapper.writeValue(response.getWriter(), Result.fail(403, "没有访问权限"));
                })
            )
            .addFilterBefore(
                new TokenAuthenticationFilter(tokenService),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
