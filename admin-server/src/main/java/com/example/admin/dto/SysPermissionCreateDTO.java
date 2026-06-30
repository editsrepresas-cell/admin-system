package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysPermissionCreateDTO {

    private Long parentId = 0L;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码不能超过100个字符")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称不能超过50个字符")
    private String permissionName;

    @NotBlank(message = "权限类型不能为空")
    @Pattern(regexp = "MENU|BUTTON", message = "权限类型只能是菜单或按钮")
    private String permissionType;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
