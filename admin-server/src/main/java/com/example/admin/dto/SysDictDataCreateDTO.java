package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SysDictDataCreateDTO {

    @NotNull(message = "字典类型不能为空")
    private Long dictTypeId;

    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "字典值只能使用字母、数字、下划线和短横线")
    private String dictValue;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
