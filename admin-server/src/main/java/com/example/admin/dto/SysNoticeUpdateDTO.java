package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysNoticeUpdateDTO {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题不能超过100个字符")
    private String title;

    @NotBlank(message = "公告类型不能为空")
    @Size(max = 50, message = "公告类型不能超过50个字符")
    private String noticeType;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 2000, message = "公告内容不能超过2000个字符")
    private String content;

    @NotNull(message = "公告状态不能为空")
    private Integer status;
}
