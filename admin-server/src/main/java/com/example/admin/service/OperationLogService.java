package com.example.admin.service;

import com.example.admin.entity.SysOperationLog;
import com.example.admin.mapper.SysOperationLogMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogService {

    private final SysOperationLogMapper sysOperationLogMapper;

    public OperationLogService(SysOperationLogMapper sysOperationLogMapper) {
        this.sysOperationLogMapper = sysOperationLogMapper;
    }

    public void record(Authentication authentication, String module, String action, Long targetId, String detail) {
        String username = authentication == null ? "SYSTEM" : authentication.getName();
        Long operatorId = null;

        if (authentication != null && authentication.getDetails() instanceof Long id) {
            operatorId = id;
        }

        record(operatorId, username, module, action, targetId, detail);
    }

    public void record(Long operatorId, String username, String module, String action, Long targetId, String detail) {
        record(operatorId, username, module, action, targetId, detail, 1);
    }

    public void recordFailure(Long operatorId, String username, String module, String action, Long targetId, String detail) {
        record(operatorId, username, module, action, targetId, detail, 0);
    }

    private void record(Long operatorId, String username, String module, String action, Long targetId, String detail, Integer result) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setOperatorId(operatorId);
            log.setOperatorUsername(StringUtils.hasText(username) ? username : "SYSTEM");
            log.setModule(module);
            log.setAction(action);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setResult(result);

            sysOperationLogMapper.insert(log);
        } catch (Exception ignored) {
            // Operation logs must not block business flows.
        }
    }
}
