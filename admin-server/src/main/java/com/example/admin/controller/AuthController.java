package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.AuthChangePasswordDTO;
import com.example.admin.dto.AuthLoginDTO;
import com.example.admin.dto.AuthLoginVO;
import com.example.admin.entity.SysPermission;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysRolePermission;
import com.example.admin.entity.SysUser;
import com.example.admin.entity.SysUserRole;
import com.example.admin.mapper.SysPermissionMapper;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysRolePermissionMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.mapper.SysUserRoleMapper;
import com.example.admin.service.OperationLogService;
import com.example.admin.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuthController {

    private final SysUserMapper sysUserMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysPermissionMapper sysPermissionMapper;

    private final SysRolePermissionMapper sysRolePermissionMapper;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final OperationLogService operationLogService;

    public AuthController(
        SysUserMapper sysUserMapper,
        SysUserRoleMapper sysUserRoleMapper,
        SysRoleMapper sysRoleMapper,
        SysPermissionMapper sysPermissionMapper,
        SysRolePermissionMapper sysRolePermissionMapper,
        PasswordEncoder passwordEncoder,
        TokenService tokenService,
        OperationLogService operationLogService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.operationLogService = operationLogService;
    }

    @PostMapping("/api/auth/login")
    public Result<AuthLoginVO> login(@Valid @RequestBody AuthLoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getUsername, loginDTO.getUsername())
        );

        if (user == null) {
            operationLogService.recordFailure(null, loginDTO.getUsername(), "认证中心", "登录系统", null, "登录失败：用户不存在");
            return Result.fail(400, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            operationLogService.recordFailure(user.getId(), user.getUsername(), "认证中心", "登录系统", user.getId(), "登录失败：密码错误");
            return Result.fail(400, "用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            operationLogService.recordFailure(user.getId(), user.getUsername(), "认证中心", "登录系统", user.getId(), "登录失败：用户已禁用");
            return Result.fail(400, "用户已被禁用");
        }

        AuthLoginVO vo = buildLoginVO(user);
        operationLogService.record(user.getId(), user.getUsername(), "认证中心", "登录系统", user.getId(), "登录成功");

        return Result.success(vo);
    }

    @GetMapping("/api/auth/me")
    public Result<AuthLoginVO> me(Authentication authentication) {
        if (authentication == null) {
            return Result.fail(401, "请先登录");
        }

        SysUser user = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getUsername, authentication.getName())
        );

        if (user == null) {
            return Result.fail(401, "请先登录");
        }

        return Result.success(buildLoginVO(user));
    }

    @PutMapping("/api/auth/password")
    public Result<Long> changePassword(
        Authentication authentication,
        @Valid @RequestBody AuthChangePasswordDTO passwordDTO
    ) {
        if (authentication == null) {
            operationLogService.recordFailure(null, "SYSTEM", "系统设置", "修改密码", null, "修改密码失败：未登录");
            return Result.fail(401, "请先登录");
        }

        SysUser user = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getUsername, authentication.getName())
        );

        if (user == null) {
            operationLogService.recordFailure(null, authentication.getName(), "系统设置", "修改密码", null, "修改密码失败：用户不存在");
            return Result.fail(401, "请先登录");
        }

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            operationLogService.recordFailure(user.getId(), user.getUsername(), "系统设置", "修改密码", user.getId(), "修改密码失败：原密码不正确");
            return Result.fail(400, "原密码不正确");
        }

        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            operationLogService.recordFailure(user.getId(), user.getUsername(), "系统设置", "修改密码", user.getId(), "修改密码失败：新密码与原密码相同");
            return Result.fail(400, "新密码不能和原密码相同");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        sysUserMapper.updateById(user);

        operationLogService.record(authentication, "系统设置", "修改密码", user.getId(), "修改当前账号密码");

        return Result.success(user.getId());
    }

    private AuthLoginVO buildLoginVO(SysUser user) {
        AuthLoginVO vo = new AuthLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());

        String roleCode = "USER";
        List<String> permissionCodes = List.of();

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );

        SysUserRole userRole = userRoles.isEmpty() ? null : userRoles.get(0);

        if (userRole != null) {
            SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                roleCode = role.getRoleCode();
                vo.setRoleId(role.getId());
                vo.setRoleCode(role.getRoleCode());
                vo.setRoleName(role.getRoleName());
                permissionCodes = getPermissionCodes(role.getId());
            }
        }

        vo.setPermissionCodes(permissionCodes);
        vo.setToken(tokenService.generateToken(user, roleCode, permissionCodes));
        return vo;
    }

    private List<String> getPermissionCodes(Long roleId) {
        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );

        if (rolePermissions.isEmpty()) {
            return List.of();
        }

        List<Long> permissionIds = rolePermissions.stream()
            .map(SysRolePermission::getPermissionId)
            .toList();

        return sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getDeleted, 0)
                    .eq(SysPermission::getStatus, 1)
                    .in(SysPermission::getId, permissionIds)
            )
            .stream()
            .map(SysPermission::getPermissionCode)
            .toList();
    }
}
