package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.SysNoticeCreateDTO;
import com.example.admin.dto.SysNoticeDTO;
import com.example.admin.dto.SysNoticeUpdateDTO;
import com.example.admin.entity.SysDictData;
import com.example.admin.entity.SysDictType;
import com.example.admin.entity.SysNotice;
import com.example.admin.entity.SysNoticeRead;
import com.example.admin.mapper.SysDictDataMapper;
import com.example.admin.mapper.SysDictTypeMapper;
import com.example.admin.mapper.SysNoticeMapper;
import com.example.admin.mapper.SysNoticeReadMapper;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class SysNoticeController {

    private final SysNoticeMapper sysNoticeMapper;

    private final SysNoticeReadMapper sysNoticeReadMapper;

    private final SysDictTypeMapper sysDictTypeMapper;

    private final SysDictDataMapper sysDictDataMapper;

    private final OperationLogService operationLogService;

    public SysNoticeController(
        SysNoticeMapper sysNoticeMapper,
        SysNoticeReadMapper sysNoticeReadMapper,
        SysDictTypeMapper sysDictTypeMapper,
        SysDictDataMapper sysDictDataMapper,
        OperationLogService operationLogService
    ) {
        this.sysNoticeMapper = sysNoticeMapper;
        this.sysNoticeReadMapper = sysNoticeReadMapper;
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.sysDictDataMapper = sysDictDataMapper;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/notices")
    public Result<List<SysNoticeDTO>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String noticeType,
        @RequestParam(required = false) Integer status
    ) {
        Map<String, String> typeNameMap = loadDictNameMap("notice_type");

        List<SysNoticeDTO> notices = sysNoticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>()
                    .eq(SysNotice::getDeleted, 0)
                    .eq(StringUtils.hasText(noticeType), SysNotice::getNoticeType, noticeType)
                    .eq(status != null, SysNotice::getStatus, status)
                    .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SysNotice::getTitle, keyword)
                        .or()
                        .like(SysNotice::getContent, keyword)
                    )
                    .orderByDesc(SysNotice::getId)
            )
            .stream()
            .map(notice -> toDTO(notice, typeNameMap))
            .toList();

        return Result.success(notices);
    }

    @GetMapping("/api/notices/unread")
    public Result<List<SysNoticeDTO>> unread(Authentication authentication) {
        Long userId = currentUserId(authentication);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        Set<Long> readNoticeIds = new HashSet<>(
            sysNoticeReadMapper.selectList(
                    new LambdaQueryWrapper<SysNoticeRead>()
                        .eq(SysNoticeRead::getUserId, userId)
                )
                .stream()
                .map(SysNoticeRead::getNoticeId)
                .toList()
        );
        Map<String, String> typeNameMap = loadDictNameMap("notice_type");

        List<SysNoticeDTO> notices = sysNoticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>()
                    .eq(SysNotice::getDeleted, 0)
                    .eq(SysNotice::getStatus, 1)
                    .orderByDesc(SysNotice::getPublishTime)
                    .orderByDesc(SysNotice::getId)
            )
            .stream()
            .filter(notice -> !readNoticeIds.contains(notice.getId()))
            .map(notice -> toDTO(notice, typeNameMap))
            .toList();

        return Result.success(notices);
    }

    @PutMapping("/api/notices/{id}/read")
    @Transactional
    public Result<Long> markRead(Authentication authentication, @PathVariable Long id) {
        Long userId = currentUserId(authentication);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        SysNotice existingNotice = selectExistingNotice(id);
        if (existingNotice == null || existingNotice.getStatus() != 1) {
            return Result.fail(404, "公告不存在");
        }

        saveReadRecord(userId, id);
        return Result.success(id);
    }

    @PutMapping("/api/notices/read-all")
    @Transactional
    public Result<Integer> markAllRead(Authentication authentication) {
        Long userId = currentUserId(authentication);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        List<SysNotice> notices = sysNoticeMapper.selectList(
            new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getDeleted, 0)
                .eq(SysNotice::getStatus, 1)
        );

        notices.forEach(notice -> saveReadRecord(userId, notice.getId()));
        return Result.success(notices.size());
    }

    @GetMapping("/api/notices/{id}")
    public Result<SysNoticeDTO> detail(@PathVariable Long id) {
        SysNotice existingNotice = selectExistingNotice(id);
        if (existingNotice == null) {
            return Result.fail(404, "公告不存在");
        }

        Map<String, String> typeNameMap = loadDictNameMap("notice_type");
        return Result.success(toDTO(existingNotice, typeNameMap));
    }

    @PostMapping("/api/notices")
    @Transactional
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SysNoticeCreateDTO createDTO) {
        SysNotice notice = new SysNotice();
        BeanUtils.copyProperties(createDTO, notice);
        notice.setCreateBy(currentUserId(authentication));
        if (notice.getStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        sysNoticeMapper.insert(notice);

        operationLogService.record(authentication, "通知公告", "新增公告", notice.getId(), "新增公告：" + notice.getTitle());
        return Result.success(notice.getId());
    }

    @PutMapping("/api/notices/{id}")
    @Transactional
    public Result<Long> update(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysNoticeUpdateDTO updateDTO
    ) {
        SysNotice existingNotice = selectExistingNotice(id);
        if (existingNotice == null) {
            return Result.fail(404, "公告不存在");
        }

        boolean publishNow = existingNotice.getStatus() != 1 && updateDTO.getStatus() == 1;
        existingNotice.setTitle(updateDTO.getTitle());
        existingNotice.setNoticeType(updateDTO.getNoticeType());
        existingNotice.setContent(updateDTO.getContent());
        existingNotice.setStatus(updateDTO.getStatus());
        if (publishNow) {
            existingNotice.setPublishTime(LocalDateTime.now());
        }
        sysNoticeMapper.updateById(existingNotice);

        operationLogService.record(authentication, "通知公告", "编辑公告", id, "编辑公告：" + existingNotice.getTitle());
        return Result.success(id);
    }

    @PutMapping("/api/notices/{id}/publish")
    @Transactional
    public Result<Long> publish(Authentication authentication, @PathVariable Long id) {
        SysNotice existingNotice = selectExistingNotice(id);
        if (existingNotice == null) {
            return Result.fail(404, "公告不存在");
        }

        existingNotice.setStatus(1);
        existingNotice.setPublishTime(LocalDateTime.now());
        sysNoticeMapper.updateById(existingNotice);

        operationLogService.record(authentication, "通知公告", "发布公告", id, "发布公告：" + existingNotice.getTitle());
        return Result.success(id);
    }

    @PutMapping("/api/notices/{id}/offline")
    @Transactional
    public Result<Long> offline(Authentication authentication, @PathVariable Long id) {
        SysNotice existingNotice = selectExistingNotice(id);
        if (existingNotice == null) {
            return Result.fail(404, "公告不存在");
        }

        existingNotice.setStatus(0);
        sysNoticeMapper.updateById(existingNotice);

        operationLogService.record(authentication, "通知公告", "下线公告", id, "下线公告：" + existingNotice.getTitle());
        return Result.success(id);
    }

    @DeleteMapping("/api/notices/{id}")
    @Transactional
    public Result<Long> delete(Authentication authentication, @PathVariable Long id) {
        SysNotice existingNotice = selectExistingNotice(id);
        if (existingNotice == null) {
            return Result.fail(404, "公告不存在");
        }

        sysNoticeMapper.deleteById(id);
        operationLogService.record(authentication, "通知公告", "删除公告", id, "删除公告：" + existingNotice.getTitle());
        return Result.success(id);
    }

    private SysNotice selectExistingNotice(Long id) {
        return sysNoticeMapper.selectOne(
            new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getDeleted, 0)
                .eq(SysNotice::getId, id)
        );
    }

    private void saveReadRecord(Long userId, Long noticeId) {
        Long count = sysNoticeReadMapper.selectCount(
            new LambdaQueryWrapper<SysNoticeRead>()
                .eq(SysNoticeRead::getUserId, userId)
                .eq(SysNoticeRead::getNoticeId, noticeId)
        );

        if (count > 0) {
            return;
        }

        SysNoticeRead read = new SysNoticeRead();
        read.setUserId(userId);
        read.setNoticeId(noticeId);
        read.setReadTime(LocalDateTime.now());
        sysNoticeReadMapper.insert(read);
    }

    private SysNoticeDTO toDTO(SysNotice notice, Map<String, String> typeNameMap) {
        SysNoticeDTO dto = new SysNoticeDTO();
        BeanUtils.copyProperties(notice, dto);
        dto.setNoticeTypeName(typeNameMap.getOrDefault(notice.getNoticeType(), notice.getNoticeType()));
        dto.setStatusName(notice.getStatus() == 1 ? "已发布" : "草稿");
        return dto;
    }

    private Map<String, String> loadDictNameMap(String dictCode) {
        SysDictType dictType = sysDictTypeMapper.selectOne(
            new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDeleted, 0)
                .eq(SysDictType::getStatus, 1)
                .eq(SysDictType::getDictCode, dictCode)
        );

        if (dictType == null) {
            return Map.of();
        }

        Map<String, String> map = new HashMap<>();
        sysDictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDeleted, 0)
                    .eq(SysDictData::getStatus, 1)
                    .eq(SysDictData::getDictTypeId, dictType.getId())
                    .orderByAsc(SysDictData::getSort)
            )
            .forEach(data -> map.put(data.getDictValue(), data.getDictLabel()));
        return map;
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication != null && authentication.getDetails() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
