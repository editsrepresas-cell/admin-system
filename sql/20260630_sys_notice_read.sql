CREATE TABLE IF NOT EXISTS sys_notice_read (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  notice_id BIGINT NOT NULL COMMENT '公告ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  UNIQUE KEY uk_notice_user (notice_id, user_id),
  KEY idx_user_id (user_id),
  KEY idx_notice_id (notice_id)
) COMMENT='公告阅读记录表';
