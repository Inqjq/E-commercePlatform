// 优惠券后端模块暂未实现（表结构已就绪），先返回空数据保证页面可用
export function getAvailableCoupons() {
  return Promise.resolve([]);
}

export function receiveCoupon() {
  return Promise.resolve(null);
}

export function getMyCoupons() {
  return Promise.resolve([]);
}
