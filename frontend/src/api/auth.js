import request from './request';
import { ElMessage } from 'element-plus';

export function login(data) {
  return request.post('/portal/auth/login', data);
}

export function register(data) {
  return request.post('/portal/auth/register', data);
}

export function logout() {
  return request.post('/portal/auth/logout');
}

export function sendSms(data) {
  return request.post('/portal/auth/sms-code', data);
}

export function getProfile() {
  return request.get('/portal/user/me');
}

export function updateProfile(data) {
  return request.put('/portal/user/profile', data);
}

export function changePassword(data) {
  return request.put('/portal/user/password', data);
}

// ===== 收藏/消息：后端暂未实现，返回空数据保证页面可用 =====
export function getMessages() {
  return Promise.resolve([]);
}

export function getFavorites() {
  return Promise.resolve([]);
}

export function addFavorite() {
  ElMessage.info('收藏功能即将上线');
  return Promise.resolve(null);
}

export function removeFavorite() {
  return Promise.resolve(null);
}
