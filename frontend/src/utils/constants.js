// 订单状态
export const ORDER_STATUS = {
  PENDING_PAYMENT: 'PENDING_PAYMENT',
  PENDING_SHIP: 'PENDING_SHIP',
  SHIPPED: 'SHIPPED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
};

export const ORDER_STATUS_MAP = {
  PENDING_PAYMENT: { label: '待付款', type: 'warning' },
  PENDING_SHIP: { label: '待发货', type: 'primary' },
  SHIPPED: { label: '待收货', type: 'info' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' },
};

export const ORDER_TABS = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING_PAYMENT', label: '待付款' },
  { key: 'PENDING_SHIP', label: '待发货' },
  { key: 'SHIPPED', label: '待收货' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' },
];

// 商品状态
export const GOODS_STATUS = {
  DRAFT: 'DRAFT',
  PENDING: 'PENDING',
  ON_SALE: 'ON_SALE',
  OFF_SALE: 'OFF_SALE',
  REJECTED: 'REJECTED',
};

export const GOODS_STATUS_MAP = {
  DRAFT: { label: '草稿', type: 'info' },
  PENDING: { label: '待审核', type: 'warning' },
  ON_SALE: { label: '在售', type: 'success' },
  OFF_SALE: { label: '已下架', type: 'info' },
  REJECTED: { label: '已驳回', type: 'danger' },
};

// 商家审核状态
export const AUDIT_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
};

export const AUDIT_STATUS_MAP = {
  PENDING: { label: '待审核', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' },
};

// 售后状态
export const AFTER_SALE_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  AGREED: 'AGREED',
  REJECTED: 'REJECTED',
  COMPLETED: 'COMPLETED',
};

export const AFTER_SALE_STATUS_MAP = {
  PENDING: { label: '待处理', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'primary' },
  AGREED: { label: '已同意', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  COMPLETED: { label: '已完成', type: 'info' },
};

// 券类型
export const COUPON_TYPE = {
  FULL_REDUCTION: 'FULL_REDUCTION',
  DISCOUNT: 'DISCOUNT',
  CASH: 'CASH',
};

export const COUPON_TYPE_MAP = {
  FULL_REDUCTION: '满减券',
  DISCOUNT: '折扣券',
  CASH: '无门槛券',
};

export const PAY_CHANNEL = {
  ALIPAY: 'ALIPAY',
  WECHAT: 'WECHAT',
};

export const PAY_CHANNEL_MAP = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
};
