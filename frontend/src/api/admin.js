import request from './request';
import { ElMessage } from 'element-plus';
import { normalizeGoods } from './merchant';

const GOODS_STATUS_BY_CODE = { 0: 'DRAFT', 1: 'PENDING', 2: 'ON_SALE', 3: 'OFF_SALE', 4: 'REJECTED' };
const CODE_BY_GOODS_STATUS = { DRAFT: 0, PENDING: 1, ON_SALE: 2, OFF_SALE: 3, REJECTED: 4 };
const AUDIT_BY_CODE = { 0: 'PENDING', 1: 'APPROVED', 2: 'REJECTED' };
const CODE_BY_AUDIT = { PENDING: 0, APPROVED: 1, REJECTED: 2 };
const USER_STATUS_BY_CODE = { 1: 'NORMAL', 0: 'DISABLED' };
const CODE_BY_USER_STATUS = { NORMAL: 1, DISABLED: 0, FROZEN: undefined };

export async function getDashboardOverview() {
  const [overview, pendingGoods, pendingMerchant] = await Promise.all([
    request.get('/admin/dashboard/overview'),
    getAdminGoods({ page: 1, size: 1, status: 'PENDING' }),
    getMerchants({ page: 1, size: 1, auditStatus: 'PENDING' }),
  ]);
  return {
    todayOrder: overview?.todayOrderCount ?? 0,
    todaySales: overview?.todaySales ?? 0,
    totalUser: overview?.userCount ?? 0,
    totalMerchant: overview?.merchantCount ?? 0,
    totalGoods: overview?.goodsCount ?? 0,
    conversionRate: '-',
    salesTrend: [],
    categorySales: [],
    pendingGoodsAudit: pendingGoods?.total ?? 0,
    pendingMerchantAudit: pendingMerchant?.total ?? 0,
  };
}

export async function getAdminUsers(params = {}) {
  const query = {
    current: params.page || 1,
    size: params.size || 10,
    keyword: params.keyword || undefined,
    status: CODE_BY_USER_STATUS[params.status],
  };
  const data = await request.get('/admin/user', { params: query });
  return {
    ...data,
    records: (data?.records || []).map((u) => ({
      ...u,
      status: typeof u.status === 'number' ? (USER_STATUS_BY_CODE[u.status] ?? u.status) : u.status,
    })),
  };
}

/** 后端为查询参数接收 status。 */
export function updateUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, null, { params: { status: CODE_BY_USER_STATUS[status] ?? status } });
}

export async function getMerchants(params = {}) {
  const query = {
    current: params.page || 1,
    size: params.size || 10,
    keyword: params.keyword || undefined,
    auditStatus: CODE_BY_AUDIT[params.auditStatus] ?? undefined,
  };
  const data = await request.get('/admin/merchant', { params: query });
  return {
    ...data,
    records: (data?.records || []).map((m) => ({
      ...m,
      contact: m.contact ?? m.contactPhone,
      auditStatus: typeof m.auditStatus === 'number' ? (AUDIT_BY_CODE[m.auditStatus] ?? m.auditStatus) : m.auditStatus,
    })),
  };
}

export function auditMerchant(data) {
  return request.post(`/admin/merchant/${data.id}/audit`, {
    approve: data.auditStatus === 'APPROVED' || data.approve === true,
    reason: data.reason,
  });
}

export async function getAdminGoods(params = {}) {
  const query = {
    current: params.page || 1,
    size: params.size || 10,
    keyword: params.keyword || undefined,
    status: CODE_BY_GOODS_STATUS[params.status] ?? undefined,
  };
  const data = await request.get('/admin/goods', { params: query });
  return {
    ...data,
    records: (data?.records || []).map(normalizeGoods),
  };
}

/** 后端契约：POST /admin/goods/{id}/audit { approve }。 */
export function auditGoods(idOrData, approve) {
  const id = typeof idOrData === 'object' ? idOrData.id : idOrData;
  const isApprove = typeof idOrData === 'object'
    ? idOrData.approve ?? idOrData.auditStatus === 'APPROVED'
    : approve === 'APPROVED' || approve === true;
  return request.post(`/admin/goods/${id}/audit`, { approve: isApprove });
}

export function forceOfflineGoods(id) {
  return request.post(`/admin/goods/${id}/offline`);
}

// ===== 类目/品牌：管理端 CRUD 暂未开放，列表复用门户接口 =====
export function getCategories() {
  return request.get('/portal/category/tree');
}

export function createCategory() {
  ElMessage.info('类目维护功能即将上线');
  return Promise.resolve(null);
}

export function updateCategory() {
  ElMessage.info('类目维护功能即将上线');
  return Promise.resolve(null);
}

export function deleteCategory() {
  ElMessage.info('类目维护功能即将上线');
  return Promise.resolve(null);
}

export function getBrands() {
  return request.get('/portal/brand/list');
}

export function getRoles() {
  return request.get('/admin/role');
}

export function createRole(data) {
  return request.post('/admin/role', data);
}

export function updateRole() {
  ElMessage.info('角色编辑功能即将上线');
  return Promise.resolve(null);
}

export function getPermissions() {
  return Promise.resolve([]);
}

export function getOperLogs(params = {}) {
  return request.get('/admin/logs', {
    params: { current: params.page || 1, size: params.size || 20 },
  });
}

export function getLoginLogs() {
  return Promise.resolve({ records: [], total: 0 });
}

export function getContent() {
  return Promise.resolve({ banners: [], notices: [] });
}

export function getMarketing() {
  return Promise.resolve({ records: [], total: 0 });
}
