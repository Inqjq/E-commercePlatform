<template>
  <div class="container" v-loading="loading">
    <template v-if="order.orderNo">
      <div class="status-banner df-card df-flex-between">
        <div class="left">
          <h2>{{ ORDER_STATUS_MAP[order.status]?.label }}</h2>
          <p v-if="order.status === 'PENDING_PAYMENT'">请在 <b>30 分钟</b> 内完成支付，超时订单将自动关闭</p>
          <p v-else class="desc">感谢您使用渡风电商平台</p>
        </div>
        <div class="right"><span class="amount">¥{{ formatPrice(order.payAmount) }}</span></div>
      </div>

      <div class="block df-card">
        <div class="title">收货信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="收货人">{{ order.address?.receiver }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.address?.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.address?.province }}{{ order.address?.city }}{{ order.address?.district }} {{ order.address?.detail }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="order.logistics" class="block df-card">
        <div class="title">物流信息</div>
        <div class="logistics">物流公司：{{ order.logistics.company }}　运单号：{{ order.logistics.no }}</div>
        <el-timeline>
          <el-timeline-item timestamp="2026-08-29 18:00" type="primary">快件已到达【深圳南山转运中心】</el-timeline-item>
          <el-timeline-item timestamp="2026-08-29 12:00">快件正在派送 快递员：王师傅</el-timeline-item>
        </el-timeline>
      </div>

      <div class="block df-card">
        <div class="title">商品清单</div>
        <div v-for="item in order.items" :key="item.id" class="item">
          <img :src="item.image" />
          <div class="info"><div class="name">{{ item.title }}</div><div class="spec">{{ item.spec }}</div></div>
          <div class="p">¥{{ formatPrice(item.price) }}</div>
          <div class="q">x{{ item.quantity }}</div>
          <div class="sub">¥{{ formatPrice(item.price * item.quantity) }}</div>
        </div>
      </div>

      <div class="block df-card">
        <div class="title">订单信息</div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ ORDER_STATUS_MAP[order.status]?.label }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="商品金额">¥{{ formatPrice(order.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="运费">¥{{ formatPrice(order.freight) }}</el-descriptions-item>
          <el-descriptions-item label="优惠">-¥{{ formatPrice(order.couponAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额"><span class="red">¥{{ formatPrice(order.payAmount) }}</span></el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="actions">
        <el-button v-if="order.status === 'PENDING_PAYMENT'" type="danger" size="large" @click="pay">去支付</el-button>
        <el-button v-if="order.status === 'PENDING_PAYMENT'" size="large" @click="cancel">取消订单</el-button>
        <el-button v-if="order.status === 'SHIPPED'" type="primary" size="large" @click="confirm">确认收货</el-button>
        <el-button v-if="['COMPLETED'].includes(order.status)" type="warning" size="large" @click="$router.push(`/after-sale/${order.orderNo}`)">申请售后</el-button>
        <el-button size="large" @click="$router.push('/orders')">返回列表</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getOrderDetail, cancelOrder, confirmOrder } from '@/api/order';
import { ORDER_STATUS_MAP } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const order = ref({});

onMounted(async () => {
  loading.value = true;
  try {
    order.value = await getOrderDetail(route.params.orderNo);
  } finally {
    loading.value = false;
  }
});

function pay() { router.push(`/pay/${order.value.orderNo}`); }
async function cancel() {
  await ElMessageBox.confirm('确认取消该订单吗？', '提示', { type: 'warning' });
  await cancelOrder(order.value.orderNo);
  ElMessage.success('订单已取消');
  order.value.status = 'CANCELLED';
}
async function confirm() {
  await confirmOrder(order.value.orderNo);
  ElMessage.success('已确认收货');
  order.value.status = 'COMPLETED';
}
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.status-banner { padding: 24px 28px; align-items: center; }
.status-banner h2 { margin: 0 0 8px; }
.status-banner .desc { color: var(--df-text-secondary); }
.amount { font-size: 32px; font-weight: 700; color: var(--df-danger); }
.block { margin-top: 16px; padding: 20px 24px; }
.block .title { font-weight: 600; margin-bottom: 16px; }
.item { display: flex; align-items: center; gap: 16px; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.item img { width: 60px; height: 60px; border-radius: 6px; object-fit: cover; }
.item .info { flex: 1; }
.item .spec { font-size: 12px; color: var(--df-text-secondary); }
.item .p { width: 90px; }
.item .q { width: 60px; }
.item .sub { width: 100px; font-weight: 600; }
.logistics { margin-bottom: 12px; color: var(--df-text-regular); }
.red { color: var(--df-danger); font-weight: 700; }
.actions { display: flex; justify-content: center; gap: 12px; margin-top: 24px; }
</style>
