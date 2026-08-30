import request from './request';

export function getReviews(params) {
  return request.get('/review', { params });
}

export function createReview(data) {
  return request.post('/review', data);
}
