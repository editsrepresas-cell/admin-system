package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.SysDeptCreateDTO;
import com.example.admin.dto.SysDeptDTO;
import com.example.admin.dto.SysDeptUpdateDTO;
import com.example.admin.entity.SysDept;
import com.example.admin.entity.SysUser;
import com.example.admin.mapper.SysDeptMapper;
import com.example.admin.mapper.SysUserMapper;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SysDeptController {

    private final SysDeptMapper sysDeptMapper;

    private final SysUserMapper sysUserMapper;

    private final OperationLogService operationLogService;

    public SysDeptController(
        SysDeptMapper sysDeptMapper,
        SysUserMapper sysUserMapper,
        OperationLogService operationLogService
    ) {
        this.sysDeptMapper = sysDeptMapper;
        this.sysUserMapper = sysUserMapper;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/depts/tree")
    public Result<List<SysDeptDTO>> tree() {
        List<SysDept> depts = sysDeptMapper.selectList(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeleted, 0)
                .orderByAsc(SysDept::getSort)
                .orderByAsc(SysDept::getId)
        );

        return Result.success(buildTree(depts));
    }

    @PostMapping("/api/depts")
    @Transactional
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SysDeptCreateDTO createDTO) {
        Result<Long> parentCheck = validateParent(createDTO.getParentId(), null);
        if (parentCheck != null) {
            return parentCheck;
        }

        if (deptNameExists(createDTO.getParentId(), createDTO.getDeptName(), null)) {
            return Result.fail(400, "同级部门名称已存在");
        }

        SysDept dept = new SysDept();
        BeanUtils.copyProperties(createDTO, dept);
        sysDeptMapper.insert(dept);

        operationLogService.record(authentication, "部门管理", "新增部门", dept.getId(), "新增部门：" + dept.getDeptName());
        return Result.success(dept.getId());
    }

    @PutMapping("/api/depts/{id}")
    @Transactional
    public Result<Long> update(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysDeptUpdateDTO updateDTO
    ) {
        SysDept existingDept = selectExistingDept(id);
        if (existingDept == null) {
            return Result.fail(404, "部门不存在");
        }

        Result<Long> parentCheck = validateParent(updateDTO.getParentId(), id);
        if (parentCheck != null) {
            return parentCheck;
        }

        if (deptNameExists(updateDTO.getParentId(), updateDTO.getDeptName(), id)) {
            return Result.fail(400, "同级部门名称已存在");
        }

        existingDept.setParentId(updateDTO.getParentId());
        existingDept.setDeptName(updateDTO.getDeptName());
        existingDept.setLeader(updateDTO.getLeader());
        existingDept.setPhone(updateDTO.getPhone());
        existingDept.setEmail(updateDTO.getEmail());
        existingDept.setSort(updateDTO.getSort());
        existingDept.setStatus(updateDTO.getStatus());
        sysDeptMapper.updateById(existingDept);

        operationLogService.record(authentication, "部门管理", "编辑部门", id, "编辑部门：" + existingDept.getDeptName());
        return Result.success(id);
    }

    @DeleteMapping("/api/depts/{id}")
    @Transactional
    public Result<Long> delete(Authentication authentication, @PathVariable Long id) {
        SysDept existingDept = selectExistingDept(id);
        if (existingDept == null) {
            return Result.fail(404, "部门不存在");
        }

        Long childCount = sysDeptMapper.selectCount(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeleted, 0)
                .eq(SysDept::getParentId, id)
        );

        if (childCount > 0) {
            return Result.fail(400, "存在下级部门，不能删除");
        }

        Long userCount = sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getDeptId, id)
        );

        if (userCount > 0) {
            return Result.fail(400, "部门下存在用户，不能删除");
        }

        sysDeptMapper.deleteById(id);
        operationLogService.record(authentication, "部门管理", "删除部门", id, "删除部门：" + existingDept.getDeptName());
        return Result.success(id);
    }

    private SysDept selectExistingDept(Long id) {
        return sysDeptMapper.selectOne(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeleted, 0)
                .eq(SysDept::getId, id)
        );
    }

    private Result<Long> validateParent(Long parentId, Long currentId) {
        if (parentId == null || parentId == 0) {
            return null;
        }

        if (parentId.equals(currentId)) {
            return Result.fail(400, "上级部门不能选择自己");
        }

        SysDept parent = selectExistingDept(parentId);
        if (parent == null) {
            return Result.fail(400, "上级部门不存在");
        }

        Long cursor = parent.getParentId();
        while (cursor != null && cursor != 0) {
            if (cursor.equals(currentId)) {
                return Result.fail(400, "上级部门不能选择自己的下级部门");
            }
            SysDept ancestor = selectExistingDept(cursor);
            cursor = ancestor == null ? 0 : ancestor.getParentId();
        }

        return null;
    }

    private boolean deptNameExists(Long parentId, String deptName, Long excludeId) {
        return sysDeptMapper.selectCount(
            new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeleted, 0)
                .eq(SysDept::getParentId, parentId)
                .eq(SysDept::getDeptName, deptName)
                .ne(excludeId != null, SysDept::getId, excludeId)
        ) > 0;
    }

    private List<SysDeptDTO> buildTree(List<SysDept> depts) {
        Map<Long, SysDeptDTO> dtoMap = new LinkedHashMap<>();
        for (SysDept dept : depts) {
            SysDeptDTO dto = new SysDeptDTO();
            BeanUtils.copyProperties(dept, dto);
            dtoMap.put(dto.getId(), dto);
        }

        List<SysDeptDTO> roots = new ArrayList<>();
        for (SysDeptDTO dto : dtoMap.values()) {
            if (dto.getParentId() == null || dto.getParentId() == 0 || !dtoMap.containsKey(dto.getParentId())) {
                roots.add(dto);
            } else {
                dtoMap.get(dto.getParentId()).getChildren().add(dto);
            }
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<SysDeptDTO> depts) {
        depts.sort(Comparator.comparing(SysDeptDTO::getSort).thenComparing(SysDeptDTO::getId));
        for (SysDeptDTO dept : depts) {
            sortTree(dept.getChildren());
        }
    }
}
