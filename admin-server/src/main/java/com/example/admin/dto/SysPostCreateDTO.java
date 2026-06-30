package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SysPostCreateDTO {

    @NotBlank(message = "岗位编码不能为空")
    @Pattern(regexp = "^[A-Z_]{2,30}$", message = "岗位编码只能使用大写字母和下划线")
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    private String postName;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
