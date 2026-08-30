import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import { isLoggedIn, getUser } from '@/utils/auth';

const PortalLayout = () => import('@/layouts/PortalLayout.vue');
const MerchantLayout = () => import('@/layouts/MerchantLayout.vue');
const AdminLayout = () => import('@/layouts/AdminLayout.vue');

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/portal/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/portal/Register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/',
    component: PortalLayout,
    children: [
      { path: '', name: 'Home', component: () => import('@/views/portal/Home.vue'), meta: { title: '首页' } },
      { path: 'goods/list', name: 'GoodsList', component: () => import('@/views/portal/GoodsList.vue'), meta: { title: '商品列表' } },
      { path: 'goods/:id', name: 'GoodsDetail', component: () => import('@/views/portal/GoodsDetail.vue'), meta: { title: '商品详情' } },
      { path: 'search', name: 'Search', component: () => import('@/views/portal/Search.vue'), meta: { title: '搜索' } },
      { path: 'cart', name: 'Cart', component: () => import('@/views/portal/Cart.vue'), meta: { title: '购物车', requiresAuth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/views/portal/Checkout.vue'), meta: { title: '结算', requiresAuth: true } },
      { path: 'pay/:orderNo', name: 'Pay', component: () => import('@/views/portal/Pay.vue'), meta: { title: '收银台', requiresAuth: true } },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/portal/OrderList.vue'), meta: { title: '我的订单', requiresAuth: true } },
      { path: 'orders/:orderNo', name: 'OrderDetail', component: () => import('@/views/portal/OrderDetail.vue'), meta: { title: '订单详情', requiresAuth: true } },
      { path: 'after-sale/:orderNo', name: 'AfterSale', component: () => import('@/views/portal/AfterSale.vue'), meta: { title: '申请售后', requiresAuth: true } },
      { path: 'coupon', name: 'Coupon', component: () => import('@/views/portal/Coupon.vue'), meta: { title: '优惠券', requiresAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } },
      { path: 'profile/address', name: 'Address', component: () => import('@/views/portal/Address.vue'), meta: { title: '收货地址', requiresAuth: true } },
    ],
  },
  {
    path: '/merchant',
    component: MerchantLayout,
    meta: { requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] },
    children: [
      { path: '', name: 'MerchantDashboard', component: () => import('@/views/merchant/Dashboard.vue'), meta: { title: '商家工作台', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
      { path: 'goods', name: 'MerchantGoods', component: () => import('@/views/merchant/GoodsManage.vue'), meta: { title: '商品管理', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
      { path: 'goods/edit/:id?', name: 'MerchantGoodsEdit', component: () => import('@/views/merchant/GoodsEdit.vue'), meta: { title: '编辑商品', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
      { path: 'stock', name: 'MerchantStock', component: () => import('@/views/merchant/StockManage.vue'), meta: { title: '库存管理', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
      { path: 'orders', name: 'MerchantOrders', component: () => import('@/views/merchant/OrderManage.vue'), meta: { title: '订单管理', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
      { path: 'after-sales', name: 'MerchantAfterSale', component: () => import('@/views/merchant/AfterSaleManage.vue'), meta: { title: '售后管理', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
      { path: 'marketing', name: 'MerchantMarketing', component: () => import('@/views/merchant/Marketing.vue'), meta: { title: '营销管理', requiresAuth: true, roles: ['MERCHANT', 'ADMIN'] } },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '数据看板', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'user', name: 'AdminUser', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'merchant', name: 'AdminMerchant', component: () => import('@/views/admin/MerchantManage.vue'), meta: { title: '商家管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'goods', name: 'AdminGoods', component: () => import('@/views/admin/GoodsAudit.vue'), meta: { title: '商品审核', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'category', name: 'AdminCategory', component: () => import('@/views/admin/CategoryManage.vue'), meta: { title: '类目管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'brand', name: 'AdminBrand', component: () => import('@/views/admin/BrandManage.vue'), meta: { title: '品牌管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'role', name: 'AdminRole', component: () => import('@/views/admin/RoleManage.vue'), meta: { title: '权限管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'marketing', name: 'AdminMarketing', component: () => import('@/views/admin/MarketingManage.vue'), meta: { title: '营销管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'content', name: 'AdminContent', component: () => import('@/views/admin/ContentManage.vue'), meta: { title: '内容管理', requiresAuth: true, roles: ['ADMIN'] } },
      { path: 'logs', name: 'AdminLogs', component: () => import('@/views/admin/Logs.vue'), meta: { title: '日志审计', requiresAuth: true, roles: ['ADMIN'] } },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/portal/NotFound.vue'), meta: { title: '404' } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to, from, next) => {
  const baseTitle = import.meta.env.VITE_APP_TITLE || '渡风电商平台';
  document.title = to.meta?.title ? `${to.meta.title} - ${baseTitle}` : baseTitle;

  const authed = isLoggedIn();
  if (to.meta?.requiresAuth && !authed) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }
  if (to.meta?.roles && to.meta.roles.length) {
    const user = getUser();
    const role = user?.role || 'USER';
    if (!to.meta.roles.includes(role)) {
      // 无对应角色一律拦截，接口权限由后端兜底校验
      ElMessage.warning('您没有访问该页面的权限');
      return next('/');
    }
  }
  return next();
});

export default router;
