<template>
  <div class="container">
    <h2 class="page-title">优惠券</h2>
    <div class="tabs">
      <span :class="{ active: tab === 'available' }" @click="tab = 'available'">领券中心</span>
      <span :class="{ active: tab === 'mine' }" @click="tab = 'mine'">我的优惠券</span>
    </div>

    <div v-if="tab === 'available'" v-loading="loading" class="coupon-grid">
      <div v-for="c in available" :key="c.id" class="coupon">
        <div class="left"><span class="value">¥{{ c.discount }}</span><span class="threshold">{{ c.threshold ? `满${c.threshold}可用` : '无门槛' }}</span></div>
        <div class="right">
          <div class="name">{{ c.name }}</div>
          <div class="scope">{{ c.scope }}</div>
          <div class="date">{{ c.validStart }} 至 {{ c.validEnd }}</div>
        </div>
        <el-button type="primary" plain size="small" class="btn" @click="receive(c)">领取</el-button>
      </div>
      <el-empty v-if="!loading && !available.length" description="暂无可领取优惠券" />
    </div>

    <div v-else v-loading="loading" class="coupon-grid">
      <div v-for="c in mine" :key="c.id" class="coupon" :class="{ used: c.status !== 'UNUSED' }">
        <div class="left"><span class="value">¥{{ c.discount }}</span><span class="threshold">{{ c.threshold ? `满${c.threshold}可用` : '无门槛' }}</span></div>
        <div class="right">
          <div class="name">{{ c.name }}</div>
          <div class="date">有效期至 {{ c.expireAt }}</div>
        </div>
        <el-tag class="btn" :type="c.status === 'UNUSED' ? 'success' : 'info'" size="small">{{ c.status === 'UNUSED' ? '未使用' : '已使用' }}</el-tag>
      </div>
      <el-empty v-if="!loading && !mine.length" description="暂无优惠券" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAvailableCoupons, getMyCoupons, receiveCoupon } from '@/api/coupon';

const tab = ref('available');
const available = ref([]);
const mine = ref([]);
const loading = ref(false);

async function fetch() {
  loading.value = true;
  try {
    available.value = await getAvailableCoupons();
    mine.value = await getMyCoupons();
  } finally {
    loading.value = false;
  }
}

async function receive(c) {
  await receiveCoupon(c.id);
  ElMessage.success('领取成功');
  fetch();
}

onMounted(fetch);
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.page-title { margin: 0 0 16px; }
.tabs { display: flex; gap: 6px; margin-bottom: 16px; background: #fff; padding: 8px; border-radius: 8px; width: fit-content; }
.tabs span { padding: 8px 20px; cursor: pointer; border-radius: 6px; }
.tabs span.active { background: var(--df-primary); color: #fff; }
.coupon-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.coupon { display: flex; align-items: center; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: var(--df-shadow); }
.coupon.used { opacity: .5; }
.left { background: linear-gradient(135deg, #f56c6c, #ff8c8c); color: #fff; width: 120px; text-align: center; padding: 24px 10px; font-weight: 700; }
.value { font-size: 28px; display: block; }
.threshold { font-size: 12px; font-weight: 400; }
.right { flex: 1; padding: 16px; }
.name { font-weight: 600; }
.scope, .date { font-size: 12px; color: var(--df-text-secondary); margin-top: 6px; }
.btn { margin-right: 12px; }
</style>
