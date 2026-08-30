/**
 * 金额格式化：保留两位小数，返回字符串。
 * @param {number|string} value
 * @returns {string}
 */
export function formatPrice(value) {
  const num = Number(value ?? 0);
  if (Number.isNaN(num)) return '0.00';
  return num.toFixed(2);
}

/**
 * 分转元（若后端以分为单位存储金额，默认按元处理时不缩放）。
 */
export function yuan(value) {
  return formatPrice(value);
}

function pad(n) {
  return String(n).padStart(2, '0');
}

/**
 * 格式化日期时间：yyyy-MM-dd HH:mm:ss
 * @param {string|number|Date} input
 * @returns {string}
 */
export function formatDateTime(input) {
  if (!input) return '-';
  const d = input instanceof Date ? input : new Date(input);
  if (Number.isNaN(d.getTime())) return '-';
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

/**
 * 格式化日期：yyyy-MM-dd
 */
export function formatDate(input) {
  if (!input) return '-';
  const d = input instanceof Date ? input : new Date(input);
  if (Number.isNaN(d.getTime())) return '-';
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/**
 * 手机号脱敏：138****1234
 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '-';
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

/**
 * 大数字转万
 */
export function formatWan(value) {
  const num = Number(value ?? 0);
  if (num >= 10000) return `${(num / 10000).toFixed(1)}万`;
  return String(num);
}
