package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.RolePermissionUpdateDTO;
import com.example.admin.dto.SysPermissionCreateDTO;
import com.example.admin.dto.SysPermissionDTO;
import com.example.admin.dto.SysPermissionUpdateDTO;
import com.example.admin.entity.SysPermission;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysRolePermission;
import com.example.admin.mapper.SysPermissionMapper;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysRolePermissionMapper;
import com.example.admin.service.OperationLogService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class SysPermissionController {

    private final SysPermissionMapper sysPermissionMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysRolePermissionMapper sysRolePermissionMapper;

    private final OperationLogService operationLogService;

    public SysPermissionController(
        SysPermissionMapper sysPermissionMapper,
        SysRoleMapper sysRoleMapper,
        SysRolePermissionMapper sysRolePermissionMapper,
        OperationLogService operationLogService
    ) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/permissions/tree")
    public Result<List<SysPermissionDTO>> tree() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .orderByAsc(SysPermission::getSort)
                .orderByAsc(SysPermission::getId)
        );

        return Result.success(buildTree(permissions));
    }

    @PostMapping("/api/permissions")
    @Transactional
    public Result<Long> create(
        Authentication authentication,
        @Valid @RequestBody SysPermissionCreateDTO createDTO
    ) {
        Result<Void> checkResult = checkPermission(createDTO.getParentId(), createDTO.getPermissionCode(), null);
        if (checkResult.getCode() != 200) {
            return Result.fail(checkResult.getCode(), checkResult.getMessage());
        }

        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(createDTO, permission);
        permission.setParentId(normalizeParentId(permission.getParentId()));
        sysPermissionMapper.insert(permission);

        operationLogService.record(authentication, "权限管理", "新增权限", permission.getId(), "新增权限：" + permission.getPermissionName());
        return Result.success(permission.getId());
    }

    @PutMapping("/api/permissions/{id}")
    @Transactional
    public Result<Long> update(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysPermissionUpdateDTO updateDTO
    ) {
        SysPermission existingPermission = selectExistingPermission(id);
        if (existingPermission == null) {
            return Result.fail(404, "权限不存在");
        }

        Long parentId = normalizeParentId(updateDTO.getParentId());
        if (id.equals(parentId)) {
            return Result.fail(400, "上级权限不能选择自己");
        }

        Result<Void> checkResult = checkPermission(parentId, updateDTO.getPermissionCode(), id);
        if (checkResult.getCode() != 200) {
            return Result.fail(checkResult.getCode(), checkResult.getMessage());
        }

        if (isDescendant(parentId, id)) {
            return Result.fail(400, "上级权限不能选择自己的下级权限");
        }

        existingPermission.setParentId(parentId);
        existingPermission.setPermissionCode(updateDTO.getPermissionCode());
        existingPermission.setPermissionName(updateDTO.getPermissionName());
        existingPermission.setPermissionType(updateDTO.getPermissionType());
        existingPermission.setSort(updateDTO.getSort());
        existingPermission.setStatus(updateDTO.getStatus());
        sysPermissionMapper.updateById(existingPermission);

        operationLogService.record(authentication, "权限管理", "编辑权限", id, "编辑权限：" + existingPermission.getPermissionName());
        return Result.success(id);
    }

    @DeleteMapping("/api/permissions/{id}")
    @Transactional
    public Result<Long> delete(Authentication authentication, @PathVariable Long id) {
        SysPermission existingPermission = selectExistingPermission(id);
        if (existingPermission == null) {
            return Result.fail(404, "权限不存在");
        }

        Long childCount = sysPermissionMapper.selectCount(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .eq(SysPermission::getParentId, id)
        );
        if (childCount > 0) {
            return Result.fail(400, "存在下级权限，不能删除");
        }

        Long rolePermissionCount = sysRolePermissionMapper.selectCount(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id)
        );
        if (rolePermissionCount > 0) {
            return Result.fail(400, "权限已分配给角色，不能删除");
        }

        sysPermissionMapper.deleteById(id);
        operationLogService.record(authentication, "权限管理", "删除权限", id, "删除权限：" + existingPermission.getPermissionName());
        return Result.success(id);
    }

    @GetMapping("/api/roles/{roleId}/permissions")
    public Result<List<Long>> rolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                    .eq(SysRolePermission::getRoleId, roleId)
            )
            .stream()
            .map(SysRolePermission::getPermissionId)
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();

        if (permissionIds.isEmpty()) {
            return Result.success(List.of());
        }

        List<Long> validPermissionIds = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getDeleted, 0)
                    .eq(SysPermission::getStatus, 1)
                    .in(SysPermission::getId, permissionIds)
            )
            .stream()
            .map(SysPermission::getId)
            .toList();

        return Result.success(validPermissionIds);
    }

    @PutMapping("/api/roles/{roleId}/permissions")
    @Transactional
    public Result<Long> updateRolePermissions(
        Authentication authentication,
        @PathVariable Long roleId,
        @Valid @RequestBody RolePermissionUpdateDTO updateDTO
    ) {
        SysRole role = sysRoleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getId, roleId)
        );

        if (role == null) {
            return Result.fail(404, "角色不存在");
        }

        Set<Long> permissionIds = updateDTO.getPermissionIds()
            .stream()
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!permissionIds.isEmpty()) {
            Long validPermissionCount = sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getDeleted, 0)
                    .eq(SysPermission::getStatus, 1)
                    .in(SysPermission::getId, permissionIds)
            );

            if (validPermissionCount != permissionIds.size()) {
                return Result.fail(400, "权限包含不存在、已删除或已禁用的数据");
            }
        }

        sysRolePermissionMapper.delete(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId)
        );

        for (Long permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            sysRolePermissionMapper.insert(rolePermission);
        }

        operationLogService.record(
            authentication,
            "角色管理",
            "配置权限",
            roleId,
            "配置角色权限：" + role.getRoleName()
        );

        return Result.success(roleId);
    }

    private List<SysPermissionDTO> buildTree(List<SysPermission> permissions) {
        Map<Long, SysPermissionDTO> dtoMap = permissions.stream()
            .map(permission -> {
                SysPermissionDTO dto = new SysPermissionDTO();
                BeanUtils.copyProperties(permission, dto);
                return dto;
            })
            .collect(Collectors.toMap(SysPermissionDTO::getId, dto -> dto));

        List<SysPermissionDTO> roots = new ArrayList<>();

        for (SysPermissionDTO dto : dtoMap.values()) {
            if (dto.getParentId() == null || dto.getParentId() == 0) {
                roots.add(dto);
                continue;
            }

            SysPermissionDTO parent = dtoMap.get(dto.getParentId());
            if (parent == null) {
                roots.add(dto);
            } else {
                parent.getChildren().add(dto);
            }
        }

        dtoMap.values().forEach(dto ->
            dto.getChildren().sort((left, right) -> Integer.compare(left.getSort(), right.getSort()))
        );
        roots.sort((left, right) -> Integer.compare(left.getSort(), right.getSort()));
        return roots;
    }

    private SysPermission selectExistingPermission(Long id) {
        return sysPermissionMapper.selectOne(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .eq(SysPermission::getId, id)
        );
    }

    private Result<Void> checkPermission(Long parentId, String permissionCode, Long excludeId) {
        if (!StringUtils.hasText(permissionCode)) {
            return Result.fail(400, "权限编码不能为空");
        }

        Long normalizedParentId = normalizeParentId(parentId);
        if (normalizedParentId != 0 && selectExistingPermission(normalizedParentId) == null) {
            return Result.fail(400, "上级权限不存在");
        }

        Long codeCount = sysPermissionMapper.selectCount(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getDeleted, 0)
                .eq(SysPermission::getPermissionCode, permissionCode)
                .ne(excludeId != null, SysPermission::getId, excludeId)
        );
        if (codeCount > 0) {
            return Result.fail(400, "权限编码已存在");
        }

        return Result.success(null);
    }

    private boolean isDescendant(Long parentId, Long id) {
        Long currentParentId = normalizeParentId(parentId);
        while (currentParentId != 0) {
            if (currentParentId.equals(id)) {
                return true;
            }

            SysPermission parent = selectExistingPermission(currentParentId);
            if (parent == null) {
                return false;
            }
            currentParentId = normalizeParentId(parent.getParentId());
        }

        return false;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
