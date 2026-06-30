package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.SysUserCreateDTO;
import com.example.admin.dto.SysUserListDTO;
import com.example.admin.dto.SysUserUpdateDTO;
import com.example.admin.entity.SysDept;
import com.example.admin.entity.SysPost;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysUser;
import com.example.admin.entity.SysUserRole;
import com.example.admin.mapper.SysDeptMapper;
import com.example.admin.mapper.SysPostMapper;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.mapper.SysUserRoleMapper;
import com.example.admin.service.OperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class SysUserController {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysPostMapper sysPostMapper;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public SysUserController(
        SysUserMapper sysUserMapper,
        SysRoleMapper sysRoleMapper,
        SysUserRoleMapper sysUserRoleMapper,
        SysDeptMapper sysDeptMapper,
        SysPostMapper sysPostMapper,
        PasswordEncoder passwordEncoder,
        OperationLogService operationLogService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysPostMapper = sysPostMapper;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/users")
    public Result<PageResult<SysUserListDTO>> list(
        @RequestParam(defaultValue = "1") Long pageNum,
        @RequestParam(defaultValue = "10") Long pageSize,
        @RequestParam(required = false) String keyword
    ) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeleted, 0)
            .and(StringUtils.hasText(keyword), wrapper -> wrapper
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getNickname, keyword)
                .or()
                .like(SysUser::getPhone, keyword)
            )
            .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = sysUserMapper.selectPage(page, queryWrapper);
        List<SysUser> users = userPage.getRecords();
        Map<Long, SysRole> roleMap = getRoleMap(users);
        Map<Long, SysDept> deptMap = getDeptMap(users);
        Map<Long, SysPost> postMap = getPostMap(users);

        List<SysUserListDTO> records = users.stream()
            .map(user -> {
                SysUserListDTO dto = new SysUserListDTO();
                BeanUtils.copyProperties(user, dto);

                SysRole role = roleMap.get(user.getId());
                if (role != null) {
                    dto.setRoleId(role.getId());
                    dto.setRoleName(role.getRoleName());
                }

                SysDept dept = deptMap.get(user.getDeptId());
                if (dept != null) {
                    dto.setDeptName(dept.getDeptName());
                }

                SysPost post = postMap.get(user.getPostId());
                if (post != null) {
                    dto.setPostName(post.getPostName());
                }

                return dto;
            })
            .toList();

        PageResult<SysUserListDTO> result = new PageResult<>(
            userPage.getTotal(),
            pageNum,
            pageSize,
            records
        );

        return Result.success(result);
    }

    @GetMapping("/api/users/export")
    public void export(
        @RequestParam(required = false) String keyword,
        HttpServletResponse response
    ) throws IOException {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeleted, 0)
            .and(StringUtils.hasText(keyword), wrapper -> wrapper
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getNickname, keyword)
                .or()
                .like(SysUser::getPhone, keyword)
            )
            .orderByDesc(SysUser::getCreateTime);

        List<SysUser> users = sysUserMapper.selectList(queryWrapper);
        Map<Long, SysRole> roleMap = getRoleMap(users);
        Map<Long, SysDept> deptMap = getDeptMap(users);
        Map<Long, SysPost> postMap = getPostMap(users);

        String fileName = URLEncoder.encode("用户列表.csv", StandardCharsets.UTF_8).replace("+", "%20");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        response.getWriter().write('\ufeff');
        response.getWriter().println("ID,用户名,姓名,部门,岗位,角色,手机号,邮箱,状态,创建时间");

        for (SysUser user : users) {
            SysRole role = roleMap.get(user.getId());
            SysDept dept = deptMap.get(user.getDeptId());
            SysPost post = postMap.get(user.getPostId());

            response.getWriter().println(String.join(",",
                csv(user.getId()),
                csv(user.getUsername()),
                csv(user.getNickname()),
                csv(dept == null ? "" : dept.getDeptName()),
                csv(post == null ? "" : post.getPostName()),
                csv(role == null ? "" : role.getRoleName()),
                csv(user.getPhone()),
                csv(user.getEmail()),
                csv(user.getStatus() != null && user.getStatus() == 1 ? "启用" : "禁用"),
                csv(user.getCreateTime())
            ));
        }
    }

    @PostMapping("/api/users")
    @Transactional
    public Result<Long> create(
        Authentication authentication,
        @Valid @RequestBody SysUserCreateDTO createDTO
    ) {
        if (existsByUsername(createDTO.getUsername())) {
            return Result.fail(400, "用户名已存在");
        }

        if (existsByPhone(createDTO.getPhone(), null)) {
            return Result.fail(400, "手机号已存在");
        }

        if (existsByEmail(createDTO.getEmail(), null)) {
            return Result.fail(400, "邮箱已存在");
        }

        Result<Void> checkResult = checkRoleDeptAndPost(createDTO.getRoleId(), createDTO.getDeptId(), createDTO.getPostId());
        if (checkResult.getCode() != 200) {
            return Result.fail(checkResult.getCode(), checkResult.getMessage());
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(createDTO, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sysUserMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(createDTO.getRoleId());
        sysUserRoleMapper.insert(userRole);

        operationLogService.record(authentication, "用户管理", "新增用户", user.getId(), "新增用户：" + user.getUsername());

        return Result.success(user.getId());
    }

    @PutMapping("/api/users/{id}")
    @Transactional
    public Result<Long> update(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysUserUpdateDTO updateDTO
    ) {
        SysUser existingUser = selectExistingUser(id);

        if (existingUser == null) {
            return Result.fail(404, "用户不存在");
        }

        if (existsByPhone(updateDTO.getPhone(), id)) {
            return Result.fail(400, "手机号已存在");
        }

        if (existsByEmail(updateDTO.getEmail(), id)) {
            return Result.fail(400, "邮箱已存在");
        }

        Result<Void> checkResult = checkRoleDeptAndPost(updateDTO.getRoleId(), updateDTO.getDeptId(), updateDTO.getPostId());
        if (checkResult.getCode() != 200) {
            return Result.fail(checkResult.getCode(), checkResult.getMessage());
        }

        existingUser.setNickname(updateDTO.getNickname());
        existingUser.setDeptId(updateDTO.getDeptId());
        existingUser.setPostId(updateDTO.getPostId());
        existingUser.setPhone(updateDTO.getPhone());
        existingUser.setEmail(updateDTO.getEmail());
        existingUser.setStatus(updateDTO.getStatus());
        sysUserMapper.updateById(existingUser);

        saveUserRole(id, updateDTO.getRoleId());

        operationLogService.record(authentication, "用户管理", "编辑用户", id, "编辑用户：" + existingUser.getUsername());

        return Result.success(id);
    }

    @PutMapping("/api/users/{id}/reset-password")
    @Transactional
    public Result<Long> resetPassword(Authentication authentication, @PathVariable Long id) {
        SysUser existingUser = selectExistingUser(id);

        if (existingUser == null) {
            return Result.fail(404, "用户不存在");
        }

        existingUser.setPassword(passwordEncoder.encode("123456"));
        sysUserMapper.updateById(existingUser);

        operationLogService.record(authentication, "用户管理", "重置密码", id, "重置用户密码：" + existingUser.getUsername());

        return Result.success(id);
    }

    @DeleteMapping("/api/users/{id}")
    @Transactional
    public Result<Long> delete(Authentication authentication, @PathVariable Long id) {
        if (id == 1L) {
            return Result.fail(400, "内置管理员不能删除");
        }

        SysUser existingUser = selectExistingUser(id);

        if (existingUser == null) {
            return Result.fail(404, "用户不存在");
        }

        sysUserMapper.deleteById(id);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));

        operationLogService.record(authentication, "用户管理", "删除用户", id, "删除用户：" + existingUser.getUsername());

        return Result.success(id);
    }

    private SysUser selectExistingUser(Long id) {
        return sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getId, id)
        );
    }

    private boolean existsByUsername(String username) {
        return sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getUsername, username)
        ) > 0;
    }

    private boolean existsByPhone(String phone, Long excludeId) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }

        return sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getPhone, phone)
                .ne(excludeId != null, SysUser::getId, excludeId)
        ) > 0;
    }

    private boolean existsByEmail(String email, Long excludeId) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        return sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getEmail, email)
                .ne(excludeId != null, SysUser::getId, excludeId)
        ) > 0;
    }

    private Result<Void> checkRoleDeptAndPost(Long roleId, Long deptId, Long postId) {
        SysRole role = sysRoleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getStatus, 1)
                .eq(SysRole::getId, roleId)
        );

        if (role == null) {
            return Result.fail(400, "角色不存在或已禁用");
        }

        SysDept dept = sysDeptMapper.selectOne(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeleted, 0)
                .eq(SysDept::getStatus, 1)
                .eq(SysDept::getId, deptId)
        );

        if (dept == null) {
            return Result.fail(400, "部门不存在或已禁用");
        }

        SysPost post = sysPostMapper.selectOne(
            new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getDeleted, 0)
                .eq(SysPost::getStatus, 1)
                .eq(SysPost::getId, postId)
        );

        if (post == null) {
            return Result.fail(400, "岗位不存在或已禁用");
        }

        return Result.success(null);
    }

    private void saveUserRole(Long userId, Long roleId) {
        SysUserRole existingUserRole = sysUserRoleMapper.selectOne(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );

        if (existingUserRole == null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            sysUserRoleMapper.insert(userRole);
            return;
        }

        existingUserRole.setRoleId(roleId);
        sysUserRoleMapper.updateById(existingUserRole);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }

    private Map<Long, SysDept> getDeptMap(List<SysUser> users) {
        List<Long> deptIds = users.stream()
            .map(SysUser::getDeptId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (deptIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return sysDeptMapper.selectList(new LambdaQueryWrapper<SysDept>().in(SysDept::getId, deptIds))
            .stream()
            .collect(Collectors.toMap(SysDept::getId, dept -> dept));
    }

    private Map<Long, SysPost> getPostMap(List<SysUser> users) {
        List<Long> postIds = users.stream()
            .map(SysUser::getPostId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return sysPostMapper.selectList(new LambdaQueryWrapper<SysPost>().in(SysPost::getId, postIds))
            .stream()
            .collect(Collectors.toMap(SysPost::getId, post -> post));
    }

    private Map<Long, SysRole> getRoleMap(List<SysUser> users) {
        if (users.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> userIds = users.stream()
            .map(SysUser::getId)
            .toList();

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds)
        );

        if (userRoles.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .distinct()
            .toList();

        Map<Long, SysRole> rolesById = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
            .stream()
            .collect(Collectors.toMap(SysRole::getId, role -> role));

        return userRoles.stream()
            .filter(userRole -> rolesById.containsKey(userRole.getRoleId()))
            .collect(Collectors.toMap(
                SysUserRole::getUserId,
                userRole -> rolesById.get(userRole.getRoleId()),
                (first, second) -> first
            ));
    }
}
