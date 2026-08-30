import axios from 'axios';
import { ElMessage } from 'element-plus';
import { getToken } from '@/utils/auth';
import router from '@/router';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 20000,
});

// 后端未就绪时启用本地 mock 适配器（生产构建 VITE_USE_MOCK=false，该分支及 mock 代码会被整体剔除）
if (import.meta.env.VITE_USE_MOCK === 'true') {
  import('@/mock').then(({ mockAdapter }) => {
    service.defaults.adapter = mockAdapter;
  });
}

service.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// 401 统一处理：清空本地登录态与 Pinia 状态，防并发重复跳转
let redirectingToLogin = false;
async function forceLogout() {
  const current = router.currentRoute.value;
  if (current.path === '/login') {
    return;
  }
  try {
    const [{ useUserStore }, { useCartStore }] = await Promise.all([
      import('@/stores/user'),
      import('@/stores/cart'),
    ]);
    useUserStore().clear();
    useCartStore().clear();
  } catch {
    // Pinia 未初始化时仅清 localStorage
  }
  if (redirectingToLogin) {
    return;
  }
  redirectingToLogin = true;
  ElMessage.warning('登录已过期，请重新登录');
  router
    .push({ path: '/login', query: { redirect: current.fullPath } })
    .finally(() => {
      redirectingToLogin = false;
    });
}

service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res && (res.code === 0 || res.code === 200)) {
      return res.data;
    }
    const message = res?.message || '请求失败';
    if (res && res.code === 401) {
      forceLogout();
    } else {
      ElMessage.error(message);
    }
    const error = new Error(message);
    error.code = res?.code;
    return Promise.reject(error);
  },
  (error) => {
    let msg = error?.response?.data?.message || error.message || '网络异常，请稍后重试';
    if (error?.response?.status === 401) {
      forceLogout();
      msg = '登录已过期，请重新登录';
    }
    ElMessage.error(msg);
    return Promise.reject(error);
  }
);

export default service;
