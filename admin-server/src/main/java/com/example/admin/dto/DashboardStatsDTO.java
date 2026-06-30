package com.example.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DashboardStatsDTO {

    private Long totalUsers;

    private Long enabledUsers;

    private Long disabledUsers;

    private Long totalRoles;

    private Long totalDepts;

    private Long totalPosts;

    private Long totalNotices;

    private Long todayOperationLogs;

    private LocalDateTime lastOperationTime;
}
