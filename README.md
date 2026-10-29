# CustomerCollection 客户信息收集与维护 APP

- 前端：Vue 3 + Vite + Vant 4 + Pinia + Vue Router（移动端 H5，可后续用 Capacitor 打包）
- 后端：Java 17 + Spring Boot 3.2 + MyBatis-Plus + MySQL
- 文件存储：本地磁盘（`backend/upload`）
- 登录鉴权：JWT

## 目录结构

```text
客户信息手机app/
├─ backend/                     Spring Boot 后端
│  ├─ src/main/java/com/zlyd/customercollection/
│  │  ├─ controller/            接口层
│  │  ├─ service/               业务层
│  │  ├─ mapper/                MyBatis-Plus Mapper
│  │  ├─ entity/                实体
│  │  ├─ dto/                   请求/响应对象
│  │  ├─ config/                配置、初始化数据
│  │  ├─ security/              JWT 与登录拦截
│  │  └─ common/                统一返回、异常、上下文
│  └─ src/main/resources/
│     ├─ application.yml
│     └─ db/schema.sql、data.sql
└─ frontend/                    Vue 3 前端
   └─ src/
      ├─ views/                 页面
      ├─ components/            表单、附件上传/展示、选择器
      ├─ api/                   接口封装
      ├─ stores/                Pinia
      ├─ router/                路由
      └─ utils/                 请求封装、常量、格式化
```

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+（推荐 20）
- MySQL 5.7+ / 8.x

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认配置：

| 项目 | 默认值 | 环境变量 |
|---|---|---|
| 端口 | 8081 | `SERVER_PORT` |
| 数据库地址 | 127.0.0.1:3306 | `DB_HOST`、`DB_PORT` |
| 数据库名 | customer_collection | `DB_NAME` |
| 数据库用户 | root | `DB_USERNAME` |
| 数据库密码 | 123456 | `DB_PASSWORD` |
| 文件根目录 | `./upload` | `FILE_UPLOAD_ROOT` |
| 登录图形验证码 | 开启 | `CAPTCHA_ENABLED=false` 可关闭（仅本机调试） |
| 邮件发送 | 关闭 | `MAIL_ENABLED=true` 并配置 `MAIL_HOST`、`MAIL_USERNAME`、`MAIL_PASSWORD`、`MAIL_FROM` |
| 演示账号初始化 | 开启 | `DEMO_USER_ENABLED=false` 可关闭 |
| 拜访提醒定时任务 | 每天 08:00 | `REMINDER_CRON`（Spring 6 位 cron） |

启动时会自动：

1. 创建 `customer_collection` 数据库（需账号有建库权限）。
2. 执行 `db/schema.sql` 建表（`CREATE TABLE IF NOT EXISTS`，可重复执行）。
3. 执行 `db/data.sql` 写入全局拜访提醒规则和系统配置。
4. 创建演示账号和演示客户数据，并生成一次演示提醒消息。

演示账号：`demo / 123456`

> 本机 8080 已被其他 Java 程序占用，因此后端默认使用 8081；如无冲突可用 `SERVER_PORT=8080` 覆盖。

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。Vite 已把 `/api`、`/upload` 代理到 `http://127.0.0.1:8081`。

生产构建：

```bash
npm run build
```

构建产物在 `frontend/dist`，可交给 Nginx 托管，并把 `/api`、`/upload` 反向代理到后端。

## 已实现功能

### 用户

- 图形验证码登录、登录失败原因提示
- 注册：手机号/邮箱格式校验、邮箱验证码（开发模式验证码自动回填并弹提示）
- 个人信息查看、头像上传（仅图片、预览、替换旧头像）
- 修改用户名/城市、修改密码（成功后强制重新登录）
- 注销登录

### 客户

- 客户列表、搜索、下拉刷新、分页加载
- 弹窗添加客户
- 客户详情、修改、屏蔽/恢复、删除
- 客户项目录入与展示
- 客户附件统一存储与下载

### 拜访记录

- 客户详情内新增拜访记录，支持现场图片/文件附件
- 拜访记录列表、查看详情
- 底部弹窗修改拜访记录
- 新增/修改拜访后自动刷新客户最后拜访时间，并处理超期提醒

