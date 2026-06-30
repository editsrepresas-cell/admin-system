USE admin_system;

INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES (0, 'permission', '权限管理', 'MENU', 9, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  sort = VALUES(sort),
  status = VALUES(status);

SET @permission_parent_id := (
  SELECT id
  FROM sys_permission
  WHERE permission_code = 'permission'
    AND deleted = 0
  LIMIT 1
);

INSERT INTO sys_permission (parent_id, permission_code, permission_name, permission_type, sort, status)
VALUES
(@permission_parent_id, 'permission:list', '权限列表', 'BUTTON', 1, 1),
(@permission_parent_id, 'permission:create', '新增权限', 'BUTTON', 2, 1),
(@permission_parent_id, 'permission:update', '编辑权限', 'BUTTON', 3, 1),
(@permission_parent_id, 'permission:delete', '删除权限', 'BUTTON', 4, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  permission_name = VALUES(permission_name),
  permission_type = VALUES(permission_type),
  sort = VALUES(sort),
  status = VALUES(status);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id
FROM sys_permission
WHERE permission_code = 'permission'
   OR permission_code LIKE 'permission:%';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id
FROM sys_permission
WHERE permission_code IN ('permission', 'permission:list');
