package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.DashboardStatsDTO;
import com.example.admin.entity.SysDept;
import com.example.admin.entity.SysNotice;
import com.example.admin.entity.SysOperationLog;
import com.example.admin.entity.SysPost;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysUser;
import com.example.admin.mapper.SysDeptMapper;
import com.example.admin.mapper.SysNoticeMapper;
import com.example.admin.mapper.SysOperationLogMapper;
import com.example.admin.mapper.SysPostMapper;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
public class DashboardController {

    private final SysUserMapper sysUserMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysDeptMapper sysDeptMapper;

    private final SysPostMapper sysPostMapper;

    private final SysNoticeMapper sysNoticeMapper;

    private final SysOperationLogMapper sysOperationLogMapper;

    public DashboardController(
        SysUserMapper sysUserMapper,
        SysRoleMapper sysRoleMapper,
        SysDeptMapper sysDeptMapper,
        SysPostMapper sysPostMapper,
        SysNoticeMapper sysNoticeMapper,
        SysOperationLogMapper sysOperationLogMapper
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysPostMapper = sysPostMapper;
        this.sysNoticeMapper = sysNoticeMapper;
        this.sysOperationLogMapper = sysOperationLogMapper;
    }

    @GetMapping("/api/dashboard/stats")
    public Result<DashboardStatsDTO> stats() {
        Long totalUsers = sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0)
        );

        Long enabledUsers = sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getStatus, 1)
        );

        Long disabledUsers = sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getStatus, 0)
        );

        Long totalRoles = sysRoleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted, 0)
        );

        Long totalDepts = sysDeptMapper.selectCount(
            new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeleted, 0)
        );

        Long totalPosts = sysPostMapper.selectCount(
            new LambdaQueryWrapper<SysPost>().eq(SysPost::getDeleted, 0)
        );

        Long totalNotices = sysNoticeMapper.selectCount(
            new LambdaQueryWrapper<SysNotice>().eq(SysNotice::getDeleted, 0)
        );

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayOperationLogs = sysOperationLogMapper.selectCount(
            new LambdaQueryWrapper<SysOperationLog>()
                .ge(SysOperationLog::getCreateTime, todayStart)
        );

        SysOperationLog lastOperationLog = sysOperationLogMapper.selectOne(
            new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getCreateTime)
                .last("LIMIT 1")
        );

        return Result.success(new DashboardStatsDTO(
            totalUsers,
            enabledUsers,
            disabledUsers,
            totalRoles,
            totalDepts,
            totalPosts,
            totalNotices,
            todayOperationLogs,
            lastOperationLog == null ? null : lastOperationLog.getCreateTime()
        ));
    }
}
