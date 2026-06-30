USE admin_system;

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND column_name = 'dept_id'
);

SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN dept_id BIGINT NULL COMMENT ''部门ID'' AFTER nickname',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_sys_user_dept_id'
);

SET @sql := IF(
    @index_exists = 0,
    'ALTER TABLE sys_user ADD INDEX idx_sys_user_dept_id (dept_id)',
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE sys_user SET dept_id = 1 WHERE username = 'admin' AND deleted = 0 AND dept_id IS NULL;
UPDATE sys_user SET dept_id = 5 WHERE username = 'operator' AND deleted = 0 AND dept_id IS NULL;
UPDATE sys_user SET dept_id = 3 WHERE username = 'test' AND deleted = 0 AND dept_id IS NULL;
