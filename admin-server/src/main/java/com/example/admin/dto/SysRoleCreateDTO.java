package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SysRoleCreateDTO {

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z_]{2,30}$", message = "角色编码只能使用大写字母和下划线")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
