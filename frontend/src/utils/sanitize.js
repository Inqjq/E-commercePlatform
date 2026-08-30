import DOMPurify from 'dompurify';

/**
 * 富文本消毒：商品详情等商家可编辑的 HTML 渲染前必须经过白名单过滤，
 * 防止存储型 XSS（后端侧同样建议在入库前消毒）。
 */
export function sanitizeHtml(html) {
  return DOMPurify.sanitize(html ?? '');
}
