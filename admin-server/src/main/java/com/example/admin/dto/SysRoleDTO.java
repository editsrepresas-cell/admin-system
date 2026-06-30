package com.example.admin.dto;

import lombok.Data;

@Data
public class SysRoleDTO {

    private Long id;

    private String roleCode;

    private String roleName;

    private Integer sort;

    private Integer status;
}
