import request from './request';

export function getAddressList() {
  return request.get('/portal/address');
}

export function addAddress(data) {
  return request.post('/portal/address', data);
}

export function updateAddress(id, data) {
  return request.put(`/portal/address/${id}`, data);
}

export function deleteAddress(id) {
  return request.delete(`/portal/address/${id}`);
}

export function setDefaultAddress(id) {
  return request.put(`/portal/address/${id}/default`);
}
