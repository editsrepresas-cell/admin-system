package com.example.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserListDTO {

    private Long id;

    private String username;

    private String nickname;

    private Long deptId;

    private String deptName;

    private Long postId;

    private String postName;

    private Long roleId;

    private String roleName;

    private String phone;

    private String email;

    private Integer status;

    private LocalDateTime createTime;
}
