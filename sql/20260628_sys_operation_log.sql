CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
  operator_username VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
  module VARCHAR(50) NOT NULL COMMENT '模块',
  action VARCHAR(50) NOT NULL COMMENT '操作',
  target_id BIGINT DEFAULT NULL COMMENT '目标ID',
  detail VARCHAR(500) DEFAULT NULL COMMENT '详情',
  result TINYINT NOT NULL DEFAULT 1 COMMENT '结果：1成功，0失败',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='操作日志表';

CREATE INDEX idx_operation_log_create_time ON sys_operation_log (create_time);
CREATE INDEX idx_operation_log_operator ON sys_operation_log (operator_username);
CREATE INDEX idx_operation_log_module ON sys_operation_log (module);
