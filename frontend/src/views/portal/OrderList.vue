<template>
  <div class="container">
    <h2 class="page-title">我的订单</h2>
    <div class="order-tabs">
      <span v-for="t in tabs" :key="t.key" :class="{ active: status === t.key }" @click="switchTab(t.key)">{{ t.label }}</span>
    </div>
    <div v-loading="loading" class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-card df-card">
        <div class="order-head df-flex-between">
          <div class="left df-flex">
            <span class="shop" @click="$router.push('/goods/list')">{{ o.shopName }}</span>
            <span class="time">{{ o.createTime }}</span>
          </div>
          <el-tag :type="ORDER_STATUS_MAP[o.status]?.type">{{ ORDER_STATUS_MAP[o.status]?.label }}</el-tag>
        </div>
        <div class="order-body" @click="$router.push(`/orders/${o.orderNo}`)">
          <div v-for="item in o.items.slice(0, 2)" :key="item.id" class="item">
            <img :src="item.image" />
            <div class="desc">
              <div class="title">{{ item.title }}</div>
              <div class="spec">{{ item.spec }}</div>
            </div>
          </div>
          <div class="amount-col">
            <div class="total">¥{{ formatPrice(o.totalAmount) }}</div>
            <div class="count">共 {{ o.items.reduce((s, i) => s + i.quantity, 0) }} 件</div>
          </div>
        </div>
        <div class="order-foot df-flex-between">
          <div class="order-no">订单号：{{ o.orderNo }}</div>
          <div class="actions">
            <el-button v-if="o.status === 'PENDING_PAYMENT'" type="danger" size="small" @click="pay(o)">去支付</el-button>
            <el-button v-if="o.status === 'PENDING_PAYMENT'" size="small" @click="cancel(o)">取消</el-button>
            <el-button v-if="o.status === 'SHIPPED'" type="primary" size="small" @click="confirm(o)">确认收货</el-button>
            <el-button v-if="o.status === 'COMPLETED'" size="small" @click="$router.push(`/after-sale/${o.orderNo}`)">申请售后</el-button>
            <el-button size="small" @click="$router.push(`/orders/${o.orderNo}`)">详情</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !orders.length" description="暂无相关订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrders, cancelOrder, confirmOrder } from '@/api/order';
import { ORDER_STATUS_MAP, ORDER_TABS } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const router = useRouter();
const orders = ref([]);
const loading = ref(false);
const status = ref('ALL');
const tabs = ORDER_TABS;

async function fetchOrders() {
  loading.value = true;
  try {
    orders.value = await getOrders({ status: status.value });
  } finally {
    loading.value = false;
  }
}

function switchTab(key) {
  status.value = key;
  fetchOrders();
}

function pay(o) {
  router.push(`/pay/${o.orderNo}`);
}

async function cancel(o) {
  await ElMessageBox.confirm('确认取消该订单吗？', '提示', { type: 'warning' });
  await cancelOrder(o.orderNo);
  ElMessage.success('订单已取消');
  fetchOrders();
}

async function confirm(o) {
  await confirmOrder(o.orderNo);
  ElMessage.success('已确认收货');
  fetchOrders();
}

onMounted(fetchOrders);
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.page-title { margin: 0 0 16px; }
.order-tabs { display: flex; gap: 6px; margin-bottom: 16px; background: #fff; padding: 8px; border-radius: 8px; }
.order-tabs span { padding: 8px 20px; cursor: pointer; border-radius: 6px; }
.order-tabs span.active { background: var(--df-primary); color: #fff; }
.order-list { min-height: 200px; }
.order-card { margin-bottom: 16px; }
.order-head { padding: 12px 20px; border-bottom: 1px solid #f0f0f0; }
.shop { font-weight: 600; cursor: pointer; }
.time { margin-left: 16px; color: var(--df-text-secondary); font-size: 13px; }
.order-body { display: flex; align-items: center; padding: 16px 20px; cursor: pointer; }
.item { display: flex; align-items: center; gap: 12px; flex: 1; }
.item img { width: 68px; height: 68px; border-radius: 6px; object-fit: cover; }
.item .title { font-size: 14px; }
.item .spec { font-size: 12px; color: var(--df-text-secondary); }
.amount-col { text-align: right; width: 140px; }
.total { color: var(--df-danger); font-weight: 700; font-size: 16px; }
.count { font-size: 12px; color: var(--df-text-secondary); }
.order-foot { padding: 12px 20px; border-top: 1px solid #f0f0f0; }
.order-no { color: var(--df-text-secondary); font-size: 13px; }
.actions { display: flex; gap: 8px; }
</style>
