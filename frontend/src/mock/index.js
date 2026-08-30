import * as db from './data';

// 可变的运行时状态
const state = {
  carts: db.carts.map((c) => ({ ...c })),
  orders: db.orders.map((o) => ({ ...o, items: o.items.map((i) => ({ ...i })) })),
  addresses: db.addresses.map((a) => ({ ...a })),
  userCoupons: db.userCoupons.map((c) => ({ ...c })),
  merchantGoods: db.merchantGoods.map((g) => ({ ...g, skus: (g.skus || []).map((s) => ({ ...s })) })),
  merchantOrders: db.merchantOrders.map((o) => ({ ...o })),
  adminUsers: db.adminUsers.map((u) => ({ ...u })),
  merchants: db.merchants.map((m) => ({ ...m })),
  roles: db.roles.map((r) => ({ ...r })),
  nextCartId: 100,
  nextOrderId: 100,
};

const ok = (data, message = 'success') => ({ code: 0, message, data });
const fail = (code, message) => ({ code, message, data: null });

function res(data, code = 0, message = 'success', delay = 120) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ data: { code, message, data }, status: 200, statusText: 'OK', headers: {}, config: {} });
    }, delay);
  });
}

function paginate(list, current = 1, size = 10) {
  const p = Math.max(1, Number(current) || 1);
  const s = Math.max(1, Number(size) || 10);
  const start = (p - 1) * s;
  // 对齐后端 PageResult 结构 { current, size, total, records }
  return { current: p, size: s, total: list.length, records: list.slice(start, start + s) };
}

