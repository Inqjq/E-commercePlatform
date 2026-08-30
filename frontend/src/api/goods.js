import request from './request';

export async function getGoodsList(params = {}) {
  const sortMap = { sales: 'sales', price: 'price_asc', price_desc: 'price_desc' };
  const query = {
    current: params.page || params.current || 1,
    size: params.size || 12,
    keyword: params.keyword || undefined,
    categoryId: params.categoryId || undefined,
    brandId: params.brandId || undefined,
    priceMin: params.minPrice || undefined,
    priceMax: params.maxPrice || undefined,
    sort: sortMap[params.sortBy] || undefined,
  };
  const data = await request.get('/portal/goods/list', { params: query });
  return { ...data, records: data?.records || [] };
}

/** 后端详情为 { goods, skus }，摊平成 mock 时代的单层结构供页面使用。 */
export async function getGoodsDetail(id) {
  const data = await request.get(`/portal/goods/${id}`);
  const goods = data?.goods || {};
  return {
    ...goods,
    // 后端 images 是 JSON 字符串（[{url}] 或 [url]），页面需要的是字符串数组
    images: parseImages(goods.images, goods.mainImage),
    skus: (data?.skus || []).map((s) => ({ ...s, specName: s.specText })),
    reviews: [],
  };
}

function parseImages(raw, mainImage) {
  let list = [];
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw || [];
    list = (Array.isArray(parsed) ? parsed : [])
      .map((it) => (typeof it === 'string' ? it : it?.url))
      .filter(Boolean);
  } catch {
    list = [];
  }
  if (!list.length && mainImage) list = [mainImage];
  return list;
}

export function getCategories() {
  return request.get('/portal/category/tree');
}

export function getBrands() {
  return request.get('/portal/brand/list');
}

export async function getHotGoods() {
  const data = await getGoodsList({ page: 1, size: 8, sortBy: 'sales' });
  return data.records;
}

export async function getNewGoods() {
  const data = await getGoodsList({ page: 1, size: 8 });
  return data.records;
}

/** 搜索联想后端暂未实现。 */
export function getSearchSuggest() {
  return Promise.resolve([]);
}

export async function getGoodsReviews(goodsId) {
  const data = await request.get('/portal/review', { params: { goodsId } });
  return Array.isArray(data) ? data : data?.records || [];
}

export { getGoodsReviews as getReviews };
