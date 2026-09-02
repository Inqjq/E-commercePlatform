import { getGoodsList, getCategories } from './goods';

/**
 * 后端暂无 banner/公告配置，首页使用本地静态运营位，保证离线可用。
 * 后续后端若提供 /api/portal/home 等配置接口，可在此处优先读取后端数据。
 */
const DEFAULT_BANNERS = [
  { id: 1, title: '新品首发 · 华为 Mate 60 Pro', image: '/img/banner-1.svg', link: '/goods/list?keyword=%E5%8D%8E%E4%B8%BA' },
  { id: 2, title: '超级品牌日 · 爆款直降', image: '/img/banner-2.svg', link: '/goods/list?sortBy=sales' },
  { id: 3, title: '年中大促 · 满 99 元包邮', image: '/img/banner-3.svg', link: '/goods/list' },
];

const DEFAULT_NOTICES = [
  { id: 1, title: '「年中大促」跨店满减活动规则公告' },
  { id: 2, title: '关于七日无理由退货服务升级的通知' },
  { id: 3, title: '平台商家入驻审核提速公告' },
];

export async function getHome() {
  // 单个接口失败不影响首页其余区块展示，避免轮播/公告被空数据覆盖
  const [hot, fresh, categories] = await Promise.all([
    getGoodsList({ page: 1, size: 8, sortBy: 'sales' }).catch(() => ({ records: [] })),
    getGoodsList({ page: 1, size: 8 }).catch(() => ({ records: [] })),
    getCategories().catch(() => []),
  ]);
  return {
    banners: DEFAULT_BANNERS,
    notices: DEFAULT_NOTICES,
    categories: categories || [],
    hotGoods: hot?.records || [],
    newGoods: fresh?.records || [],
  };
}
