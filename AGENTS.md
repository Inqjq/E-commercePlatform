# AGENTS.md

> 本文件用于指导 AI 编程代理（Codex 等）在本仓库内工作的约定与约束。项目：**渡风电商平台**（Spring Boot + Vue）。

## 一、项目概览

渡风电商平台是一个 **B2C 自营 + 商家入驻** 的综合性电商系统，采用前后端分离架构：

- 后端：Spring Boot 3.x、JDK 17、MySQL 8、Redis 7
- 前端：Vue 3（Composition API）、Vite、Pinia、Vue Router 4、Element Plus
- 交互：前后端通过 RESTful API 通信，Nginx 反向代理

需求基线见 `docs/渡风电商平台需求分析文档.md`，任何实现应与该文档保持一致。

## 二、预计目录结构

```text
E-commercePlatform/
├── backend/            # Spring Boot 后端
│   ├── src/main/java/com/dufeng/...
│   ├── src/main/resources/...
│   └── src/test/java/...
├── frontend/           # Vue 3 前端
│   ├── src/
│   │   ├── api/        # Axios 请求封装
│   │   ├── views/      # 页面
│   │   ├── components/ # 组件
│   │   ├── stores/     # Pinia 状态
│   │   └── router/     # 路由
│   ├── package.json
│   └── vite.config.*
├── docs/               # 文档
└── AGENTS.md
```

> 当前为规划结构，实际代码落地时可按模块（user、goods、order、payment 等）在 backend 内分包。

## 三、技术约定

### 3.1 后端

- 分层：`Controller → Service → Mapper/Repository`，禁止在 Controller 直接写业务逻辑。
- 后端包名：`com.dufeng.<module>`，模块按领域划分（如 user、goods、order、payment）。
- 统一返回结构：`{ code: 0, message: success, data: ... }`，`code=0` 表示成功。
- 认证鉴权：JWT + Redis 会话黑名单，敏感接口需校验接口级权限（RBAC）。
- 参数校验：使用 Bean Validation（`@Valid`/`@NotBlank` 等），错误码统一。
- 密码存储：BCrypt 哈希，禁止明文；敏感信息脱敏展示。
- 数据库：MySQL 8，表名使用 `t_` 前缀、snake_case；字段命名 snake_case。
- 数据变更：使用迁移脚本（Flyway/Liquibase）管理，避免手工改库。
- 统一异常处理：全局 `@RestControllerAdvice`，避免将堆栈暴露给前端。

### 3.2 前端

- 使用 Vue 3 Composition API（`<script setup>`），优先函数式组合而非选项式。
- 状态管理使用 Pinia，按模块拆分 store；避免把全部状态塞入单一 store。
- 路由使用 Vue Router 4，页面路由懒加载。
- 统一使用 Axios 实例，封装请求拦截与统一错误处理。
- 组件风格与现有 Element Plus 组件保持一致，避免重复造轮子。
- 使用 TypeScript 时避免滥用 `any`，接口通过类型定义约束。

## 四、编码与格式规范

- 命名：Java/JS 使用 `camelCase`；类名/组件名 `PascalCase`；常量 `UPPER_SNAKE`。
- 前端对齐：语句以分号结尾（按项目现有 Prettier 配置），缩进 2 空格。
- 遵循现有格式化工具；后端可使用 Google Java Format/Checkstyle，前端使用 Prettier + ESLint。
- 除非明确要求，不添加许可证/版权头，不添加无关注释。
- 修改需最小且聚焦，优先根治问题，不做无关重构。
- 注释用中文可接受，公开 API/复杂逻辑建议补充 JSDoc / Javadoc。

## 五、Git 分支与提交规范

- 默认分支：`master`。
- 开发分支前缀：`codex/`（例如 `codex/goods-api`），除非用户另有指定。
- 除非用户明确要求，否则**不要**创建分支或执行 `git commit`。
- 提交信息建议遵循 Conventional Commits：`feat/fix/docs/refactor/test/style/chore`。
- 示例：`feat(goods): add goods list API`、`fix(order): handle duplicate payment callback`。

## 六、测试

- 后端：JUnit 5 + Spring Boot Test + Mockito，核心业务（下单、库存、支付回调）应有单测。
- 前端：Vitest（如适用），组件与 store 建议覆盖。
- 常用命令（规划）：
  - 后端：`mvn test` / `./mvnw test`（backend 目录）
  - 前端：`npm install`、`npm run dev`、`npm run test`（frontend 目录）
- 完成修改后应运行相关测试；若仓库尚无测试或无法运行，须在交付说明中注明。

## 七、API 与文档

- 接口前缀：前台 `/api/portal`、商家 `/api/merchant`、平台 `/api/admin`。
- 接口遵循 RESTful：GET 查询、POST 新增/提交、PUT 修改、DELETE 删除。
- 使用 OpenAPI / Swagger 自动生成接口文档，保持与实际接口一致。
- 修改接口或需求后，同步更新 `docs/` 下文档或相关注释。

## 八、安全与合规

- 遵循《个人信息保护法》《网络安全法》，涉及用户隐私的数据需脱敏并明示授权。
- 防御 SQL 注入、XSS、CSRF；使用参数化查询；限制请求频率。
- 支付回调必须验签且幂等；订单/库存变更需具备事务与补偿。
- 操作日志与登录日志留痕，满足审计要求。

## 九、完成任务的通用要求

- 先理解现状，再动手；改动应符合仓库现有风格与结构。
- 修复问题需定位根因，不做表面修补；不去修复与本次任务无关的缺陷。
- 修改完成后，如适用，运行验证（编译/测试/格式化）；无法运行时明确说明。
- 最终输出须说明改动了哪些文件、为何这样做、有哪些待用户验证的步骤。
