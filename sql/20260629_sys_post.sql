USE admin_system;

CREATE TABLE IF NOT EXISTS sys_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_code VARCHAR(50) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    UNIQUE KEY uk_post_code (post_code)
) COMMENT='系统岗位表';

INSERT INTO sys_post (post_code, post_name, sort, status)
VALUES
('GENERAL_MANAGER', '总经理', 1, 1),
('DEPT_MANAGER', '部门经理', 2, 1),
('PROJECT_MANAGER', '项目经理', 3, 1),
('ENGINEER', '工程师', 4, 1),
('SPECIALIST', '专员', 5, 1)
ON DUPLICATE KEY UPDATE
    post_name = VALUES(post_name),
    sort = VALUES(sort),
    status = VALUES(status);

INSERT INTO sys_permission (id, parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES
(24, 0, 'post', '岗位管理', 'MENU', 5, 1),
(25, 24, 'post:list', '岗位列表', 'BUTTON', 1, 1),
(26, 24, 'post:create', '新增岗位', 'BUTTON', 2, 1),
(27, 24, 'post:update', '编辑岗位', 'BUTTON', 3, 1),
(28, 24, 'post:delete', '删除岗位', 'BUTTON', 4, 1)
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    permission_name = VALUES(permission_name),
    permission_type = VALUES(permission_type),
    sort = VALUES(sort),
    status = VALUES(status);

UPDATE sys_permission SET sort = 6 WHERE permission_code = 'role';
UPDATE sys_permission SET sort = 7 WHERE permission_code = 'operation-log';
UPDATE sys_permission SET sort = 8 WHERE permission_code = 'settings';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE permission_code LIKE 'post%';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE permission_code LIKE 'post%';
