CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
  permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
  permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
  permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：MENU菜单，BUTTON按钮',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_permission_code (permission_code)
) COMMENT='系统权限表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) COMMENT='角色权限关联表';

INSERT INTO sys_permission (id, parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES
(1, 0, 'dashboard', '仪表盘', 'MENU', 1, 1),
(2, 0, 'user', '用户管理', 'MENU', 2, 1),
(3, 2, 'user:list', '用户列表', 'BUTTON', 1, 1),
(4, 2, 'user:create', '新增用户', 'BUTTON', 2, 1),
(5, 2, 'user:update', '编辑用户', 'BUTTON', 3, 1),
(6, 2, 'user:delete', '删除用户', 'BUTTON', 4, 1),
(7, 2, 'user:reset-password', '重置密码', 'BUTTON', 5, 1),
(8, 0, 'role', '角色管理', 'MENU', 3, 1),
(9, 8, 'role:list', '角色列表', 'BUTTON', 1, 1),
(10, 8, 'role:create', '新增角色', 'BUTTON', 2, 1),
(11, 8, 'role:update', '编辑角色', 'BUTTON', 3, 1),
(12, 8, 'role:delete', '删除角色', 'BUTTON', 4, 1),
(13, 8, 'role:permission', '权限配置', 'BUTTON', 5, 1),
(14, 0, 'operation-log', '操作日志', 'MENU', 4, 1),
(15, 14, 'operation-log:list', '日志列表', 'BUTTON', 1, 1),
(16, 0, 'settings', '系统设置', 'MENU', 5, 1),
(17, 16, 'settings:view', '查看设置', 'BUTTON', 1, 1),
(18, 16, 'settings:change-password', '修改密码', 'BUTTON', 2, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission
WHERE permission_code <> 'role:delete';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission
WHERE permission_code IN ('dashboard', 'user', 'user:list');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission
WHERE permission_code IN ('dashboard');
