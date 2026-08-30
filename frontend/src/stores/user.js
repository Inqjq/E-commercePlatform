import { defineStore } from 'pinia';
import { login as apiLogin, register as apiRegister, logout as apiLogout, getProfile } from '@/api/auth';
import { getToken, setToken, getUser, setUser, clearAuth } from '@/utils/auth';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUser(),
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    nickname: (state) => state.userInfo?.nickname || state.userInfo?.username || '未登录',
    avatar: (state) => state.userInfo?.avatar || '',
  },
  actions: {
    async login(payload) {
      const data = await apiLogin(payload);
      this.applyLogin(data);
      return data;
    },
    async register(payload) {
      const data = await apiRegister(payload);
      this.applyLogin(data);
      return data;
    },
    /** 兼容后端 LoginResponse（无 user 嵌套）与 mock 的 { token, user } 结构。 */
    applyLogin(data) {
      const user = data.user || {
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar,
        roles: data.roles,
      };
      this.token = data.token;
      this.userInfo = user;
      setToken(data.token);
      setUser(user);
    },
    async logout() {
      try {
        await apiLogout();
      } catch {
        // 忽略登出接口异常
      }
      this.clear();
    },
    async fetchProfile() {
      const data = await getProfile();
      this.userInfo = data;
      setUser(data);
      return data;
    },
    updateProfile(user) {
      this.userInfo = { ...this.userInfo, ...user };
      setUser(this.userInfo);
    },
    clear() {
      this.token = '';
      this.userInfo = null;
      clearAuth();
    },
  },
});
