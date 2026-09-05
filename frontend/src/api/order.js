import request from './request';

// 后端订单状态码 → 前端状态名（utils/constants 的 ORDER_STATUS_MAP 以字符串为键）
const STATUS_BY_CODE = {
  0: 'PENDING_PAYMENT',
  1: 'PENDING_SHIP',
  2: 'SHIPPED',
  3: 'COMPLETED',
  4: 'CANCELLED',
  5: 'AFTERSALE',
};
const CODE_BY_STATUS = {
  PENDING_PAYMENT: 0,
  PENDING_SHIP: 1,
  SHIPPED: 2,
  COMPLETED: 3,
  CANCELLED: 4,
  AFTERSALE: 5,
};

/** 后端订单 VO → 前端展示结构（状态转字符串、明细与收货/物流字段对齐页面模板）。 */
export function normalizeOrder(order) {
  if (!order) return order;
  return {
    ...order,
    status: typeof order.status === 'number' ? (STATUS_BY_CODE[order.status] ?? order.status) : order.status,
    phone: order.phone ?? order.receiverPhone,
    amount: order.amount ?? order.payAmount,
    freight: order.freight ?? order.freightAmount,
    couponAmount: order.couponAmount ?? order.discountAmount,
    logistics: order.logistics ?? (order.logisticsCompany
      ? { company: order.logisticsCompany, no: order.logisticsNo }
      : undefined),
    address: order.address ?? {
      receiver: order.receiver,
      phone: order.receiverPhone,
      detail: order.receiverAddress,
    },
    items: (order.items || []).map((i) => ({
      ...i,
      title: i.title ?? i.goodsTitle,
      spec: i.spec ?? i.specText,
    })),
  };
}

/**
 * 创建订单。data 契约见后端 OrderCreateRequest：
 * { addressId, fromCart, items?: [{skuId, quantity}], remark, requestId }
 * 后端按店铺拆单，可能返回多笔订单；这里返回首笔（含 orderNos 全量）便于收银台跳转。
 */
export async function createOrder(data) {
  const requestId = data.requestId || (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
    ? crypto.randomUUID()
    : `req-${Date.now()}-${Math.random().toString(16).slice(2)}`);
  const list = await request.post('/portal/orders', { ...data, requestId });
  const orders = Array.isArray(list) ? list : [list];
  return { ...(orders[0] || {}), orderNos: orders.map((o) => o.orderNo) };
}

export async function getOrders(params = {}) {
  const query = { current: params.page || params.current || 1, size: params.size || 20 };
  if (params.status && params.status !== 'ALL') {
    query.status = CODE_BY_STATUS[params.status] ?? params.status;
  }
  const data = await request.get('/portal/orders', { params: query });
  const records = Array.isArray(data) ? data : data?.records || [];
  return records.map(normalizeOrder);
}

export async function getOrderDetail(orderNo) {
  return normalizeOrder(await request.get(`/portal/orders/${orderNo}`));
}

export function cancelOrder(orderNo) {
  return request.post(`/portal/orders/${orderNo}/cancel`);
}

export function confirmOrder(orderNo) {
  return request.post(`/portal/orders/${orderNo}/confirm`);
}

export function payOrder(orderNo, channel) {
  return request.post(`/portal/pay/${orderNo}`, { channel });
}

/**
 * 查询支付状态（后端会对支付宝渠道主动查单并入账）。
 * @returns {{ orderNo: string, paid: boolean }}
 */
export function getPayStatus(orderNo) {
  return request.get(`/portal/pay/${orderNo}/status`);
}
