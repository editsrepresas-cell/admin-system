package com.example.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionUpdateDTO {

    @NotNull(message = "权限不能为空")
    private List<Long> permissionIds;
}
