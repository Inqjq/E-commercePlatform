# 渡风电商平台 · 前端

基于 **Vue 3 + Vite + Pinia + Vue Router 4 + Element Plus** 的电商平台单页应用（SPA），覆盖 **前台消费者端 / 商家端 / 平台管理端** 三大子系统，界面语言为中文。

## 技术栈

- Vue 3（Composition API，`<script setup>`）
- Vite 6 构建
- Pinia 状态管理（用户、购物车、全局 UI）
- Vue Router 4（路由懒加载 + 登录/角色守卫）
- Element Plus UI 组件库 + 图标
- Axios 请求封装（统一拦截、鉴权头、统一错误提示）
- 内置 **Mock 数据层**，后端未就绪即可独立运行演示

## 目录结构

```text
frontend/
├── index.html
├── package.json
├── vite.config.js
├── public/
└── src/
    ├── main.js              # 应用入口，注册 Element Plus / Pinia / Router
    ├── App.vue
    ├── api/                 # Axios 实例与各业务模块接口
    ├── assets/styles/       # 全局样式与 CSS 变量
    ├── components/          # 通用组件（商品卡片等）
    ├── layouts/             # 三大端布局（Portal/Merchant/Admin）
    ├── mock/                # Mock 适配器与演示数据
    ├── router/              # 路由与权限守卫
    ├── stores/              # Pinia 状态
    ├── utils/               # 鉴权、格式化、常量
    └── views/
        ├── portal/          # 前台：首页/商品/购物车/订单/个人中心等
        ├── merchant/        # 商家端：商品/库存/订单/售后/营销
        └── admin/           # 平台端：用户/商家/审核/类目/RBAC/日志
```

## 快速开始

```bash
cd frontend
npm install
npm run dev      # 开发环境，默认 http://localhost:5173
npm run build    # 生产构建，输出到 dist/
npm run preview  # 预览生产构建
```

## Mock 与真实后端切换

开发环境默认开启 Mock（`.env.development` 中 `VITE_USE_MOCK=true`），因此无需后端即可跑通全部流程。

切换到真实后端：

1. 修改 `.env.development`：`VITE_USE_MOCK=false`。
2. 确认 `vite.config.js` 中 `/api` 代理指向后端（默认 `http://localhost:8080`）。
3. 后端按 `docs/渡风电商平台需求分析文档.md` 第 8 章接口规范提供 `/api/portal`、`/api/merchant`、`/api/admin` 接口。

## 功能范围

已实现需求文档中 **P0 / P1** 关键能力：

- **前台**：注册登录（密码/短信）、首页轮播与分类、商品列表（筛选/排序/分页）、商品详情（SKU/评价）、购物车、结算下单、收银台支付、订单中心、售后申请、优惠券、个人中心、收货地址、收藏、消息。
- **商家端**：经营工作台、商品发布/编辑/上下架、库存管理、订单发货、售后处理、营销活动。
- **平台端**：数据看板、用户管理、商家入驻审核、商品审核、类目管理、品牌管理、营销管理、内容管理、角色权限（RBAC）、日志审计。

> 说明：演示数据为内置 Mock，图片使用内联 SVG data URI，离线可用。
