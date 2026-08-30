-- 渡风电商平台 初始化脚本
-- 表结构遵循 snake_case、t_ 前缀，字段命名与需求文档数据模型保持一致。

-- 用户表
CREATE TABLE t_user (
    id            BIGINT       NOT NULL COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '用户名',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email         VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    nickname      VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    avatar        VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    gender        TINYINT      DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    status        TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted       TINYINT      DEFAULT 0 COMMENT '逻辑删除 0正常 1删除',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 收货地址表
CREATE TABLE t_address (
    id           BIGINT      NOT NULL COMMENT '主键',
    user_id      BIGINT      NOT NULL COMMENT '用户ID',
    receiver     VARCHAR(64) NOT NULL COMMENT '收货人',
    phone        VARCHAR(20) NOT NULL COMMENT '联系电话',
    province     VARCHAR(64) NOT NULL COMMENT '省',
    city         VARCHAR(64) NOT NULL COMMENT '市',
    district     VARCHAR(64) NOT NULL COMMENT '区',
    detail       VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default   TINYINT     DEFAULT 0 COMMENT '是否默认 0否 1是',
    deleted      TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 商家表
CREATE TABLE t_merchant (
    id             BIGINT      NOT NULL COMMENT '主键',
    account_id     BIGINT      NOT NULL COMMENT '登录账号ID(t_user.id)',
    name           VARCHAR(128) NOT NULL COMMENT '商家名称',
    license_no     VARCHAR(128) DEFAULT NULL COMMENT '营业执照号',
    legal_person   VARCHAR(64)  DEFAULT NULL COMMENT '法人',
    contact_phone  VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    category_ids   VARCHAR(255) DEFAULT NULL COMMENT '经营类目ID集合',
    audit_status   TINYINT      DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2驳回 3冻结',
    audit_reason   VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝原因',
    status         TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted        TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_account_id (account_id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 店铺表
CREATE TABLE t_shop (
    id          BIGINT      NOT NULL COMMENT '主键',
    merchant_id BIGINT      NOT NULL COMMENT '商家ID',
    name        VARCHAR(128) NOT NULL COMMENT '店铺名称',
    logo        VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo',
    intro       VARCHAR(512) DEFAULT NULL COMMENT '店铺简介',
    service_phone VARCHAR(20) DEFAULT NULL COMMENT '客服电话',
    status      TINYINT     DEFAULT 1 COMMENT '状态 0关闭 1营业',
    deleted     TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- 类目表
CREATE TABLE t_category (
    id          BIGINT       NOT NULL COMMENT '主键',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父级ID，0为顶级',
    name        VARCHAR(64)  NOT NULL COMMENT '类目名称',
    level       INT          DEFAULT 1 COMMENT '层级',
    sort        INT          DEFAULT 0 COMMENT '排序',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品类目表';

-- 品牌表
CREATE TABLE t_brand (
    id          BIGINT       NOT NULL COMMENT '主键',
    name        VARCHAR(128) NOT NULL COMMENT '品牌名称',
    logo        VARCHAR(255) DEFAULT NULL COMMENT '品牌Logo',
    description VARCHAR(512) DEFAULT NULL COMMENT '品牌描述',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- 商品表(SPU)
CREATE TABLE t_goods (
    id          BIGINT       NOT NULL COMMENT '主键',
    shop_id     BIGINT       NOT NULL COMMENT '店铺ID',
    category_id BIGINT       NOT NULL COMMENT '类目ID',
    brand_id    BIGINT       DEFAULT NULL COMMENT '品牌ID',
    title       VARCHAR(255) NOT NULL COMMENT '商品标题',
    subtitle    VARCHAR(255) DEFAULT NULL COMMENT '副标题',
    main_image  VARCHAR(255) DEFAULT NULL COMMENT '主图',
    images      TEXT         DEFAULT NULL COMMENT '商品图集(JSON)',
    detail      LONGTEXT     DEFAULT NULL COMMENT '详情富文本',
    price       DECIMAL(12,2) DEFAULT 0.00 COMMENT '最低售价',
    status      TINYINT      DEFAULT 0 COMMENT '状态 0草稿 1待审核 2已上架 3已下架 4审核驳回',
    audit_status TINYINT     DEFAULT 0 COMMENT '审核状态 0未提交 1待审核 2通过 3驳回',
    sales       INT          DEFAULT 0 COMMENT '销量',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shop_id (shop_id),
    KEY idx_category_id (category_id),
    KEY idx_title (title(64)),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表(SPU)';

-- SKU表
CREATE TABLE t_sku (
    id          BIGINT       NOT NULL COMMENT '主键',
    goods_id    BIGINT       NOT NULL COMMENT '商品ID',
    sku_code    VARCHAR(64)  DEFAULT NULL COMMENT 'SKU编码',
    spec_json   VARCHAR(255) DEFAULT NULL COMMENT '规格JSON(如颜色:红;尺码:XL)',
    spec_text   VARCHAR(255) DEFAULT NULL COMMENT '规格文本(用于展示)',
    image       VARCHAR(255) DEFAULT NULL COMMENT '规格图',
    price       DECIMAL(12,2) NOT NULL COMMENT '价格',
    stock       INT          DEFAULT 0 COMMENT '库存',
    weight      DECIMAL(10,2) DEFAULT 0.00 COMMENT '重量(g)',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0下架 1上架',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU表';

-- 购物车表
CREATE TABLE t_cart_item (
    id          BIGINT   NOT NULL COMMENT '主键',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    sku_id      BIGINT   NOT NULL COMMENT 'SKU ID',
    goods_id    BIGINT   NOT NULL COMMENT '商品ID',
    shop_id     BIGINT   NOT NULL COMMENT '店铺ID',
    quantity    INT      NOT NULL DEFAULT 1 COMMENT '数量',
    checked     TINYINT  DEFAULT 1 COMMENT '是否选中 0否 1是',
    deleted     TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单主表
CREATE TABLE t_orders (
    id            BIGINT        NOT NULL COMMENT '主键',
    order_no      VARCHAR(64)   NOT NULL COMMENT '订单号',
    user_id       BIGINT        NOT NULL COMMENT '用户ID',
    shop_id       BIGINT        NOT NULL COMMENT '店铺ID',
    total_amount  DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '商品总额',
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    freight_amount  DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
    pay_amount    DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
    status        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态 0待付款 1待发货 2待收货 3已完成 4已取消 5售后中',
    receiver      VARCHAR(64)   DEFAULT NULL COMMENT '收货人快照',
    receiver_phone VARCHAR(20)  DEFAULT NULL COMMENT '收货电话快照',
    receiver_address VARCHAR(255) DEFAULT NULL COMMENT '收货地址快照',
    remark        VARCHAR(255)  DEFAULT NULL COMMENT '订单备注',
    cancel_reason VARCHAR(255)  DEFAULT NULL COMMENT '取消原因',
    expire_time   DATETIME      DEFAULT NULL COMMENT '支付超时时间',
    pay_time      DATETIME      DEFAULT NULL COMMENT '支付时间',
    ship_time     DATETIME      DEFAULT NULL COMMENT '发货时间',
    finish_time   DATETIME      DEFAULT NULL COMMENT '完成时间',
    logistics_company VARCHAR(64) DEFAULT NULL COMMENT '物流公司',
    logistics_no  VARCHAR(64)   DEFAULT NULL COMMENT '物流单号',
    version       INT           DEFAULT 0 COMMENT '乐观锁版本',
    deleted       TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_shop_id (shop_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单明细表
CREATE TABLE t_order_item (
    id          BIGINT        NOT NULL COMMENT '主键',
    order_id    BIGINT        NOT NULL COMMENT '订单ID',
    order_no    VARCHAR(64)   DEFAULT NULL COMMENT '订单号(冗余)',
    shop_id     BIGINT        NOT NULL COMMENT '店铺ID',
    goods_id    BIGINT        NOT NULL COMMENT '商品ID',
    sku_id      BIGINT        NOT NULL COMMENT 'SKU ID',
    goods_title VARCHAR(255)  DEFAULT NULL COMMENT '商品标题快照',
    spec_text   VARCHAR(255)  DEFAULT NULL COMMENT '规格快照',
    image       VARCHAR(255)  DEFAULT NULL COMMENT '商品图快照',
    price       DECIMAL(12,2) NOT NULL COMMENT '成交单价',
    quantity    INT           NOT NULL COMMENT '数量',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '小计',
    reviewed    TINYINT       DEFAULT 0 COMMENT '是否已评价 0否 1是',
    deleted     TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_goods_id (goods_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 支付流水表
CREATE TABLE t_payment (
    id            BIGINT        NOT NULL COMMENT '主键',
    order_id      BIGINT        NOT NULL COMMENT '订单ID',
    order_no      VARCHAR(64)   NOT NULL COMMENT '订单号',
    pay_no        VARCHAR(64)   NOT NULL COMMENT '支付流水号',
    user_id       BIGINT        NOT NULL COMMENT '用户ID',
    channel       VARCHAR(32)   NOT NULL COMMENT '渠道(alipay/wechat/balance)',
    amount        DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    status        TINYINT       NOT NULL DEFAULT 0 COMMENT '状态 0待支付 1成功 2失败 3已退款',
    callback_time DATETIME      DEFAULT NULL COMMENT '回调时间',
    deleted       TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_no (pay_no),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- 售后表
CREATE TABLE t_after_sale (
    id          BIGINT       NOT NULL COMMENT '主键',
    after_sale_no VARCHAR(64) NOT NULL COMMENT '售后单号',
    order_id    BIGINT       NOT NULL COMMENT '订单ID',
    order_item_id BIGINT     DEFAULT NULL COMMENT '订单明细ID(整单退时为空)',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    type        TINYINT      NOT NULL COMMENT '类型 1退款 2退货退款 3换货',
    reason      VARCHAR(255) DEFAULT NULL COMMENT '申请原因',
    evidence    TEXT         DEFAULT NULL COMMENT '凭证(JSON)',
    refund_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
    status      TINYINT      DEFAULT 0 COMMENT '状态 0待商家处理 1待退货 2已完成 3已拒绝 4平台介入',
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_after_sale_no (after_sale_no),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后表';

-- 评价表
CREATE TABLE t_review (
    id            BIGINT      NOT NULL COMMENT '主键',
    order_item_id BIGINT      NOT NULL COMMENT '订单明细ID',
    order_id      BIGINT      NOT NULL COMMENT '订单ID',
    goods_id      BIGINT      NOT NULL COMMENT '商品ID',
    user_id       BIGINT      NOT NULL COMMENT '用户ID',
    score         TINYINT     NOT NULL COMMENT '评分 1-5',
    content       VARCHAR(1024) DEFAULT NULL COMMENT '评价内容',
    images        TEXT        DEFAULT NULL COMMENT '评价图片(JSON)',
    anonymous     TINYINT     DEFAULT 0 COMMENT '是否匿名 0否 1是',
    status        TINYINT     DEFAULT 1 COMMENT '状态 0待审核 1公开 2隐藏',
    deleted       TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    create_time   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_item_id (order_item_id),
    KEY idx_goods_id (goods_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 优惠券表
CREATE TABLE t_coupon (
    id           BIGINT       NOT NULL COMMENT '主键',
    shop_id      BIGINT       DEFAULT NULL COMMENT '店铺ID(平台券为空)',
    name         VARCHAR(128) NOT NULL COMMENT '券名称',
    type         TINYINT      NOT NULL COMMENT '类型 1满减 2折扣 3无门槛',
    discount     DECIMAL(12,2) NOT NULL COMMENT '面额/折扣',
    threshold    DECIMAL(12,2) DEFAULT 0.00 COMMENT '使用门槛',
    total        INT          DEFAULT 0 COMMENT '发行总量',
    issued       INT          DEFAULT 0 COMMENT '已领取数量',
    valid_start  DATETIME     DEFAULT NULL COMMENT '生效时间',
    valid_end    DATETIME     DEFAULT NULL COMMENT '失效时间',
    status       TINYINT      DEFAULT 1 COMMENT '状态 0下架 1上架',
    deleted      TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE t_user_coupon (
    id           BIGINT   NOT NULL COMMENT '主键',
    coupon_id    BIGINT   NOT NULL COMMENT '优惠券ID',
    user_id      BIGINT   NOT NULL COMMENT '用户ID',
    status       TINYINT  DEFAULT 0 COMMENT '状态 0未使用 1已锁定 2已使用 3已过期',
    used_order_id BIGINT  DEFAULT NULL COMMENT '使用订单ID',
    deleted      TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_coupon_id (coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 角色表
CREATE TABLE t_role (
    id          BIGINT      NOT NULL COMMENT '主键',
    name        VARCHAR(64) NOT NULL COMMENT '角色名称',
    code        VARCHAR(64) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    deleted     TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE t_user_role (
    id      BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限表
CREATE TABLE t_permission (
    id        BIGINT      NOT NULL COMMENT '主键',
    parent_id BIGINT      DEFAULT 0 COMMENT '父级ID',
    name      VARCHAR(64) NOT NULL COMMENT '权限名称',
    code      VARCHAR(128) NOT NULL COMMENT '权限编码',
    type      TINYINT     DEFAULT 1 COMMENT '类型 1菜单 2按钮 3数据',
    route     VARCHAR(255) DEFAULT NULL COMMENT '前端路由',
    sort      INT         DEFAULT 0 COMMENT '排序',
    status    TINYINT     DEFAULT 1 COMMENT '状态 0禁用 1启用',
    deleted   TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME  DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE t_role_permission (
    id            BIGINT NOT NULL COMMENT '主键',
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 操作日志表
CREATE TABLE t_oper_log (
    id          BIGINT       NOT NULL COMMENT '主键',
    user_id     BIGINT       DEFAULT NULL COMMENT '操作人ID',
    username    VARCHAR(64)  DEFAULT NULL COMMENT '操作人',
    module      VARCHAR(64)  DEFAULT NULL COMMENT '模块',
    action      VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    method      VARCHAR(255) DEFAULT NULL COMMENT '请求方法',
    params      TEXT         DEFAULT NULL COMMENT '请求参数',
    ip          VARCHAR(64)  DEFAULT NULL COMMENT 'IP',
    status      TINYINT      DEFAULT 1 COMMENT '结果 0失败 1成功',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 登录日志表
CREATE TABLE t_login_log (
    id          BIGINT      NOT NULL COMMENT '主键',
    user_id     BIGINT      DEFAULT NULL COMMENT '用户ID',
    username    VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    ip          VARCHAR(64) DEFAULT NULL COMMENT 'IP',
    device      VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
    status      TINYINT     DEFAULT 1 COMMENT '结果 0失败 1成功',
    message     VARCHAR(255) DEFAULT NULL COMMENT '提示信息',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 基础角色数据
INSERT INTO t_role (id, name, code, description) VALUES
    (1, '平台管理员', 'ADMIN', '平台管理端角色'),
    (2, '入驻商家', 'MERCHANT', '商家端角色'),
    (3, '普通用户', 'USER', '前台消费者角色');
