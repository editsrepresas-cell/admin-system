CREATE TABLE IF NOT EXISTS sys_dept (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
  dept_name VARCHAR(80) NOT NULL COMMENT '部门名称',
  leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
  KEY idx_sys_dept_parent_id (parent_id),
  KEY idx_sys_dept_sort (sort)
) COMMENT='系统部门表';

INSERT INTO sys_dept (id, parent_id, dept_name, leader, phone, email, sort, status)
VALUES
(1, 0, '总经办', '霍新雨', '18737258951', 'office@example.com', 1, 1),
(2, 0, '技术中心', '张新宇', '18838248951', 'tech@example.com', 2, 1),
(3, 2, '研发部', '吕士杰', '18737268961', 'rd@example.com', 1, 1),
(4, 2, '测试部', '系统管理员', '13800000001', 'qa@example.com', 2, 1),
(5, 0, '运营中心', '运营人员', '13800000002', 'ops@example.com', 3, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  dept_name = VALUES(dept_name),
  leader = VALUES(leader),
  phone = VALUES(phone),
  email = VALUES(email),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT INTO sys_permission (id, parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES
(19, 0, 'dept', '部门管理', 'MENU', 3, 1),
(20, 19, 'dept:list', '部门列表', 'BUTTON', 1, 1),
(21, 19, 'dept:create', '新增部门', 'BUTTON', 2, 1),
(22, 19, 'dept:update', '编辑部门', 'BUTTON', 3, 1),
(23, 19, 'dept:delete', '删除部门', 'BUTTON', 4, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE permission_code LIKE 'dept:%' OR permission_code = 'dept';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE permission_code LIKE 'dept:%' OR permission_code = 'dept';
