package com.example.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysNoticeDTO {

    private Long id;

    private String title;

    private String noticeType;

    private String noticeTypeName;

    private String content;

    private Integer status;

    private String statusName;

    private Long createBy;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;
}
