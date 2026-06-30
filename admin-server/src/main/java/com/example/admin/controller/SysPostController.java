package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.Result;
import com.example.admin.dto.SysPostCreateDTO;
import com.example.admin.dto.SysPostDTO;
import com.example.admin.dto.SysPostUpdateDTO;
import com.example.admin.entity.SysPost;
import com.example.admin.entity.SysUser;
import com.example.admin.mapper.SysPostMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.service.OperationLogService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SysPostController {

    private final SysPostMapper sysPostMapper;

    private final SysUserMapper sysUserMapper;

    private final OperationLogService operationLogService;

    public SysPostController(
        SysPostMapper sysPostMapper,
        SysUserMapper sysUserMapper,
        OperationLogService operationLogService
    ) {
        this.sysPostMapper = sysPostMapper;
        this.sysUserMapper = sysUserMapper;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/api/posts")
    public Result<List<SysPostDTO>> list() {
        List<SysPostDTO> posts = sysPostMapper.selectList(
                new LambdaQueryWrapper<SysPost>()
                    .eq(SysPost::getDeleted, 0)
                    .orderByAsc(SysPost::getSort)
                    .orderByAsc(SysPost::getId)
            )
            .stream()
            .map(post -> {
                SysPostDTO dto = new SysPostDTO();
                BeanUtils.copyProperties(post, dto);
                return dto;
            })
            .toList();

        return Result.success(posts);
    }

    @GetMapping("/api/posts/options")
    public Result<List<SysPostDTO>> options() {
        List<SysPostDTO> posts = sysPostMapper.selectList(
                new LambdaQueryWrapper<SysPost>()
                    .eq(SysPost::getDeleted, 0)
                    .eq(SysPost::getStatus, 1)
                    .orderByAsc(SysPost::getSort)
                    .orderByAsc(SysPost::getId)
            )
            .stream()
            .map(post -> {
                SysPostDTO dto = new SysPostDTO();
                BeanUtils.copyProperties(post, dto);
                return dto;
            })
            .toList();

        return Result.success(posts);
    }

    @PostMapping("/api/posts")
    @Transactional
    public Result<Long> create(Authentication authentication, @Valid @RequestBody SysPostCreateDTO createDTO) {
        if (postCodeExists(createDTO.getPostCode(), null)) {
            return Result.fail(400, "岗位编码已存在");
        }

        if (postNameExists(createDTO.getPostName(), null)) {
            return Result.fail(400, "岗位名称已存在");
        }

        SysPost post = new SysPost();
        BeanUtils.copyProperties(createDTO, post);
        sysPostMapper.insert(post);

        operationLogService.record(authentication, "岗位管理", "新增岗位", post.getId(), "新增岗位：" + post.getPostName());
        return Result.success(post.getId());
    }

    @PutMapping("/api/posts/{id}")
    @Transactional
    public Result<Long> update(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody SysPostUpdateDTO updateDTO
    ) {
        SysPost existingPost = selectExistingPost(id);
        if (existingPost == null) {
            return Result.fail(404, "岗位不存在");
        }

        if (postNameExists(updateDTO.getPostName(), id)) {
            return Result.fail(400, "岗位名称已存在");
        }

        existingPost.setPostName(updateDTO.getPostName());
        existingPost.setSort(updateDTO.getSort());
        existingPost.setStatus(updateDTO.getStatus());
        sysPostMapper.updateById(existingPost);

        operationLogService.record(authentication, "岗位管理", "编辑岗位", id, "编辑岗位：" + existingPost.getPostName());
        return Result.success(id);
    }

    @DeleteMapping("/api/posts/{id}")
    @Transactional
    public Result<Long> delete(Authentication authentication, @PathVariable Long id) {
        SysPost existingPost = selectExistingPost(id);
        if (existingPost == null) {
            return Result.fail(404, "岗位不存在");
        }

        Long userCount = sysUserMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .eq(SysUser::getPostId, id)
        );

        if (userCount > 0) {
            return Result.fail(400, "岗位下存在用户，不能删除");
        }

        sysPostMapper.deleteById(id);
        operationLogService.record(authentication, "岗位管理", "删除岗位", id, "删除岗位：" + existingPost.getPostName());
        return Result.success(id);
    }

    private SysPost selectExistingPost(Long id) {
        return sysPostMapper.selectOne(
            new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getDeleted, 0)
                .eq(SysPost::getId, id)
        );
    }

    private boolean postCodeExists(String postCode, Long excludeId) {
        return sysPostMapper.selectCount(
            new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getDeleted, 0)
                .eq(SysPost::getPostCode, postCode)
                .ne(excludeId != null, SysPost::getId, excludeId)
        ) > 0;
    }

    private boolean postNameExists(String postName, Long excludeId) {
        return sysPostMapper.selectCount(
            new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getDeleted, 0)
                .eq(SysPost::getPostName, postName)
                .ne(excludeId != null, SysPost::getId, excludeId)
        ) > 0;
    }
}
