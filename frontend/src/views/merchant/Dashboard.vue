<template>
  <div class="page">
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">今日订单</div>
        <div class="value">{{ data.todayOrder }}</div>
        <div class="sub">较昨日 +18%</div>
      </div>
      <div class="stat-card">
        <div class="label">今日销售额</div>
        <div class="value">¥{{ formatPrice(data.todaySales) }}</div>
        <div class="sub">较昨日 +8%</div>
      </div>
      <div class="stat-card">
        <div class="label">商品总数</div>
        <div class="value">{{ data.totalGoods }}</div>
        <div class="sub">在售 {{ data.totalGoods - 2 }} 件</div>
      </div>
      <div class="stat-card">
        <div class="label">待发货</div>
        <div class="value">{{ data.pendingShip }}</div>
        <div class="sub">需要及时处理</div>
      </div>
      <div class="stat-card warning">
        <div class="label">库存预警</div>
        <div class="value">{{ data.stockWarning }}</div>
        <div class="sub">库存偏低商品</div>
      </div>
    </div>

    <div class="grid-2">
      <el-card shadow="never" class="card">
        <template #header><b>近 7 日销售趋势</b></template>
        <div class="trend">
          <el-progress v-for="(v, i) in trend" :key="i" :percentage="v" :format="() => `${sales[i]}元`" :stroke-width="18" />
        </div>
      </el-card>
      <el-card shadow="never" class="card">
        <template #header><b>待处理事项</b></template>
        <div class="todo">
          <div class="todo-item"><el-badge :value="data.pendingShip" :max="99"><span>待发货订单</span></el-badge><a @click="$router.push('/merchant/orders?status=PENDING_SHIP')">去处理</a></div>
          <div class="todo-item"><el-badge :value="3" :max="99"><span>待处理售后</span></el-badge><a @click="$router.push('/merchant/after-sales')">去处理</a></div>
          <div class="todo-item"><el-badge :value="data.stockWarning" :max="99"><span>库存预警</span></el-badge><a @click="$router.push('/merchant/stock')">去补货</a></div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { getMerchantDashboard } from '@/api/merchant';
import { formatPrice } from '@/utils/format';

const data = reactive({ todayOrder: 0, todaySales: 0, totalGoods: 0, pendingShip: 0, stockWarning: 0 });
const sales = [45680, 52000, 41000, 38000, 56000, 61000, 45800];
const trend = [46, 52, 41, 38, 56, 61, 46];

onMounted(async () => {
  Object.assign(data, await getMerchantDashboard());
});
</script>

<style scoped>
.page { padding: 20px; }
.stat-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--df-shadow); }
.label { color: var(--df-text-secondary); font-size: 13px; }
.value { font-size: 28px; font-weight: 700; margin: 8px 0; }
.sub { font-size: 12px; color: var(--df-text-secondary); }
.stat-card.warning .value { color: var(--df-warning); }
.grid-2 { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-top: 16px; }
.trend { display: flex; flex-direction: column; gap: 12px; }
.todo-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid #f5f5f5; }
.todo-item a { color: var(--df-primary); cursor: pointer; }
</style>
