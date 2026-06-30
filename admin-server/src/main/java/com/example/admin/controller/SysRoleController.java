package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.SysRoleCreateDTO;
import com.example.admin.dto.SysRoleDTO;
import com.example.admin.dto.SysRoleUpdateDTO;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysUserRole;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysUserRoleMapper;
import com.example.admin.service.OperationLogService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SysRoleController {

    private final SysRoleMapper sysRoleMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final OperationLogService operationLogService;

    public SysRoleController(
        SysRoleMapper sysRoleMapper,
        SysUserRoleMapper sysUserRoleMapper,
        OperationLogService operationLogService
    ) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/roles")
    public Result<List<SysRoleDTO>> list() {
        return Result.success(selectRoles(false));
    }

    @GetMapping("/api/roles/options")
    public Result<List<SysRoleDTO>> options() {
        return Result.success(selectRoles(true));
    }

    @PostMapping("/api/roles")
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SysRoleCreateDTO createDTO) {
        Long codeCount = sysRoleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getRoleCode, createDTO.getRoleCode())
        );

        if (codeCount > 0) {
            return Result.fail(400, "角色编码已存在");
        }

        Long nameCount = sysRoleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getRoleName, createDTO.getRoleName())
        );

        if (nameCount > 0) {
            return Result.fail(400, "角色名称已存在");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(createDTO, role);
        sysRoleMapper.insert(role);

        operationLogService.record(authentication, "角色管理", "新增角色", role.getId(), "新增角色：" + role.getRoleName());

        return Result.success(role.getId());
    }

    @PutMapping("/api/roles/{id}")
    public Result<Long> update(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysRoleUpdateDTO updateDTO
    ) {
        SysRole existingRole = selectExistingRole(id);
        if (existingRole == null) {
            return Result.fail(404, "角色不存在");
        }

        Long nameCount = sysRoleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getRoleName, updateDTO.getRoleName())
                .ne(SysRole::getId, id)
        );

        if (nameCount > 0) {
            return Result.fail(400, "角色名称已存在");
        }

        existingRole.setRoleName(updateDTO.getRoleName());
        existingRole.setSort(updateDTO.getSort());
        existingRole.setStatus(updateDTO.getStatus());
        sysRoleMapper.updateById(existingRole);

        operationLogService.record(authentication, "角色管理", "编辑角色", id, "编辑角色：" + existingRole.getRoleName());

        return Result.success(id);
    }

    @DeleteMapping("/api/roles/{id}")
    @Transactional
    public Result<Long> delete(Authentication authentication, @PathVariable Long id) {
        SysRole existingRole = selectExistingRole(id);
        if (existingRole == null) {
            return Result.fail(404, "角色不存在");
        }

        if (id <= 4) {
            return Result.fail(400, "内置角色不能删除");
        }

        Long userCount = sysUserRoleMapper.selectCount(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id)
        );

        if (userCount > 0) {
            return Result.fail(400, "角色已分配给用户，不能删除");
        }

        existingRole.setDeleted(1);
        sysRoleMapper.updateById(existingRole);

        operationLogService.record(authentication, "角色管理", "删除角色", id, "删除角色：" + existingRole.getRoleName());

        return Result.success(id);
    }

    private List<SysRoleDTO> selectRoles(boolean onlyEnabled) {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getDeleted, 0)
                    .eq(onlyEnabled, SysRole::getStatus, 1)
                    .orderByAsc(SysRole::getSort)
                    .orderByAsc(SysRole::getId)
            )
            .stream()
            .map(role -> {
                SysRoleDTO dto = new SysRoleDTO();
                BeanUtils.copyProperties(role, dto);
                return dto;
            })
            .toList();
    }

    private SysRole selectExistingRole(Long id) {
        return sysRoleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .eq(SysRole::getId, id)
        );
    }
}
