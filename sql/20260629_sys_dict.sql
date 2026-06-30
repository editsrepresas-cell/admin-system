CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
  dict_name VARCHAR(50) NOT NULL COMMENT '字典名称',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_dict_type_code (dict_code)
) COMMENT='系统字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  dict_type_id BIGINT NOT NULL COMMENT '字典类型ID',
  dict_label VARCHAR(50) NOT NULL COMMENT '字典标签',
  dict_value VARCHAR(50) NOT NULL COMMENT '字典值',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  UNIQUE KEY uk_dict_data_type_value (dict_type_id, dict_value),
  KEY idx_dict_data_type_id (dict_type_id)
) COMMENT='系统字典数据表';

INSERT INTO sys_dict_type (id, dict_code, dict_name, sort, status)
VALUES
(1, 'sys_status', '系统状态', 1, 1),
(2, 'user_gender', '用户性别', 2, 1),
(3, 'notice_type', '通知类型', 3, 1)
ON DUPLICATE KEY UPDATE
  dict_name = VALUES(dict_name),
  sort = VALUES(sort),
  status = VALUES(status),
  deleted = 0;

INSERT INTO sys_dict_data (dict_type_id, dict_label, dict_value, sort, status, remark)
VALUES
(1, '启用', '1', 1, 1, '通用启用状态'),
(1, '禁用', '0', 2, 1, '通用禁用状态'),
(2, '男', 'male', 1, 1, NULL),
(2, '女', 'female', 2, 1, NULL),
(2, '未知', 'unknown', 3, 1, NULL),
(3, '系统通知', 'system', 1, 1, NULL),
(3, '业务通知', 'business', 2, 1, NULL)
ON DUPLICATE KEY UPDATE
  dict_label = VALUES(dict_label),
  sort = VALUES(sort),
  status = VALUES(status),
  remark = VALUES(remark),
  deleted = 0;

SET @dict_parent_id := (SELECT id FROM sys_permission WHERE permission_code = 'dict' LIMIT 1);

INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, sort, status)
SELECT 0, 'dict', '字典管理', 'MENU', 7, 1
WHERE @dict_parent_id IS NULL;

SET @dict_parent_id := (SELECT id FROM sys_permission WHERE permission_code = 'dict' LIMIT 1);

INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES
(@dict_parent_id, 'dict:list', '字典列表', 'BUTTON', 1, 1),
(@dict_parent_id, 'dict:create', '新增字典', 'BUTTON', 2, 1),
(@dict_parent_id, 'dict:update', '编辑字典', 'BUTTON', 3, 1),
(@dict_parent_id, 'dict:delete', '删除字典', 'BUTTON', 4, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  sort = VALUES(sort),
  status = VALUES(status),
  deleted = 0;

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE permission_code LIKE 'dict%';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE permission_code IN ('dict', 'dict:list', 'dict:create', 'dict:update');
