package com.example.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysPostDTO {

    private Long id;

    private String postCode;

    private String postName;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;
}