### 合同

- 客户详情内新增合同，支持合同附件（本地私有目录）
- 合同列表、搜索、查看详情
- 底部弹窗修改合同
- 合同金额、回款状态展示

### 最近消息与拜访推送

- 全局提醒规则：3 个月（按天）、5 个月（按周），可在 `visit_reminder_rule` 表调整
- 定时任务每天 08:00 扫描超期未拜访客户
- 重复推送间隔控制 + `customer_id + rule_id + push_date` 唯一索引防重
- 同一客户同一规则只保留一条未读，历史消息保留
- 最近消息列表、未读筛选、单条已读、全部已读、未读角标
- 点击提醒进入客户详情
- 仅站内消息，不做手机通知栏离线推送

手动触发一次提醒任务（演示用）：

```bash
curl -X POST http://127.0.0.1:8081/api/message/reminder/run \
  -H "Authorization: Bearer <登录返回的 token>"
```

### 附件

- 统一 `attachment` 表，合同/拜访/客户/项目/头像共用
- 图片、PDF、Word、Excel、PPT、TXT、ZIP、RAR
- 扩展名、MIME、图片文件头三重校验
- 头像和业务图片公开访问；合同等文档私有存储，下载走鉴权接口
- 图片自动生成缩略图
- 未绑定业务的临时附件（`biz_id = 0`）可后续增加定时清理

## 主要接口

| 模块 | 接口 |
|---|---|
| 认证 | `GET /api/auth/captcha`、`POST /api/auth/login`、`POST /api/auth/register`、`POST /api/auth/email-code` |
| 用户 | `GET/PUT /api/user/profile`、`PUT /api/user/password`、`POST /api/user/avatar` |
| 客户 | `GET /api/customer/page`、`POST /api/customer`、`GET/PUT/DELETE /api/customer/{id}`、`PUT /api/customer/{id}/valid`、`POST /api/customer/{id}/project` |
| 拜访 | `GET /api/visit/page`、`POST /api/visit`、`GET/PUT/DELETE /api/visit/{id}`、`GET /api/visit/customer/{customerId}` |
| 合同 | `GET /api/contract/page`、`POST /api/contract`、`GET/PUT/DELETE /api/contract/{id}`、`GET /api/contract/customer/{customerId}` |

列表查询参数：

| 页面 | 参数 |
|---|---|
| 客户 | `customerName`、`contactName`、`contactPhone` |
| 合同 | `customerName`、`signDateStart`、`signDateEnd`、`paymentStatus` |
| 拜访记录 | `customerName`、`visitDateStart`、`visitDateEnd` |

日期参数格式 `yyyy-MM-dd`，日期范围为闭区间；`paymentStatus` 为 `0未回款 / 1部分回款 / 2已回款`。
| 消息 | `GET /api/message/page`、`GET /api/message/unread-count`、`PUT /api/message/{id}/read`、`PUT /api/message/read-all` |
| 附件 | `POST /api/attachment/upload`、`GET /api/attachment/list`、`POST /api/attachment/bind`、`GET /api/attachment/{id}/download` |

所有业务接口（除 `/api/auth/**`）都需要请求头：`Authorization: Bearer <token>`。

## 数据库表

`schema.sql` 中共 11 张表：

`sys_user`、`sys_verification_code`、`sys_config`、`customer`、`customer_project`、`visit_record`、`contract`、`message`、`visit_reminder_rule`、`visit_reminder_record`、`attachment`。

字段与索引以 `backend/src/main/resources/db/schema.sql` 为准。

已有数据库首次升级到本次查询版本时，执行一次索引迁移：

```bash
mysql -u root -p customer_collection < backend/src/main/resources/db/migration_v2.sql
```

新数据库由 `schema.sql` 自动创建，无需执行迁移。

## 说明

- 当前工作区没有 `dbstruct.excel`，数据库结构按双方确认的功能设计落地；拿到 Excel 后可再做字段名对齐。
- 上传目录 `backend/upload/private` 不要配置静态服务器直出，合同等文件通过鉴权下载接口访问。
- 生产环境请修改 `JWT_SECRET`、数据库密码，并关闭演示账号。
- 删除均为逻辑删除；文件物理清理可后续加定时任务。
