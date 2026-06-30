package com.example.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthLoginVO {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private Long roleId;
    
    private String roleCode;

    private String roleName;

    private List<String> permissionCodes;
}
