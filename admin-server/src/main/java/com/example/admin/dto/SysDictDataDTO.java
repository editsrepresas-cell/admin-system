package com.example.admin.dto;

import lombok.Data;

@Data
public class SysDictDataDTO {

    private Long id;

    private Long dictTypeId;

    private String dictLabel;

    private String dictValue;

    private Integer sort;

    private Integer status;

    private String remark;
}
