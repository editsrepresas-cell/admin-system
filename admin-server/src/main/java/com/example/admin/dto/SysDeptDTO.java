package com.example.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SysDeptDTO {

    private Long id;

    private Long parentId;

    private String deptName;

    private String leader;

    private String phone;

    private String email;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private List<SysDeptDTO> children = new ArrayList<>();
}
