# 后台管理系统

在线演示：https://editsrepresas-cell.github.io/admin-system/

项目源码：https://github.com/editsrepresas-cell/admin-system

企业级后台管理系统基础版本，包含用户、角色、权限、部门、岗位、字典、通知公告、操作日志、认证鉴权、仪表盘和系统设置等后台基础能力。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Element Plus
- 后端：Spring Boot 3、Java 17、MyBatis Plus、MySQL
- 认证：Bearer Token

## 目录结构

```text
admin-web/      前端项目
admin-server/   后端项目
sql/            数据库初始化和增量脚本
```

## 环境要求

- Node.js 20 或更高版本
- Java 17
- Maven 3.9 或使用项目 Maven Wrapper
- MySQL 8.x

## 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS admin_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 按时间顺序执行 `sql/` 目录下脚本。

PowerShell 不支持直接使用 `<` 重定向给 `mysql`，推荐以下任一方式：

```bat
cmd /c "mysql -u root -p admin_system < C:\temp\20260628_sys_permission.sql"
```

或进入 MySQL 后执行：

```sql
SOURCE C:/temp/20260628_sys_permission.sql;
```

如果项目路径包含中文，建议先把 SQL 文件复制到 `C:/temp/` 再执行。

## 后端配置

后端配置文件位于 `admin-server/src/main/resources/application.properties`，支持通过环境变量覆盖：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | 后端服务端口 |
| `DB_URL` | 本地 `admin_system` | MySQL 连接地址 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `030523` | 数据库密码 |
| `APP_JWT_SECRET` | 本地开发默认值 | Token 签名密钥，生产环境必须替换 |
| `APP_JWT_EXPIRE_SECONDS` | `7200` | Token 过期时间，单位秒 |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | 允许跨域来源，多个用英文逗号分隔 |

生产环境至少应覆盖：

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/admin_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:APP_JWT_SECRET="替换为足够长的随机密钥"
$env:APP_CORS_ALLOWED_ORIGINS="https://你的前端域名"
```

## 启动后端

```powershell
cd admin-server
.\mvnw.cmd spring-boot:run
```

如果本机 Maven Wrapper 执行异常，可使用本机 Maven：

```powershell
mvn spring-boot:run
```

健康检查：

```powershell
curl http://127.0.0.1:8081/api/health
```

期望返回：

```json
{"code":200,"message":"success","data":"admin-server is running"}
```

## 启动前端

```powershell
cd admin-web
npm install
cmd /c npm.cmd run dev
```

默认访问：

```text
http://127.0.0.1:5173
```

前端开发代理在 `admin-web/vite.config.ts` 中配置，默认代理 `/api` 到 `http://localhost:8081`。

## 构建

前端：

```powershell
cd admin-web
cmd /c npm.cmd run build
```

后端：

```powershell
cd admin-server
.\mvnw.cmd -DskipTests package
```

## 功能清单

- 登录认证、Token 自动携带、登录失效跳转
- 用户管理：分页、搜索、新增、编辑、删除、重置密码、部门岗位绑定、导出
- 角色管理：新增、编辑、删除、权限配置
- 权限管理：权限树、新增、编辑、删除、菜单/按钮权限维护
- 部门管理：树形列表、新增、编辑、删除、新增下级、删除前业务校验
- 岗位管理：列表、新增、编辑、删除、删除前业务校验
- 字典管理：字典类型和字典数据维护
- 通知公告：新增、编辑、发布、下线、删除、详情
- 操作日志：列表、筛选、时间范围、详情、导出、成功/失败记录
- 系统设置：当前账号信息、修改密码
- 仪表盘：用户、角色、组织、公告和操作统计
- 前后端权限控制：菜单、按钮、接口权限

## 验收建议

1. 使用超级管理员登录，确认所有菜单和按钮可见。
2. 使用普通用户登录，确认无权限菜单和按钮不可见。
3. 直接调用无权限接口，应返回 `401` 或 `403`。
4. 新增、编辑、删除任一模块数据后，列表应立即刷新。
5. 删除部门或岗位时，如果已有用户绑定，应禁止删除。
6. 登录失败、修改密码失败、关键增删改操作应写入操作日志。
7. 操作日志按模块、结果、时间、关键词筛选应正确。
8. 操作日志和用户导出文件应能正常下载，Excel 打开中文不乱码。

## 常见问题

- Vite 构建中的 `#__PURE__` 和 chunk size 警告来自依赖和打包体积提示，目前不影响运行。
- 如果 PowerShell 中中文显示乱码，先用编辑器确认文件实际编码，不要直接替换中文内容。
- 如果接口返回 403，优先检查 token、请求头、后端权限配置和当前角色权限关联。
- 用户删除为逻辑删除，列表查询必须过滤 `deleted = 0`。
