import request from './request';
import { normalizeOrder } from './order';

// 后端商品状态码 ↔ 前端状态名
const GOODS_STATUS_BY_CODE = { 0: 'DRAFT', 1: 'PENDING', 2: 'ON_SALE', 3: 'OFF_SALE', 4: 'REJECTED' };
const CODE_BY_GOODS_STATUS = { DRAFT: 0, PENDING: 1, ON_SALE: 2, OFF_SALE: 3, REJECTED: 4 };
const ORDER_CODE_BY_STATUS = { PENDING_PAYMENT: 0, PENDING_SHIP: 1, SHIPPED: 2, COMPLETED: 3, CANCELLED: 4, AFTERSALE: 5 };

function normalizeGoods(g) {
  return g ? { ...g, status: typeof g.status === 'number' ? (GOODS_STATUS_BY_CODE[g.status] ?? g.status) : g.status } : g;
}

/** 工作台指标：总商品数/待发货数来自真实接口，其余暂无统计接口。 */
export async function getMerchantDashboard() {
  const [goodsPage, waitShip] = await Promise.all([
    getMerchantGoods({ page: 1, size: 1 }),
    getMerchantOrders({ page: 1, size: 1, status: 'PENDING_SHIP' }),
  ]);
  return {
    todayOrder: null,
    todaySales: null,
    totalGoods: goodsPage?.total ?? 0,
    pendingShip: waitShip?.total ?? 0,
    stockWarning: 0,
  };
}

export async function getMerchantGoods(params = {}) {
  const query = {
    current: params.page || 1,
    size: params.size || 10,
    keyword: params.keyword || undefined,
    status: CODE_BY_GOODS_STATUS[params.status] ?? undefined,
  };
  const data = await request.get('/merchant/goods', { params: query });
  return { ...data, records: (data?.records || []).map(normalizeGoods) };
}

export function getMerchantGoodsDetail(id) {
  return request.get(`/merchant/goods/${id}`);
}

export function createMerchantGoods(data) {
  return request.post('/merchant/goods', data);
}

export function updateMerchantGoods(id, data) {
  return request.put(`/merchant/goods/${id}`, data);
}

export function updateMerchantGoodsStatus(id, status) {
  const target = CODE_BY_GOODS_STATUS[status] ?? status;
  return request.put(`/merchant/goods/${id}/status`, null, { params: { status: target } });
}

export async function getMerchantOrders(params = {}) {
  const query = { current: params.page || 1, size: params.size || 20 };
  if (params.status && params.status !== 'ALL') {
    query.status = ORDER_CODE_BY_STATUS[params.status] ?? params.status;
  }
  const data = await request.get('/merchant/orders', { params: query });
  const records = Array.isArray(data) ? data : data?.records || [];
  return records.map(normalizeOrder);
}

/** 按订单号发货（后端契约：POST /merchant/orders/{orderNo}/ship）。 */
export function shipOrder(orderNo, data) {
  return request.post(`/merchant/orders/${orderNo}/ship`, {
    logisticsCompany: data.company ?? data.logisticsCompany,
    logisticsNo: data.no ?? data.logisticsNo,
  });
}

// ===== 售后/营销/库存预警：后端暂未实现 =====
export function getAfterSales() {
  return Promise.resolve([]);
}

export function auditAfterSale() {
  return Promise.resolve(null);
}

export function getMarketing() {
  return Promise.resolve([]);
}

export function getStockWarnings() {
  return Promise.resolve([]);
}
