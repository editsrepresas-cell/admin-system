USE admin_system;

SET @column_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'post_id'
);

SET @add_column_sql := IF(
  @column_exists = 0,
  'ALTER TABLE sys_user ADD COLUMN post_id BIGINT NULL COMMENT ''岗位ID'' AFTER dept_id',
  'SELECT 1'
);

PREPARE add_column_stmt FROM @add_column_sql;
EXECUTE add_column_stmt;
DEALLOCATE PREPARE add_column_stmt;

SET @index_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND INDEX_NAME = 'idx_user_post_id'
);

SET @add_index_sql := IF(
  @index_exists = 0,
  'ALTER TABLE sys_user ADD INDEX idx_user_post_id (post_id)',
  'SELECT 1'
);

PREPARE add_index_stmt FROM @add_index_sql;
EXECUTE add_index_stmt;
DEALLOCATE PREPARE add_index_stmt;

UPDATE sys_user
SET post_id = (
  SELECT id
  FROM sys_post
  WHERE post_code = 'DEPT_MANAGER'
    AND deleted = 0
  LIMIT 1
)
WHERE username = 'admin'
  AND deleted = 0
  AND post_id IS NULL;

UPDATE sys_user
SET post_id = (
  SELECT id
  FROM sys_post
  WHERE post_code = 'SPECIALIST'
    AND deleted = 0
  LIMIT 1
)
WHERE deleted = 0
  AND post_id IS NULL;
