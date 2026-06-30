package com.example.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysOperationLogDTO {

    private Long id;

    private Long operatorId;

    private String operatorUsername;

    private String module;

    private String action;

    private Long targetId;

    private String detail;

    private Integer result;

    private LocalDateTime createTime;
}