function matchUrl(url, pattern) {
  const keys = [];
  const regex = new RegExp(
    '^' +
      pattern
        .replace(/\//g, '\\/')
        .replace(/:([^/]+)/g, (_, key) => {
          keys.push(key);
          return '([^/]+)';
        }) +
      '$'
  );
  const m = url.match(regex);
  if (!m) return null;
  const params = {};
  keys.forEach((k, i) => {
    params[k] = decodeURIComponent(m[i + 1]);
  });
  return params;
}

const GOODS_STATUS_BY_CODE = { 0: 'DRAFT', 1: 'PENDING', 2: 'ON_SALE', 3: 'OFF_SALE', 4: 'REJECTED' };
const CODE_BY_GOODS_STATUS = { DRAFT: 0, PENDING: 1, ON_SALE: 2, OFF_SALE: 3, REJECTED: 4 };
const ORDER_CODE_BY_STATUS = { PENDING_PAYMENT: 0, PENDING_SHIP: 1, SHIPPED: 2, COMPLETED: 3, CANCELLED: 4 };
const AUDIT_BY_CODE = { 0: 'PENDING', 1: 'APPROVED', 2: 'REJECTED' };
const USER_STATUS_BY_CODE = { 1: 'NORMAL', 0: 'DISABLED' };

export function isMockEnabled() {
  return (import.meta.env.VITE_USE_MOCK || 'false') === 'true';
}

export function mockAdapter(config) {
  const method = (config.method || 'get').toLowerCase();
  let url = config.url || '';
  // 去掉 baseURL 前缀（通常为 /api）
  url = url.replace(/^\/api/, '');
  const query = { ...(config.params || {}), ...extractQuery(url) };
  url = url.split('?')[0];

  // 查询参数与请求体合并传给处理器，贴近真实后端的参数接收方式
  function run(handler, matched) {
    const payload = { ...query, ...(config.data ? parseBody(config.data) : {}) };
    const result = handler(matched, payload);
    if (result && typeof result.then === 'function') return result;
    if (result && result.code !== undefined) return res(result.data, result.code, result.message);
    return res(result);
  }

  const routes = [
    // ===== 认证/用户（/api/portal/auth、/api/portal/user）=====
    ['/portal/auth/login', 'post', () => ok({ token: 'mock-token-' + Date.now(), user: db.currentUser })],
    ['/portal/auth/register', 'post', () => ok({ token: 'mock-token-' + Date.now(), user: db.currentUser })],
    ['/portal/auth/logout', 'post', () => ok(null)],
    ['/portal/auth/sms-code', 'post', () => ok(null)],
    ['/portal/auth/reset-password', 'post', () => ok(null)],
    ['/portal/user/me', 'get', () => ok(db.currentUser)],
    ['/portal/user/profile', 'put', (p) => ok({ ...db.currentUser, ...p })],
    ['/portal/user/password', 'put', () => ok(null)],

    // ===== 商品/类目/品牌/评价（/api/portal）=====
    ['/portal/category/tree', 'get', () => ok(db.categories)],
    ['/portal/brand/list', 'get', () => ok(db.brands)],
    ['/portal/goods/list', 'get', (p) => {
      let list = [...db.goods];
      if (p.keyword) {
        const kw = String(p.keyword).toLowerCase();
        list = list.filter((g) => g.title.toLowerCase().includes(kw));
      }
      if (p.categoryId) {
        const cid = Number(p.categoryId);
        const parent = db.categories.find((c) => c.id === cid);
        const ids = parent ? [cid, ...(parent.children || []).map((c) => c.id)] : [cid];
        list = list.filter((g) => ids.includes(g.categoryId));
      }
      if (p.brandId) list = list.filter((g) => g.brandId === Number(p.brandId));
      if (p.priceMin) list = list.filter((g) => g.price >= Number(p.priceMin));
      if (p.priceMax) list = list.filter((g) => g.price <= Number(p.priceMax));
      if (p.sort === 'sales') list.sort((a, b) => b.sales - a.sales);
      else if (p.sort === 'price_asc') list.sort((a, b) => a.price - b.price);
      else if (p.sort === 'price_desc') list.sort((a, b) => b.price - a.price);
      else list.sort((a, b) => b.id - a.id);
      return ok(paginate(list, p.current, p.size));
    }],
    ['/portal/goods/:id', 'get', (p) => {
      const g = db.goods.find((x) => x.id === Number(p.id));
      return g ? ok({ goods: { ...g, reviews: undefined }, skus: g.skus || [] }) : fail(20001, '商品不存在或已下架');
    }],
    ['/portal/review', 'get', (p) => ok(db.reviews.filter((r) => r.goodsId === Number(p.goodsId)))],
    ['/portal/review', 'post', () => ok(null)],

    // ===== 购物车（/api/portal/cart）=====
    ['/portal/cart', 'get', () => ok(groupCart(state.carts))],
    ['/portal/cart', 'post', (p) => {
      const item = { id: state.nextCartId++, userId: 1, shopId: p.shopId || 1, shopName: p.shopName || '渡风自营旗舰店', skuId: p.skuId, goodsId: p.goodsId, title: p.title, spec: p.spec || '标准版', image: p.image || '', price: p.price, quantity: p.quantity || 1, checked: 1, stock: p.stock || 100 };
      const found = state.carts.find((c) => c.skuId === item.skuId);
      if (found) found.quantity += item.quantity;
      else state.carts.push(item);
      return ok(null, '已加入购物车');
    }],
    ['/portal/cart/:id/quantity', 'put', (p) => {
      const c = state.carts.find((x) => x.id === Number(p.id));
      if (c) c.quantity = Number(p.quantity);
      return ok(null);
    }],
    ['/portal/cart/:id/checked', 'put', (p) => {
      const c = state.carts.find((x) => x.id === Number(p.id));
      if (c) c.checked = p.checked ? 1 : 0;
      return ok(null);
    }],
    ['/portal/cart/:id', 'delete', (p) => {
      state.carts = state.carts.filter((x) => x.id !== Number(p.id));
      return ok(null);
    }],

    // ===== 地址（/api/portal/address）=====
    ['/portal/address', 'get', () => ok(state.addresses)],
    ['/portal/address', 'post', (p) => {
      const id = Math.max(0, ...state.addresses.map((a) => a.id)) + 1;
      state.addresses.push({ id, ...p });
      if (p.isDefault) state.addresses.forEach((a) => { a.isDefault = a.id === id; });
      return ok(null);
    }],
    ['/portal/address/:id', 'put', (p) => {
      const a = state.addresses.find((x) => x.id === Number(p.id));
      if (a) Object.assign(a, p);
      if (p.isDefault) state.addresses.forEach((x) => { x.isDefault = x.id === Number(p.id); });
      return ok(null);
    }],
    ['/portal/address/:id', 'delete', (p) => { state.addresses = state.addresses.filter((x) => x.id !== Number(p.id)); return ok(null); }],
    ['/portal/address/:id/default', 'put', (p) => { state.addresses.forEach((x) => { x.isDefault = x.id === Number(p.id); }); return ok(null); }],

    // ===== 订单（/api/portal/orders）=====
    ['/portal/orders', 'post', (p) => {
      const skuMap = {};
      db.goods.forEach((g) => (g.skus || []).forEach((s) => {
        skuMap[s.id] = { price: s.price, title: g.title, image: g.mainImage, spec: s.specName || s.specText, goodsId: g.id, shopId: g.shopId };
      }));
      const fromCart = p.fromCart !== false;
      const source = fromCart
        ? state.carts.filter((c) => c.checked).map((c) => ({ skuId: c.skuId, quantity: c.quantity }))
        : p.items || [];
      const items = source.map((i) => ({
        skuId: i.skuId,
        quantity: i.quantity,
        price: skuMap[i.skuId]?.price ?? 0,
        title: skuMap[i.skuId]?.title ?? '商品',
        image: skuMap[i.skuId]?.image ?? '',
        spec: skuMap[i.skuId]?.spec ?? '',
      }));
      const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);
      const address = state.addresses.find((a) => a.id === Number(p.addressId)) || state.addresses[0];
      const order = {
        id: state.nextOrderId++,
        orderNo: 'DF' + Date.now(),
        userId: 1,
        shopId: 1,
        shopName: '渡风自营旗舰店',
        status: 0,
        totalAmount: total,
        payAmount: total,
        freightAmount: 0,
        discountAmount: 0,
        items,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        expireTime: new Date(Date.now() + 30 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' '),
        receiver: address?.receiver,
        receiverPhone: address?.phone,
        receiverAddress: address ? `${address.province || ''}${address.city || ''}${address.district || ''} ${address.detail || ''}` : '',
      };
      state.orders.unshift(order);
      if (fromCart) state.carts = state.carts.filter((c) => !c.checked);
      return ok([{ id: order.id, orderNo: order.orderNo, payAmount: total }]);
    }],
    ['/portal/orders', 'get', (p) => {
      let list = [...state.orders].filter((o) => o.userId === 1);
      if (p.status !== undefined && p.status !== null && p.status !== '') list = list.filter((o) => o.status === Number(p.status));
      return ok(paginate(list, p.current, p.size));
    }],
    ['/portal/orders/:orderNo', 'get', (p) => {
      const o = state.orders.find((x) => x.orderNo === p.orderNo);
      return o ? ok(o) : fail(30001, '订单不存在');
    }],
    ['/portal/orders/:orderNo/cancel', 'post', (p) => {
      const o = state.orders.find((x) => x.orderNo === p.orderNo);
      if (o) o.status = 4;
      return ok(null);
    }],
    ['/portal/orders/:orderNo/confirm', 'post', (p) => {
      const o = state.orders.find((x) => x.orderNo === p.orderNo);
      if (o) o.status = 3;
      return ok(null);
    }],
    ['/portal/pay/:orderNo', 'post', (p) => {
      const o = state.orders.find((x) => x.orderNo === p.orderNo);
      if (o) o.status = 1;
      return ok({ orderNo: p.orderNo, payNo: 'PAY' + Date.now(), channel: p.channel }, '支付成功');
    }],
    ['/portal/pay/:orderNo/status', 'get', (p) => {
      const o = state.orders.find((x) => x.orderNo === p.orderNo);
      return ok({ orderNo: p.orderNo, paid: !!o && o.status !== 0 });
    }],

    // ===== 商家端（/api/merchant）=====
    ['/merchant/goods', 'get', (p) => {
      let list = [...state.merchantGoods];
      if (p.keyword) list = list.filter((g) => g.title.toLowerCase().includes(String(p.keyword).toLowerCase()));
      if (p.status !== undefined && p.status !== null && p.status !== '') list = list.filter((g) => g.status === GOODS_STATUS_BY_CODE[Number(p.status)]);
      return ok(paginate(list, p.current, p.size));
    }],
    ['/merchant/goods', 'post', (p) => {
      const id = Math.max(0, ...state.merchantGoods.map((g) => g.id)) + 1;
      state.merchantGoods.unshift({
        id,
        title: p.title,
        subtitle: p.subtitle,
        categoryId: p.categoryId,
        brandId: p.brandId,
        price: Math.min(...(p.skus || [{ price: p.price || 0 }]).map((s) => s.price)),
        mainImage: p.mainImage,
        detail: p.detail,
        status: 'PENDING',
        sales: 0,
        skus: (p.skus || []).map((s, i) => ({ id: id * 10 + i, specName: s.specText, price: s.price, stock: s.stock })),
        createTime: new Date().toISOString(),
      });
      return ok(id);
    }],
    ['/merchant/goods/:id', 'get', (p) => {
      const g = state.merchantGoods.find((x) => x.id === Number(p.id));
      return g ? ok({ goods: g, skus: g.skus || [] }) : fail(20001, '商品不存在');
    }],
    ['/merchant/goods/:id', 'put', (p) => {
      const g = state.merchantGoods.find((x) => x.id === Number(p.id));
      if (g) {
        Object.assign(g, p, { status: 'PENDING' });
        if (p.skus) {
          g.skus = p.skus.map((s, i) => ({ id: s.id ?? (g.skus?.[i]?.id ?? i + 1), specName: s.specText, price: s.price, stock: s.stock }));
        }
      }
      return ok(g ? g.id : null);
    }],
    ['/merchant/goods/:id/status', 'put', (p) => {
      const g = state.merchantGoods.find((x) => x.id === Number(p.id));
      if (g) g.status = GOODS_STATUS_BY_CODE[Number(p.status)] ?? g.status;
      return ok(null);
    }],
    ['/merchant/orders', 'get', (p) => {
      let list = [...state.merchantOrders];
      if (p.status !== undefined && p.status !== null && p.status !== '' && p.status !== 'ALL') list = list.filter((o) => o.status === ORDER_CODE_BY_STATUS[p.status]);
      return ok(paginate(list, p.current, p.size));
    }],
    ['/merchant/orders/:orderNo/ship', 'post', (p) => {
      const o = state.merchantOrders.find((x) => x.orderNo === p.orderNo);
      if (o) {
        o.status = 'SHIPPED';
        o.logistics = { company: p.logisticsCompany, no: p.logisticsNo };
      }
      return ok(null);
    }],

    // ===== 平台端（/api/admin）=====
    ['/admin/dashboard/overview', 'get', () => ok({
      userCount: 1286,
      goodsCount: db.goods.length,
      orderCount: 356,
      merchantCount: state.merchants.length,
      todayOrderCount: 12,
      todaySales: 4568.5,
    })],
    ['/admin/user', 'get', (p) => {
      let list = [...state.adminUsers];
      if (p.keyword) list = list.filter((u) => (u.username || '').includes(p.keyword) || (u.phone || '').includes(p.keyword));
      if (p.status !== undefined && p.status !== null && p.status !== '') list = list.filter((u) => u.status === (USER_STATUS_BY_CODE[Number(p.status)] ?? u.status));
      return ok(paginate(list, p.current, p.size));
    }],
    ['/admin/user/:id/status', 'put', (p) => {
      const u = state.adminUsers.find((x) => x.id === Number(p.id));
      if (u) u.status = USER_STATUS_BY_CODE[Number(p.status)] ?? u.status;
      return ok(null);
    }],
    ['/admin/merchant', 'get', (p) => {
      let list = [...state.merchants];
      if (p.auditStatus !== undefined && p.auditStatus !== null && p.auditStatus !== '') {
        list = list.filter((m) => m.auditStatus === (AUDIT_BY_CODE[Number(p.auditStatus)] ?? m.auditStatus));
      }
      return ok(paginate(list, p.current, p.size));
    }],
    ['/admin/merchant/:id/audit', 'post', (p) => {
      const m = state.merchants.find((x) => x.id === Number(p.id));
      if (m) {
        m.auditStatus = p.approve ? 'APPROVED' : 'REJECTED';
        m.auditTime = new Date().toISOString();
      }
      return ok(null);
    }],
    ['/admin/goods', 'get', (p) => {
      let list = [...db.goods];
      if (p.keyword) list = list.filter((g) => g.title.toLowerCase().includes(String(p.keyword).toLowerCase()));
      if (p.status !== undefined && p.status !== null && p.status !== '') list = list.filter((g) => g.status === GOODS_STATUS_BY_CODE[Number(p.status)]);
      return ok(paginate(list, p.current, p.size));
    }],
    ['/admin/goods/:id/audit', 'post', () => ok(null)],
    ['/admin/role', 'get', () => ok(state.roles)],
    ['/admin/role', 'post', (p) => {
      state.roles.push({ id: Math.max(0, ...state.roles.map((r) => r.id)) + 1, ...p });
      return ok(null);
    }],
    ['/admin/logs', 'get', (p) => ok(paginate(db.operLogs, p.current, p.size))],
  ];

  for (const [pattern, m, handler] of routes) {
    if (method !== m) continue;
    const matched = matchUrl(url, pattern);
    if (matched) return run(handler, matched);
  }

  return res(null, 50001, `Mock 未实现: ${method.toUpperCase()} ${url}`);
}

function extractQuery(url) {
  const query = {};
  const idx = url.indexOf('?');
  if (idx < 0) return query;
  const search = url.slice(idx + 1);
  new URLSearchParams(search).forEach((v, k) => {
    query[k] = v;
  });
  return query;
}

function parseBody(data) {
  if (!data) return {};
  if (typeof data !== 'string') return data;
  try {
    return JSON.parse(data);
  } catch {
    return {};
  }
}

function groupCart(items) {
  const map = new Map();
  items.forEach((item) => {
    if (!map.has(item.shopId)) map.set(item.shopId, { shopId: item.shopId, shopName: item.shopName, items: [] });
    map.get(item.shopId).items.push(item);
  });
  return Array.from(map.values());
}

export default mockAdapter;
