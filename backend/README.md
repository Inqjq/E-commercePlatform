# 渡风电商平台后端

基于 Spring Boot 3.3.5 + JDK 17 的模块化单体后端，面向「前台消费者 / 商家入驻 / 平台管理」三个使用端。

## 技术栈

- Spring Boot 3.3.5、JDK 17
- Spring Security + JWT（jjwt 0.12.x）+ Redis 会话与黑名单
- MyBatis-Plus 3.5.7
- MySQL 8 + Flyway（数据库迁移）
- Redis 7
- Bean Validation、springdoc-openapi（Swagger UI）

## 目录结构

```text
backend/
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/dufeng
    │   │   ├── common        # 统一返回、异常、常量、实体基类
    │   │   ├── config        # 安全、MyBatis-Plus、OpenAPI、种子数据
    │   │   ├── security      # JWT 过滤器、会话服务、权限工具
    │   │   └── module        # user/address/goods/cart/order/payment/review/merchant/admin
    │   └── resources
    │       ├── application.yml
    │       └── db/migration/V1__init.sql
    └── test/java/com/dufeng
```

## 运行方式

1. 创建 MySQL 数据库：`CREATE DATABASE dufeng DEFAULT CHARSET utf8mb4;`
2. 启动 Redis（默认 `localhost:6379`）。
3. 凭证通过环境变量注入：`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`、`PAY_SECRET` 等。生产环境**不提供默认值**，缺失即启动失败；本地开发由 `application-dev.yml` 提供默认值，`mvn spring-boot:run` 可直接跑通。
4. 启动应用：
   ```bash
   mvn spring-boot:run
   ```
5. 访问 Swagger：http://localhost:8080/swagger-ui.html（生产 profile 自动关闭）

首次启动会由 Flyway 自动建表。`DataInitializer` 仅在 `dev` profile 生成演示账号（幂等），生产需自行创建管理员：
首次启动会由 Flyway 自动建表并播种初始化数据：`V1__init.sql` 建表，`V3__init_seed_cloud.sql` 从云服务器数据库导出演示账号、类目、品牌、商家、店铺、商品与 SKU（全部 `INSERT IGNORE`，幂等，可安全重复执行）。因此下列 demo 账号可直接登录；生产环境如需重置数据，以云库为准或自行创建管理员。

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 平台管理员 | admin | admin123 |
| 入驻商家 | merchant | merchant123 |
| 普通用户 | demo | demo123 |

## 接口前缀与鉴权

- 前台：`/api/portal/**`
- 商家：`/api/merchant/**`（需 `ROLE_MERCHANT`，`Authorization: Bearer <token>`）
- 平台：`/api/admin/**`（需 `ROLE_ADMIN`，`Authorization: Bearer <token>`）

登录接口 `POST /api/portal/auth/login` 返回 JWT；统一响应结构 `{code, message, data}`，`code=0` 为成功。

## 支付回调说明（模拟实现）

真实环境应接入微信支付/支付宝 SDK。当前为可扩展的模拟实现：

- 签名算法：`HmacSHA256(orderNo|amount|channel|payNo|secret)`，密钥从环境变量 `PAY_SECRET` 注入，禁止硬编码
- 回调端点为公开接口 `POST /api/portal/pay/callback`，需携带 `sign`；回调会校验金额与应付一致、流水与订单归属一致，`status` 缺省按失败处理，且回调幂等
- 开发/联调环境可开启 `dufeng.pay.mock-callback-enabled`（dev 默认开启）：发起支付后由服务端模拟网关回调，形成支付闭环；生产必须关闭

## 支付宝沙箱接入

`alipay` 渠道在 `dufeng.alipay.enabled=true` 时走真实支付宝（默认关闭，退回模拟回调）。

### 1. 准备沙箱参数

登录 [支付宝开放平台沙箱](https://open.alipay.com/develop/sandbox/app)，获取：

| 配置项 | 环境变量 | 说明 |
| --- | --- | --- |
| APPID | `ALIPAY_APP_ID` | 沙箱应用 APPID（9021 开头） |
| 应用私钥 | `ALIPAY_PRIVATE_KEY` | 用官方"密钥工具"生成（PKCS8/RSA2），工具同时给出应用公钥，需粘贴回沙箱控制台 |
| 支付宝公钥 | `ALIPAY_PUBLIC_KEY` | 沙箱控制台"查看"里给的是支付宝公钥（不是应用公钥） |
| 网关 | `ALIPAY_GATEWAY` | 已默认沙箱网关 `https://openapi-sandbox.dl.alipaydev.com/gateway.do`，生产换 `https://openapi.alipay.com/gateway.do` |
| 异步通知 | `ALIPAY_NOTIFY_URL` | 公网可访问的 `https://你的域名/api/portal/pay/callback/alipay/notify` |
| 回跳地址 | `ALIPAY_RETURN_URL` | 前端站点地址，如 `http://localhost:5173` |

### 2. 启用与测试

```bash
export ALIPAY_ENABLED=true ALIPAY_APP_ID=... ALIPAY_PRIVATE_KEY=... ALIPAY_PUBLIC_KEY=...
mvn spring-boot:run
```

- 下单支付选择「支付宝」→ 后端返回收银台跳转地址 → 前端自动跳转 → 用**沙箱买家账号**（沙箱控制台"沙箱账号"页）登录付款；
- 支付结果有两条入账通道，均带金额比对与 CAS 状态机：异步 notify（需公网地址）+ 收银台轮询主动查单（`GET /api/portal/pay/{orderNo}/status`，本地开发即可闭环）；
- 沙箱测试无需真实扣款，沙箱买家账户自带余额。

### 3. 安全约束

- 异步通知用**支付宝公钥**验签（`AlipaySignature.rsaCheckV1`），同时校验 `app_id` 匹配；
- 入账金额必须与支付流水一致，`out_trade_no` 对应支付流水号（payNo），订单推进复用统一 CAS（`markGatewayPaid`）；
- 未启用支付宝时 `alipay` 渠道自动退回模拟实现，开关切换不影响其余渠道。

## 测试与构建

```bash
mvn test
mvn package -DskipTests
```

## 安全机制速览

- 订单状态流转（支付回调/取消/关单/发货/收货）全部基于条件 UPDATE（CAS），冲突方更新 0 行即回滚，避免资金/库存不一致
- 未支付订单由 `OrderTimeoutTask` 每 30 秒扫描关单并回补库存，超时时长由 `dufeng.order.timeout-minutes` 控制
- 商品编辑时 SKU 原地更新（保留主键），未携带的旧规格下架而非删除；请求不含 skus 时不触碰规格
- 短信验证码：错 5 次作废、单手机号日发 10 条上限、日志脱敏；绑定手机号的注册必须携带验证码
- 登录连续失败 5 次锁定 15 分钟；改密/重置/禁用/角色变更会失效存量会话（过滤器逐请求校验）
- 下单幂等键基于 Redis SETNX 原子占位；分页全局最大 200 条
- 管理端敏感操作通过 `@AdminAudit` 注解落库审计，查询接口 `GET /api/admin/logs`

## 需求覆盖

已实现需求文档中的 P0 核心链路，并按模块维护错误码（用户 1xxxx、商品 2xxxx、订单 3xxxx、支付 4xxxx、平台 5xxxx），详见 `ResultCode`。
