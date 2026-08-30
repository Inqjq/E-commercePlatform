<template>
  <div class="container" v-loading="loading">
    <div class="pay-card df-card">
      <template v-if="order.orderNo">
        <div class="top">
          <el-icon class="icon" :size="48"><Wallet /></el-icon>
          <div>
            <div class="title">订单提交成功，请尽快完成支付</div>
            <div class="sub">订单号：{{ order.orderNo }}</div>
          </div>
        </div>
        <div class="amount-box">
          <span>应付金额</span>
          <span class="amount">¥{{ formatPrice(order.payAmount) }}</span>
        </div>

        <div class="channels">
          <div v-for="c in channels" :key="c.value" class="channel" :class="{ active: channel === c.value }" @click="channel = c.value">
            <img :src="c.icon" /><span>{{ c.label }}</span>
            <el-icon v-if="channel === c.value" class="check"><CircleCheckFilled /></el-icon>
          </div>
        </div>

        <div class="countdown" v-if="remainSeconds > 0">剩余支付时间 <b>{{ minute }}:{{ second }}</b></div>
        <div class="countdown expired" v-else>订单已超时，请重新下单</div>

        <el-button type="danger" size="large" class="pay-btn" :loading="paying" :disabled="remainSeconds <= 0" @click="handlePay">立即支付</el-button>
        <div class="links">
          <a @click="$router.push('/orders')">稍后支付</a>
          <a v-if="order.status !== 'PENDING_PAYMENT'" @click="$router.push({ name: 'OrderDetail', params: { orderNo: order.orderNo } })">查看订单</a>
        </div>
      </template>
      <el-empty v-else description="订单不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Wallet, CircleCheckFilled } from '@element-plus/icons-vue';
import { getOrderDetail, payOrder, getPayStatus } from '@/api/order';
import { formatPrice } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const paying = ref(false);
const order = ref({});
const channel = ref('alipay');
const remainSeconds = ref(0);
let timer = null;
let pollTimer = null;
const channels = [
  { value: 'alipay', label: '支付宝', icon: 'data:image/svg+xml;utf8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 48 48%22%3E%3Ccircle cx=%2224%22 cy=%2224%22 r=%2224%22 fill=%22%231677ff%22/%3E%3Ctext x=%2224%22 y=%2232%22 font-size=%2224%22 fill=%22%23fff%22 text-anchor=%22middle%22%3E%E6%94%AF%3C/text%3E%3C/svg%3E' },
  { value: 'wechat', label: '微信支付', icon: 'data:image/svg+xml;utf8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 48 48%22%3E%3Crect width=%2248%22 height=%2248%22 rx=%2210%22 fill=%22%2309bb07%22/%3E%3Ctext x=%2224%22 y=%2232%22 font-size=%2220%22 fill=%22%23fff%22 text-anchor=%22middle%22%3E%E5%BE%AE%3C/text%3E%3C/svg%3E' },
  { value: 'balance', label: '余额支付', icon: 'data:image/svg+xml;utf8,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 48 48%22%3E%3Ccircle cx=%2224%22 cy=%2224%22 r=%2224%22 fill=%22%23f59e0b%22/%3E%3Ctext x=%2224%22 y=%2232%22 font-size=%2220%22 fill=%22%23fff%22 text-anchor=%22middle%22%3E%E4%BD%99%3C/text%3E%3C/svg%3E' },
];

const minute = computed(() => String(Math.floor(remainSeconds.value / 60)).padStart(2, '0'));
const second = computed(() => String(remainSeconds.value % 60).padStart(2, '0'));

onMounted(async () => {
  loading.value = true;
  try {
    order.value = await getOrderDetail(route.params.orderNo);
  } finally {
    loading.value = false;
  }
  startCountdown();
  // 支付宝沙箱/生产从收银台返回后由轮询完成入账确认（后端主动查单）
  if (order.value?.status === 'PENDING_PAYMENT') startPolling();
});

onUnmounted(() => {
  timer && clearInterval(timer);
  pollTimer && clearInterval(pollTimer);
});

/** 以后端返回的订单过期时间为基准倒计时，刷新页面不再重置 */
function startCountdown() {
  const expireTime = order.value?.expireTime;
  const expire = expireTime ? new Date(String(expireTime).replace(' ', 'T')) : null;
  remainSeconds.value = expire && !Number.isNaN(expire.getTime())
    ? Math.max(0, Math.floor((expire.getTime() - Date.now()) / 1000))
    : 0;
  timer = setInterval(() => {
    if (remainSeconds.value > 0) remainSeconds.value -= 1;
    else clearInterval(timer);
  }, 1000);
}

async function handlePay() {
  paying.value = true;
  try {
    const res = await payOrder(order.value.orderNo, channel.value);
    if (res.payUrl) {
      // 真实支付宝：跳转沙箱/生产收银台，支付结果由轮询与异步通知确认
      ElMessage.success('正在跳转支付宝收银台...');
      setTimeout(() => {
        window.location.href = res.payUrl;
      }, 600);
      return;
    }
    ElMessage.success('支付成功');
    setTimeout(() => router.replace(`/orders/${order.value.orderNo}`), 800);
  } finally {
    paying.value = false;
  }
}

/** 轮询支付状态（后端会主动向支付宝查单并入账），已支付则跳转订单详情 */
function startPolling() {
  pollTimer = setInterval(async () => {
    if (document.hidden) return;
    try {
      const res = await getPayStatus(order.value.orderNo);
      if (res.paid) {
        clearInterval(pollTimer);
        pollTimer = null;
        clearInterval(timer);
        ElMessage.success('支付成功');
        setTimeout(() => router.replace(`/orders/${order.value.orderNo}`), 800);
      }
    } catch {
      // 查单异常（交易暂不存在等）继续下一轮
    }
  }, 4000);
}
</script>

<style scoped>
.container { max-width: 800px; margin: 0 auto; padding: 40px 20px; }
.pay-card { padding: 40px; text-align: center; }
.top { display: flex; align-items: center; gap: 16px; justify-content: center; }
.top .icon { color: var(--df-success); }
.top .title { font-size: 18px; font-weight: 600; }
.top .sub { font-size: 13px; color: var(--df-text-secondary); margin-top: 4px; }
.amount-box { background: #f5f7fa; padding: 20px; border-radius: 8px; margin: 24px 0; }
.amount-box span { color: var(--df-text-secondary); margin-right: 12px; }
.amount { color: var(--df-danger); font-size: 36px; font-weight: 700; }
.channels { display: flex; justify-content: center; gap: 16px; margin-bottom: 24px; }
.channel { display: flex; align-items: center; gap: 8px; padding: 14px 24px; border: 2px solid #e4e7ed; border-radius: 8px; cursor: pointer; position: relative; }
.channel.active { border-color: var(--df-primary); }
.channel img { width: 28px; height: 28px; }
.check { position: absolute; top: -10px; right: -8px; color: var(--df-primary); background: #fff; border-radius: 50%; }
.countdown { color: var(--df-text-secondary); margin-bottom: 20px; }
.countdown b { color: var(--df-warning); }
.countdown.expired { color: var(--df-danger); }
.pay-btn { width: 320px; }
.links { display: flex; justify-content: center; gap: 24px; margin-top: 16px; }
.links a { color: var(--df-text-secondary); cursor: pointer; }
.links a:hover { color: var(--df-primary); }
</style>
