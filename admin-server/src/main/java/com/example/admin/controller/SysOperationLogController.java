package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.SysOperationLogDTO;
import com.example.admin.entity.SysOperationLog;
import com.example.admin.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class SysOperationLogController {

    private final SysOperationLogMapper sysOperationLogMapper;

    public SysOperationLogController(SysOperationLogMapper sysOperationLogMapper) {
        this.sysOperationLogMapper = sysOperationLogMapper;
    }

    @GetMapping("/api/operation-logs/modules")
    public Result<List<String>> modules() {
        List<String> modules = sysOperationLogMapper.selectList(
                new LambdaQueryWrapper<SysOperationLog>()
                    .select(SysOperationLog::getModule)
                    .isNotNull(SysOperationLog::getModule)
                    .groupBy(SysOperationLog::getModule)
                    .orderByAsc(SysOperationLog::getModule)
            )
            .stream()
            .map(SysOperationLog::getModule)
            .filter(StringUtils::hasText)
            .toList();

        return Result.success(modules);
    }

    @GetMapping("/api/operation-logs")
    public Result<PageResult<SysOperationLogDTO>> list(
        @RequestParam(defaultValue = "1") Long pageNum,
        @RequestParam(defaultValue = "10") Long pageSize,
        @RequestParam(required = false) String module,
        @RequestParam(required = false) Integer result,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @RequestParam(required = false) String keyword
    ) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<SysOperationLog> queryWrapper = buildQueryWrapper(module, result, startTime, endTime, keyword);

        Page<SysOperationLog> logPage = sysOperationLogMapper.selectPage(page, queryWrapper);

        List<SysOperationLogDTO> records = logPage.getRecords().stream()
            .map(log -> {
                SysOperationLogDTO dto = new SysOperationLogDTO();
                BeanUtils.copyProperties(log, dto);
                return dto;
            })
            .toList();

        return Result.success(new PageResult<>(
            logPage.getTotal(),
            pageNum,
            pageSize,
            records
        ));
    }

    @GetMapping("/api/operation-logs/export")
    public void export(
        @RequestParam(required = false) String module,
        @RequestParam(required = false) Integer result,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @RequestParam(required = false) String keyword,
        HttpServletResponse response
    ) throws IOException {
        List<SysOperationLog> logs = sysOperationLogMapper.selectList(
            buildQueryWrapper(module, result, startTime, endTime, keyword)
        );

        String filename = "operation-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("ID,操作人ID,操作人,模块,操作,目标ID,结果,详情,操作时间\n");

        for (SysOperationLog log : logs) {
            csv.append(csv(log.getId())).append(',')
                .append(csv(log.getOperatorId())).append(',')
                .append(csv(log.getOperatorUsername())).append(',')
                .append(csv(log.getModule())).append(',')
                .append(csv(log.getAction())).append(',')
                .append(csv(log.getTargetId())).append(',')
                .append(csv(log.getResult() != null && log.getResult() == 1 ? "成功" : "失败")).append(',')
                .append(csv(log.getDetail())).append(',')
                .append(csv(log.getCreateTime()))
                .append('\n');
        }

        response.getWriter().write(csv.toString());
    }

    private LambdaQueryWrapper<SysOperationLog> buildQueryWrapper(
        String module,
        Integer result,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String keyword
    ) {
        return new LambdaQueryWrapper<SysOperationLog>()
            .eq(StringUtils.hasText(module), SysOperationLog::getModule, module)
            .eq(result != null, SysOperationLog::getResult, result)
            .ge(startTime != null, SysOperationLog::getCreateTime, startTime)
            .le(endTime != null, SysOperationLog::getCreateTime, endTime)
            .and(StringUtils.hasText(keyword), wrapper -> wrapper
                .like(SysOperationLog::getOperatorUsername, keyword)
                .or()
                .like(SysOperationLog::getAction, keyword)
                .or()
                .like(SysOperationLog::getDetail, keyword)
            )
            .orderByDesc(SysOperationLog::getCreateTime)
            .orderByDesc(SysOperationLog::getId);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        if (text.contains("\"") || text.contains(",") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}
