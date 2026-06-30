package com.example.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysPermissionDTO {

    private Long id;

    private Long parentId;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private Integer sort;

    private Integer status;

    private List<SysPermissionDTO> children = new ArrayList<>();
}
