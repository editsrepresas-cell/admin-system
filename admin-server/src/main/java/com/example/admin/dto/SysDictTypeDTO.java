package com.example.admin.dto;

import lombok.Data;

@Data
public class SysDictTypeDTO {

    private Long id;

    private String dictCode;

    private String dictName;

    private Integer sort;

    private Integer status;
}
