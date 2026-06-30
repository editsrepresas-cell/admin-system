package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.SysDictDataCreateDTO;
import com.example.admin.dto.SysDictDataDTO;
import com.example.admin.dto.SysDictDataUpdateDTO;
import com.example.admin.dto.SysDictTypeCreateDTO;
import com.example.admin.dto.SysDictTypeDTO;
import com.example.admin.dto.SysDictTypeUpdateDTO;
import com.example.admin.entity.SysDictData;
import com.example.admin.entity.SysDictType;
import com.example.admin.mapper.SysDictDataMapper;
import com.example.admin.mapper.SysDictTypeMapper;
import com.example.admin.service.OperationLogService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SysDictController {

    private final SysDictTypeMapper sysDictTypeMapper;

    private final SysDictDataMapper sysDictDataMapper;

    private final OperationLogService operationLogService;

    public SysDictController(
        SysDictTypeMapper sysDictTypeMapper,
        SysDictDataMapper sysDictDataMapper,
        OperationLogService operationLogService
    ) {
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.sysDictDataMapper = sysDictDataMapper;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/dict-types")
    public Result<List<SysDictTypeDTO>> listTypes(@RequestParam(required = false) String keyword) {
        List<SysDictTypeDTO> types = sysDictTypeMapper.selectList(
                new LambdaQueryWrapper<SysDictType>()
                    .eq(SysDictType::getDeleted, 0)
                    .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SysDictType::getDictCode, keyword)
                        .or()
                        .like(SysDictType::getDictName, keyword)
                    )
                    .orderByAsc(SysDictType::getSort)
                    .orderByAsc(SysDictType::getId)
            )
            .stream()
            .map(type -> {
                SysDictTypeDTO dto = new SysDictTypeDTO();
                BeanUtils.copyProperties(type, dto);
                return dto;
            })
            .toList();

        return Result.success(types);
    }

    @GetMapping("/api/dict-types/options")
    public Result<List<SysDictTypeDTO>> typeOptions() {
        List<SysDictTypeDTO> types = sysDictTypeMapper.selectList(
                new LambdaQueryWrapper<SysDictType>()
                    .eq(SysDictType::getDeleted, 0)
                    .eq(SysDictType::getStatus, 1)
                    .orderByAsc(SysDictType::getSort)
                    .orderByAsc(SysDictType::getId)
            )
            .stream()
            .map(type -> {
                SysDictTypeDTO dto = new SysDictTypeDTO();
                BeanUtils.copyProperties(type, dto);
                return dto;
            })
            .toList();

        return Result.success(types);
    }

    @PostMapping("/api/dict-types")
    @Transactional
    public Result<Long> createType(Authentication authentication, @Valid @RequestBody SysDictTypeCreateDTO createDTO) {
        if (dictCodeExists(createDTO.getDictCode(), null)) {
            return Result.fail(400, "字典编码已存在");
        }

        if (dictNameExists(createDTO.getDictName(), null)) {
            return Result.fail(400, "字典名称已存在");
        }

        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(createDTO, dictType);
        sysDictTypeMapper.insert(dictType);

        operationLogService.record(authentication, "字典管理", "新增字典类型", dictType.getId(), "新增字典类型：" + dictType.getDictName());
        return Result.success(dictType.getId());
    }

    @PutMapping("/api/dict-types/{id}")
    @Transactional
    public Result<Long> updateType(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysDictTypeUpdateDTO updateDTO
    ) {
        SysDictType existingType = selectExistingType(id);
        if (existingType == null) {
            return Result.fail(404, "字典类型不存在");
        }

        if (dictNameExists(updateDTO.getDictName(), id)) {
            return Result.fail(400, "字典名称已存在");
        }

        existingType.setDictName(updateDTO.getDictName());
        existingType.setSort(updateDTO.getSort());
        existingType.setStatus(updateDTO.getStatus());
        sysDictTypeMapper.updateById(existingType);

        operationLogService.record(authentication, "字典管理", "编辑字典类型", id, "编辑字典类型：" + existingType.getDictName());
        return Result.success(id);
    }

    @DeleteMapping("/api/dict-types/{id}")
    @Transactional
    public Result<Long> deleteType(Authentication authentication, @PathVariable Long id) {
        SysDictType existingType = selectExistingType(id);
        if (existingType == null) {
            return Result.fail(404, "字典类型不存在");
        }

        Long dataCount = sysDictDataMapper.selectCount(
            new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDeleted, 0)
                .eq(SysDictData::getDictTypeId, id)
        );

        if (dataCount > 0) {
            return Result.fail(400, "字典类型下存在字典数据，不能删除");
        }

        sysDictTypeMapper.deleteById(id);

        operationLogService.record(authentication, "字典管理", "删除字典类型", id, "删除字典类型：" + existingType.getDictName());
        return Result.success(id);
    }

    @GetMapping("/api/dict-data")
    public Result<List<SysDictDataDTO>> listData(
        @RequestParam Long dictTypeId,
        @RequestParam(required = false) String keyword
    ) {
        List<SysDictDataDTO> dataList = sysDictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDeleted, 0)
                    .eq(SysDictData::getDictTypeId, dictTypeId)
                    .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SysDictData::getDictLabel, keyword)
                        .or()
                        .like(SysDictData::getDictValue, keyword)
                    )
                    .orderByAsc(SysDictData::getSort)
                    .orderByAsc(SysDictData::getId)
            )
            .stream()
            .map(data -> {
                SysDictDataDTO dto = new SysDictDataDTO();
                BeanUtils.copyProperties(data, dto);
                return dto;
            })
            .toList();

        return Result.success(dataList);
    }

    @GetMapping("/api/dict-data/options")
    public Result<List<SysDictDataDTO>> dataOptions(@RequestParam String dictCode) {
        SysDictType dictType = sysDictTypeMapper.selectOne(
            new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDeleted, 0)
                .eq(SysDictType::getStatus, 1)
                .eq(SysDictType::getDictCode, dictCode)
        );

        if (dictType == null) {
            return Result.success(List.of());
        }

        List<SysDictDataDTO> dataList = sysDictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDeleted, 0)
                    .eq(SysDictData::getStatus, 1)
                    .eq(SysDictData::getDictTypeId, dictType.getId())
                    .orderByAsc(SysDictData::getSort)
                    .orderByAsc(SysDictData::getId)
            )
            .stream()
            .map(data -> {
                SysDictDataDTO dto = new SysDictDataDTO();
                BeanUtils.copyProperties(data, dto);
                return dto;
            })
            .toList();

        return Result.success(dataList);
    }

    @PostMapping("/api/dict-data")
    @Transactional
    public Result<Long> createData(Authentication authentication, @Valid @RequestBody SysDictDataCreateDTO createDTO) {
        SysDictType dictType = selectExistingType(createDTO.getDictTypeId());
        if (dictType == null) {
            return Result.fail(404, "字典类型不存在");
        }

        if (dictValueExists(createDTO.getDictTypeId(), createDTO.getDictValue(), null)) {
            return Result.fail(400, "字典值已存在");
        }

        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(createDTO, data);
        sysDictDataMapper.insert(data);

        operationLogService.record(authentication, "字典管理", "新增字典数据", data.getId(), "新增字典数据：" + data.getDictLabel());
        return Result.success(data.getId());
    }

    @PutMapping("/api/dict-data/{id}")
    @Transactional
    public Result<Long> updateData(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysDictDataUpdateDTO updateDTO
    ) {
        SysDictData existingData = selectExistingData(id);
        if (existingData == null) {
            return Result.fail(404, "字典数据不存在");
        }

        existingData.setDictLabel(updateDTO.getDictLabel());
        existingData.setSort(updateDTO.getSort());
        existingData.setStatus(updateDTO.getStatus());
        existingData.setRemark(updateDTO.getRemark());
        sysDictDataMapper.updateById(existingData);

        operationLogService.record(authentication, "字典管理", "编辑字典数据", id, "编辑字典数据：" + existingData.getDictLabel());
        return Result.success(id);
    }

    @DeleteMapping("/api/dict-data/{id}")
    @Transactional
    public Result<Long> deleteData(Authentication authentication, @PathVariable Long id) {
        SysDictData existingData = selectExistingData(id);
        if (existingData == null) {
            return Result.fail(404, "字典数据不存在");
        }

        sysDictDataMapper.deleteById(id);

        operationLogService.record(authentication, "字典管理", "删除字典数据", id, "删除字典数据：" + existingData.getDictLabel());
        return Result.success(id);
    }

    private SysDictType selectExistingType(Long id) {
        return sysDictTypeMapper.selectOne(
            new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDeleted, 0)
                .eq(SysDictType::getId, id)
        );
    }

    private SysDictData selectExistingData(Long id) {
        return sysDictDataMapper.selectOne(
            new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDeleted, 0)
                .eq(SysDictData::getId, id)
        );
    }

    private boolean dictCodeExists(String dictCode, Long excludeId) {
        return sysDictTypeMapper.selectCount(
            new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDeleted, 0)
                .eq(SysDictType::getDictCode, dictCode)
                .ne(excludeId != null, SysDictType::getId, excludeId)
        ) > 0;
    }

    private boolean dictNameExists(String dictName, Long excludeId) {
        return sysDictTypeMapper.selectCount(
            new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDeleted, 0)
                .eq(SysDictType::getDictName, dictName)
                .ne(excludeId != null, SysDictType::getId, excludeId)
        ) > 0;
    }

    private boolean dictValueExists(Long dictTypeId, String dictValue, Long excludeId) {
        return sysDictDataMapper.selectCount(
            new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDeleted, 0)
                .eq(SysDictData::getDictTypeId, dictTypeId)
                .eq(SysDictData::getDictValue, dictValue)
                .ne(excludeId != null, SysDictData::getId, excludeId)
        ) > 0;
    }
}
