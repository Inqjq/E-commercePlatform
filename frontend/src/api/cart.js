import request from './request';

function normalizeCart(groups) {
  return (groups || []).map((g) => ({
    ...g,
    items: (g.items || []).map((i) => ({
      ...i,
      title: i.title ?? i.goodsTitle,
      image: i.image ?? i.goodsImage,
      spec: i.spec ?? i.specText,
      checked: Number(i.checked) === 1,
    })),
  }));
}

export async function getCart() {
  return normalizeCart(await request.get('/portal/cart'));
}

export function addToCart(data) {
  // 后端 CartAddRequest 只认 skuId + quantity，价格等以后端为准
  return request.post('/portal/cart', { skuId: data.skuId, quantity: data.quantity || 1 });
}

export function updateCart(id, data) {
  if (data.quantity !== undefined) {
    return request.put(`/portal/cart/${id}/quantity`, { quantity: data.quantity });
  }
  if (data.checked !== undefined) {
    return request.put(`/portal/cart/${id}/checked`, { checked: data.checked });
  }
  return Promise.resolve();
}

export function removeCart(id) {
  return request.delete(`/portal/cart/${id}`);
}

/** 后端仅支持单条勾选，批量勾选由前端并发逐条调用。 */
export async function checkCart(data) {
  const ids = data.ids || [];
  await Promise.all(ids.map((id) => request.put(`/portal/cart/${id}/checked`, { checked: data.checked })));
}

export function clearCart() {
  // 后端暂无清空接口，页面仅本地清空
  return Promise.resolve();
}
