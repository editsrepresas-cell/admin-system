package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SysDictTypeCreateDTO {

    @NotBlank(message = "字典编码不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,49}$", message = "字典编码只能使用小写字母、数字和下划线，且必须以字母开头")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
