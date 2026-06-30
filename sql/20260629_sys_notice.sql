CREATE TABLE IF NOT EXISTS sys_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(100) NOT NULL COMMENT '公告标题',
  notice_type VARCHAR(50) NOT NULL COMMENT '公告类型',
  content TEXT NOT NULL COMMENT '公告内容',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿，1已发布',
  create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_notice_type (notice_type),
  KEY idx_notice_status (status),
  KEY idx_notice_create_time (create_time)
) COMMENT='系统通知公告表';

INSERT INTO sys_notice (title, notice_type, content, status, create_by, publish_time)
SELECT '系统上线通知', 'system', '后台管理系统基础模块已上线，请按权限使用各项功能。', 1, 1, NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM sys_notice WHERE title = '系统上线通知' AND deleted = 0
);

INSERT INTO sys_notice (title, notice_type, content, status, create_by, publish_time)
SELECT '账号安全提醒', 'business', '请定期修改登录密码，不要与他人共享账号。', 1, 1, NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM sys_notice WHERE title = '账号安全提醒' AND deleted = 0
);

SET @notice_parent_id := (SELECT id FROM sys_permission WHERE permission_code = 'notice' LIMIT 1);

INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, sort, status)
SELECT 0, 'notice', '通知公告', 'MENU', 9, 1
WHERE @notice_parent_id IS NULL;

SET @notice_parent_id := (SELECT id FROM sys_permission WHERE permission_code = 'notice' LIMIT 1);

INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES
(@notice_parent_id, 'notice:list', '公告列表', 'BUTTON', 1, 1),
(@notice_parent_id, 'notice:create', '新增公告', 'BUTTON', 2, 1),
(@notice_parent_id, 'notice:update', '编辑公告', 'BUTTON', 3, 1),
(@notice_parent_id, 'notice:publish', '发布公告', 'BUTTON', 4, 1),
(@notice_parent_id, 'notice:delete', '删除公告', 'BUTTON', 5, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  sort = VALUES(sort),
  status = VALUES(status),
  deleted = 0;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE permission_code = 'notice' OR permission_code LIKE 'notice:%';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission
WHERE permission_code IN ('notice', 'notice:list', 'notice:create', 'notice:update', 'notice:publish', 'notice:delete');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission
WHERE permission_code IN ('notice', 'notice:list', 'notice:create', 'notice:update');
