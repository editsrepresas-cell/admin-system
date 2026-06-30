package com.example.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SysUserUpdateDTO {

    @NotBlank(message = "姓名不能为空")
    private String nickname;

    @NotNull(message = "部门不能为空")
    private Long deptId;

    @NotNull(message = "岗位不能为空")
    private Long postId;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色不能为空")
    private Long roleId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
