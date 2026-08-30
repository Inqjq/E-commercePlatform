/**
 * 模拟数据源：CDN 图片离线不可用，因此使用内联 SVG data URI 作为占位图。
 */
const img = (text, bg = '#e8f0ff', fg = '#3d7eff') =>
  `data:image/svg+xml;utf8,${encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' width='400' height='400'><rect width='400' height='400' fill='${bg}'/><text x='50%' y='50%' font-size='30' fill='${fg}' text-anchor='middle' dominant-baseline='middle'>${text}</text></svg>`
  )}`;

export const currentUser = {
  id: 1,
  username: 'dufeng',
  nickname: '渡风用户',
  phone: '13800001234',
  email: 'user@dufeng.com',
  avatar: img('渡', '#3d7eff', '#fff'),
  gender: 1,
  intro: '欢迎来到渡风电商平台',
  role: 'USER',
  balance: 128.0,
  score: 520,
};

export const categories = [
  { id: 1, name: '手机数码', children: [{ id: 11, name: '手机' }, { id: 12, name: '笔记本' }, { id: 13, name: '平板' }] },
  { id: 2, name: '家用电器', children: [{ id: 21, name: '电视' }, { id: 22, name: '冰箱' }, { id: 23, name: '洗衣机' }] },
  { id: 3, name: '服饰鞋包', children: [{ id: 31, name: '男装' }, { id: 32, name: '女装' }, { id: 33, name: '鞋靴' }] },
  { id: 4, name: '美妆护肤', children: [{ id: 41, name: '面部护肤' }, { id: 42, name: '彩妆' }] },
  { id: 5, name: '食品生鲜', children: [{ id: 51, name: '休闲零食' }, { id: 52, name: '水果' }] },
];

export const brands = [
  { id: 1, name: '苹果', logo: img('A') },
  { id: 2, name: '华为', logo: img('HW') },
  { id: 3, name: '小米', logo: img('MI') },
  { id: 4, name: '美的', logo: img('MD') },
  { id: 5, name: '格力', logo: img('G') },
  { id: 6, name: '优衣库', logo: img('U') },
];

const seed = [
  [1, 'Apple iPhone 15 Pro 256GB 原色钛金属', 11, 1, 7999, 120, 3200],
  [2, '华为 Mate 60 Pro 12GB+512GB', 11, 2, 6999, 80, 2800],
  [3, '小米 14 Ultra 16GB+512GB', 11, 3, 6499, 150, 1900],
  [4, 'Apple MacBook Air M3 13 英寸', 12, 1, 9499, 60, 980],
  [5, '华为 MateBook X Pro 2024', 12, 2, 8999, 40, 760],
  [6, '小米平板 6 Pro 11 英寸', 13, 3, 2499, 200, 2100],
  [7, '美的 55 英寸 4K 智能电视', 21, 4, 2999, 90, 1500],
  [8, '格力 1.5 匹变频空调', 22, 5, 3199, 100, 1750],
  [9, '海尔 505L 十字对开门冰箱', 22, 4, 4599, 50, 830],
  [10, '小米 10KG 滚筒洗衣机', 23, 3, 1999, 130, 1120],
  [11, '优衣库 男装 防风连帽外套', 31, 6, 399, 500, 6800],
  [12, '女装 法式碎花连衣裙', 32, 6, 259, 460, 5200],
  [13, '百丽 女士平底单鞋', 33, 6, 329, 300, 4100],
  [14, '悦木之源 灵芝焕能精华水', 41, 2, 580, 260, 2700],
  [15, '完美日记 哑光唇釉', 42, 3, 89, 800, 9600],
  [16, '三只松鼠 每日坚果礼盒', 51, 3, 129, 600, 8300],
  [17, '智利车厘子 2斤装', 52, 2, 99, 400, 6100],
  [18, '每日鲜奶 250ml*12 盒', 52, 4, 69, 350, 4400],
  [19, '苹果 15 寸 MacBook Pro M3 Max', 12, 1, 18999, 25, 430],
  [20, '华为 FreeBuds Pro 3 耳机', 11, 2, 1499, 300, 2500],
];

export const goods = seed.map((g) => {
  const [id, title, categoryId, brandId, price, stock, sales] = g;
  const skus = Array.from({ length: 4 }, (_, i) => ({
    id: id * 10 + i,
    goodsId: id,
    specName: i === 0 ? '标准版' : `规格${i + 1}`,
    specJson: { 颜色: i === 0 ? '经典色' : `颜色${i + 1}`, 版本: i === 0 ? '标准版' : `版本${i + 1}` },
    price: price + i * 100,
    stock: Math.max(0, stock - i * 10),
    code: `SKU${id}${i}`,
  }));
  return {
    id,
    title,
    subtitle: '正品保障 · 快速发货 · 售后无忧',
    categoryId,
    brandId,
    shopId: g[2] % 2 === 0 ? 1 : 2,
    price,
    marketPrice: Math.round(price * 1.25),
    stock,
    sales,
    mainImage: img(title.slice(0, 2), '#e8f0ff', '#3d7eff'),
    images: [img(title.slice(0, 2)), img('细节图'), img('实拍图')],
    detail: '<p>精选品质商品，支持七天无理由退换，满 99 元包邮。</p><p>本文为需求演示用描述内容，实际商品描述可在商家端自助编辑。</p>',
    service: ['正品保障', '七天无理由', '运费险', '极速退款'],
    status: 'ON_SALE',
    skus,
    reviews: [],
  };
});

export const banners = [
  { id: 1, title: '新品首发 · iPhone 15 Pro', image: img('新品首发', '#3d7eff', '#fff'), link: '/goods/1' },
  { id: 2, title: '超级品牌日 · 华为专场', image: img('品牌日', '#f56c6c', '#fff'), link: '/goods/list?brandId=2' },
  { id: 3, title: '年中大促 · 全场满减', image: img('年中大促', '#67c23a', '#fff'), link: '/goods/list' },
];

export const notices = [
  { id: 1, title: '「渡风618」大促活动规则公告', content: '活动期间全场跨店满减，详情见活动页。' },
  { id: 2, title: '关于支持七天无理由退货的通知', content: '自公告发布之日起，符合条件的商品支持七天无理由退货。' },
  { id: 3, title: '平台商家入驻审核加速说明', content: '商家入驻审核时效由 3 个工作日缩短至 1 个工作日。' },
];

export const reviews = [
  { id: 1, goodsId: 1, userId: 101, nickname: '极客老王', avatar: img('王'), score: 5, content: '手感很好，拍照一流，物流也快，非常满意！', images: [img('实拍1')], createTime: '2026-08-20 10:20:00' },
  { id: 2, goodsId: 1, userId: 102, nickname: '数码控', avatar: img('数'), score: 4, content: '整体不错，就是价格略贵，希望有优惠活动。', images: [], createTime: '2026-08-19 15:36:00' },
  { id: 3, goodsId: 1, userId: 103, nickname: '匿名用户', avatar: img('匿'), score: 5, content: '正品保障，比实体店优惠。', images: [], createTime: '2026-08-15 09:10:00' },
];

export const addresses = [
  { id: 1, receiver: '张三', phone: '13800001234', province: '广东省', city: '深圳市', district: '南山区', detail: '科技园南路 88 号 3 栋 501', isDefault: true },
  { id: 2, receiver: '李四', phone: '13900005678', province: '上海市', city: '上海市', district: '浦东新区', detail: '张江高科路 100 号', isDefault: false },
];

export const coupons = [
  { id: 1, shopId: 0, name: '平台新人券', type: 'CASH', discount: 10, threshold: 0, validStart: '2026-08-01', validEnd: '2026-12-31', scope: '全场通用', received: 0 },
  { id: 2, shopId: 0, name: '满300减30', type: 'FULL_REDUCTION', discount: 30, threshold: 300, validStart: '2026-08-01', validEnd: '2026-12-31', scope: '全场通用', received: 0 },
  { id: 3, shopId: 1, name: '九折券', type: 'DISCOUNT', discount: 9, threshold: 0, validStart: '2026-08-01', validEnd: '2026-12-31', scope: '限自营商品', received: 0 },
  { id: 4, shopId: 2, name: '满99减15', type: 'FULL_REDUCTION', discount: 15, threshold: 99, validStart: '2026-08-01', validEnd: '2026-12-31', scope: '限入驻店铺', received: 0 },
];

export const userCoupons = [
  { id: 1, couponId: 1, name: '平台新人券', type: 'CASH', discount: 10, threshold: 0, status: 'UNUSED', expireAt: '2026-12-31' },
  { id: 2, couponId: 2, name: '满300减30', type: 'FULL_REDUCTION', discount: 30, threshold: 300, status: 'UNUSED', expireAt: '2026-12-31' },
  { id: 3, couponId: 4, name: '满99减15', type: 'FULL_REDUCTION', discount: 15, threshold: 99, status: 'USED', expireAt: '2026-12-31' },
];

export const shops = [
  { id: 1, merchantId: 1, name: '渡风自营旗舰店', logo: img('自营'), intro: '渡风平台官方自营，正品保障', status: 'OPEN' },
  { id: 2, merchantId: 2, name: '优选生活馆', logo: img('优选'), intro: '入驻商家，品质好物', status: 'OPEN' },
];

export const merchants = [
  { id: 1, name: '渡风自营', licenseNo: '91440300MA5XXXXX1F', legalPerson: '王渡风', contact: '13800001111', auditStatus: 'APPROVED', auditTime: '2026-06-01 10:00:00', status: 'NORMAL', shopId: 1, categoryIds: '1,2,3' },
  { id: 2, name: '优选生活馆', licenseNo: '91310115MA1XXXXX2K', legalPerson: '陈优选', contact: '13900002222', auditStatus: 'APPROVED', auditTime: '2026-06-10 14:00:00', status: 'NORMAL', shopId: 2, categoryIds: '3,4,5' },
  { id: 3, name: '新兴科技贸易', licenseNo: '91330106MA2XXXXX3L', legalPerson: '赵科技', contact: '13700003333', auditStatus: 'PENDING', auditTime: '', status: 'PENDING', shopId: null, categoryIds: '1,2' },
  { id: 4, name: '悦己美妆', licenseNo: '91440101MA5XXXXX4M', legalPerson: '孙美妆', contact: '13600004444', auditStatus: 'REJECTED', auditTime: '2026-08-25 09:00:00', status: 'REJECTED', shopId: null, categoryIds: '4' },
];

export const adminUsers = [
  { id: 1, username: 'dufeng', nickname: '渡风用户', phone: '13800001234', status: 'NORMAL', registerTime: '2026-05-01 10:00:00', orderCount: 6, totalAmount: 168800 },
  { id: 2, username: 'customer2', nickname: '静静', phone: '13500005678', status: 'NORMAL', registerTime: '2026-05-12 09:30:00', orderCount: 2, totalAmount: 599 },
  { id: 3, username: 'customer3', nickname: '阿豪', phone: '13200009876', status: 'FROZEN', registerTime: '2026-06-02 16:20:00', orderCount: 1, totalAmount: 129 },
  { id: 4, username: 'customer4', nickname: '小雅', phone: '13100001111', status: 'DISABLED', registerTime: '2026-07-15 11:00:00', orderCount: 0, totalAmount: 0 },
];

export const roles = [
  { id: 1, name: '平台管理员', code: 'ADMIN', description: '平台超级管理员', type: 'PLATFORM', menus: ['/admin', '/admin/user', '/admin/merchant', '/admin/goods', '/admin/category', '/admin/role', '/admin/system'] },
  { id: 2, name: '平台运营', code: 'OPERATOR', description: '日常运营操作', type: 'PLATFORM', menus: ['/admin', '/admin/goods', '/admin/category', '/admin/marketing', '/admin/content'] },
  { id: 3, name: '商家管理员', code: 'MERCHANT', description: '商家店铺管理员', type: 'MERCHANT', menus: ['/merchant', '/merchant/goods', '/merchant/orders', '/merchant/marketing'] },
];

export const permissions = [
  { id: 1, parentId: 0, name: '平台管理', code: 'admin', type: 'MENU', route: '/admin' },
  { id: 2, parentId: 1, name: '用户管理', code: 'admin:user', type: 'MENU', route: '/admin/user' },
  { id: 3, parentId: 1, name: '商家管理', code: 'admin:merchant', type: 'MENU', route: '/admin/merchant' },
  { id: 4, parentId: 1, name: '商品审核', code: 'admin:goods', type: 'MENU', route: '/admin/goods' },
  { id: 5, parentId: 1, name: '类目管理', code: 'admin:category', type: 'MENU', route: '/admin/category' },
  { id: 6, parentId: 1, name: '角色管理', code: 'admin:role', type: 'MENU', route: '/admin/role' },
  { id: 7, parentId: 2, name: '禁用用户', code: 'admin:user:disable', type: 'BUTTON', route: '' },
];

export const operLogs = [
  { id: 1, operator: 'admin', module: '用户管理', action: '禁用用户', content: '禁用用户 customer3', ip: '127.0.0.1', createTime: '2026-08-29 10:00:00' },
  { id: 2, operator: 'admin', module: '商品管理', action: '审核通过', content: '商品「小米 14 Ultra」审核通过', ip: '127.0.0.1', createTime: '2026-08-28 15:30:00' },
  { id: 3, operator: 'operator', module: '营销管理', action: '发布活动', content: '发布「年中大促」活动', ip: '127.0.0.1', createTime: '2026-08-28 11:00:00' },
];

// 建造若干订单
export const orders = [
  {
    id: 1,
    orderNo: 'DF20260830100001',
    userId: 1,
    shopId: 1,
    shopName: '渡风自营旗舰店',
    status: 'PENDING_PAYMENT',
    totalAmount: 8069.0,
    payAmount: 8069.0,
    freight: 0,
    couponAmount: 0,
    items: [
      { id: 11, skuId: 10, goodsId: 1, title: 'Apple iPhone 15 Pro 256GB 原色钛金属', spec: '经典色', image: goods[0].mainImage, price: 7999, quantity: 1 },
      { id: 12, skuId: 170, goodsId: 17, title: '智利车厘子 2斤装', spec: '标准版', image: goods[16].mainImage, price: 99, quantity: 1 },
    ],
    createTime: '2026-08-30 10:00:00',
    payExpireTime: '2026-08-30 10:30:00',
    address: addresses[0],
  },
  {
    id: 2,
    orderNo: 'DF20260829100002',
    userId: 1,
    shopId: 1,
    shopName: '渡风自营旗舰店',
    status: 'PENDING_SHIP',
    totalAmount: 2999.0,
    payAmount: 2999.0,
    freight: 0,
    couponAmount: 0,
    items: [
      { id: 21, skuId: 70, goodsId: 7, title: '美的 55 英寸 4K 智能电视', spec: '标准版', image: goods[6].mainImage, price: 2999, quantity: 1 },
    ],
    createTime: '2026-08-29 15:30:00',
    address: addresses[1],
  },
  {
    id: 3,
    orderNo: 'DF20260828000003',
    userId: 1,
    shopId: 2,
    shopName: '优选生活馆',
    status: 'SHIPPED',
    totalAmount: 259.0,
    payAmount: 259.0,
    freight: 0,
    couponAmount: 0,
    items: [
      { id: 31, skuId: 120, goodsId: 12, title: '女装 法式碎花连衣裙', spec: '标准版', image: goods[11].mainImage, price: 259, quantity: 1 },
    ],
    createTime: '2026-08-28 09:00:00',
    logistics: { company: '顺丰速运', no: 'SF123456789', address: '广东省深圳市南山区' },
    address: addresses[0],
  },
  {
    id: 4,
    orderNo: 'DF20260825000004',
    userId: 1,
    shopId: 1,
    shopName: '渡风自营旗舰店',
    status: 'COMPLETED',
    totalAmount: 6499.0,
    payAmount: 6499.0,
    freight: 0,
    couponAmount: 0,
    items: [
      { id: 41, skuId: 30, goodsId: 3, title: '小米 14 Ultra 16GB+512GB', spec: '标准版', image: goods[2].mainImage, price: 6499, quantity: 1 },
    ],
    createTime: '2026-08-20 11:00:00',
    address: addresses[0],
  },
  {
    id: 5,
    orderNo: 'DF20260826000005',
    userId: 1,
    shopId: 1,
    shopName: '渡风自营旗舰店',
    status: 'CANCELLED',
    totalAmount: 129.0,
    payAmount: 0,
    freight: 0,
    couponAmount: 0,
    items: [
      { id: 51, skuId: 160, goodsId: 16, title: '三只松鼠 每日坚果礼盒', spec: '标准版', image: goods[15].mainImage, price: 129, quantity: 1 },
    ],
    createTime: '2026-08-26 09:00:00',
    address: addresses[0],
  },
];

// 购物车
export const carts = [
  { id: 1, userId: 1, shopId: 1, shopName: '渡风自营旗舰店', skuId: 10, goodsId: 1, title: 'Apple iPhone 15 Pro 256GB 原色钛金属', spec: '经典色', image: goods[0].mainImage, price: 7999, quantity: 1, checked: true, stock: 100 },
  { id: 2, userId: 1, shopId: 1, shopName: '渡风自营旗舰店', skuId: 30, goodsId: 3, title: '小米 14 Ultra 16GB+512GB', spec: '标准版', image: goods[2].mainImage, price: 6499, quantity: 1, checked: false, stock: 150 },
  { id: 3, userId: 1, shopId: 2, shopName: '优选生活馆', skuId: 120, goodsId: 12, title: '女装 法式碎花连衣裙', spec: '标准版', image: goods[11].mainImage, price: 259, quantity: 2, checked: true, stock: 460 },
  { id: 4, userId: 1, shopId: 2, shopName: '优选生活馆', skuId: 170, goodsId: 17, title: '智利车厘子 2斤装', spec: '标准版', image: goods[16].mainImage, price: 99, quantity: 3, checked: false, stock: 400 },
];

// 售后单
export const afterSales = [
  { id: 1, orderId: 3, orderNo: 'DF20260828000003', userId: 1, goodsTitle: '女装 法式碎花连衣裙', type: 'REFUND', reason: '尺码不合适', price: 259, status: 'PENDING', createTime: '2026-08-30 12:00:00' },
  { id: 2, orderId: 4, orderNo: 'DF20260825000004', userId: 1, goodsTitle: '小米 14 Ultra', type: 'REFUND', reason: '不喜欢', price: 6499, status: 'AGREED', createTime: '2026-08-25 14:00:00' },
];

// 店铺商品（商家视角，含草稿、待审核等状态）
export const merchantGoods = [
  { id: 1, title: 'Apple iPhone 15 Pro 256GB 原色钛金属', categoryId: 11, brandId: 1, price: 7999, stock: 120, sales: 3200, status: 'ON_SALE', createTime: '2026-06-10 10:00:00' },
  { id: 2, title: '华为 Mate 60 Pro 12GB+512GB', categoryId: 11, brandId: 2, price: 6999, stock: 80, sales: 2800, status: 'ON_SALE', createTime: '2026-06-12 11:00:00' },
  { id: 3, title: '小米 14 Ultra 16GB+512GB', categoryId: 11, brandId: 3, price: 6499, stock: 150, sales: 1900, status: 'ON_SALE', createTime: '2026-06-15 09:00:00' },
  { id: 4, title: '美的 55 英寸 4K 智能电视', categoryId: 21, brandId: 4, price: 2999, stock: 90, sales: 1500, status: 'OFF_SALE', createTime: '2026-06-20 10:00:00' },
  { id: 5, title: '海尔 505L 十字对开门冰箱', categoryId: 22, brandId: 4, price: 4599, stock: 50, sales: 830, status: 'PENDING', createTime: '2026-08-28 10:00:00' },
  { id: 6, title: '格力 1.5 匹变频空调', categoryId: 22, brandId: 5, price: 3199, stock: 100, sales: 1750, status: 'REJECTED', createTime: '2026-08-27 10:00:00' },
];

// 商家订单
export const merchantOrders = [
  { id: 1, orderNo: 'DF20260830100001', status: 'PENDING_PAYMENT', amount: 7999, receiver: '张三', phone: '13800001234', createTime: '2026-08-30 10:00:00' },
  { id: 2, orderNo: 'DF20260829100002', status: 'PENDING_SHIP', amount: 2999, receiver: '李四', phone: '13900005678', createTime: '2026-08-29 15:30:00' },
  { id: 3, orderNo: 'DF20260828000003', status: 'SHIPPED', amount: 259, receiver: '张三', phone: '13800001234', createTime: '2026-08-28 09:00:00' },
];

export const dashboardOverview = {
  todayOrder: 128,
  todaySales: 98560.0,
  totalUser: 2360,
  totalMerchant: 86,
  totalGoods: 1230,
  pendingGoodsAudit: 12,
  pendingMerchantAudit: 3,
  conversionRate: 3.2,
  orderTrend: [12, 20, 15, 30, 42, 38, 55, 48, 62, 70, 58, 72],
  salesTrend: [12000, 18000, 15000, 26000, 31000, 28000, 42000, 38000, 50000, 56000, 48000, 62000],
  categorySales: [
    { name: '手机数码', value: 32000 },
    { name: '家用电器', value: 22000 },
    { name: '服饰鞋包', value: 18000 },
    { name: '美妆护肤', value: 12000 },
    { name: '食品生鲜', value: 9000 },
  ],
};

export const activityList = [
  { id: 1, name: '年中大促', type: 'COUPON', startTime: '2026-08-01', endTime: '2026-08-31', status: 'ON', goodsCount: 120 },
  { id: 2, name: '限时秒杀', type: 'FLASH', startTime: '2026-08-30', endTime: '2026-08-30', status: 'ON', goodsCount: 16 },
  { id: 3, name: '店铺满减满赠', type: 'PMT', startTime: '2026-09-01', endTime: '2026-09-30', status: 'OFF', goodsCount: 0 },
];

// 操作日志、登录日志
export const loginLogs = [
  { id: 1, username: 'dufeng', ip: '127.0.0.1', browser: 'Chrome 124', os: 'Windows 11', status: 'SUCCESS', loginTime: '2026-08-30 09:00:00' },
  { id: 2, username: 'customer2', ip: '192.168.1.10', browser: 'Edge 124', os: 'Windows 10', status: 'SUCCESS', loginTime: '2026-08-30 09:10:00' },
  { id: 3, username: 'ghost', ip: '10.0.0.5', browser: 'Firefox 125', os: 'MacOS', status: 'FAIL', loginTime: '2026-08-30 09:20:00' },
];

export const stockWarnings = [
  { id: 1, goodsId: 4, title: 'Apple MacBook Air M3', sku: '标准版', stock: 5, threshold: 20 },
  { id: 2, goodsId: 8, title: '海尔 505L 冰箱', sku: '标准版', stock: 10, threshold: 30 },
];

export const favorites = [
  { id: 1, goodsId: 1, title: 'Apple iPhone 15 Pro', price: 7999, image: goods[0].mainImage, shopName: '渡风自营旗舰店' },
  { id: 2, goodsId: 2, title: '华为 Mate 60 Pro', price: 6999, image: goods[1].mainImage, shopName: '渡风自营旗舰店' },
];

export const myMessages = [
  { id: 1, type: 'ORDER', title: '订单支付成功', content: '您的订单 DF20260830100001 已支付成功，商家将尽快发货。', read: false, createTime: '2026-08-30 10:01:00' },
  { id: 2, type: 'SYS', title: '平台公告', content: '平台将于本周六 02:00-04:00 进行系统维护，请知悉。', read: false, createTime: '2026-08-29 09:00:00' },
  { id: 3, type: 'AFTER', title: '售后进度', content: '您的售后申请已进入商家处理。', read: true, createTime: '2026-08-30 12:05:00' },
];
