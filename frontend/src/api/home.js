import { getGoodsList, getCategories } from './goods';

/**
 * 后端暂无 banner/公告配置，首页由类目树 + 热销/最新商品拼装。
 */
export async function getHome() {
  const [hot, fresh, categories] = await Promise.all([
    getGoodsList({ page: 1, size: 8, sortBy: 'sales' }),
    getGoodsList({ page: 1, size: 8 }),
    getCategories(),
  ]);
  return {
    banners: [],
    notices: [],
    categories: categories || [],
    hotGoods: hot.records || [],
    newGoods: fresh.records || [],
  };
}
