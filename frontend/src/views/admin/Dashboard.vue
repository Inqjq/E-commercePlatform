<template>
  <div class="page">
    <div class="stat-grid">
      <div class="stat-card"><div class="label">今日订单</div><div class="value">{{ data.todayOrder }}</div></div>
      <div class="stat-card"><div class="label">今日销售额</div><div class="value">¥{{ formatWan(data.todaySales) }}</div></div>
      <div class="stat-card"><div class="label">总用户</div><div class="value">{{ data.totalUser }}</div></div>
      <div class="stat-card"><div class="label">入驻商家</div><div class="value">{{ data.totalMerchant }}</div></div>
      <div class="stat-card"><div class="label">商品总量</div><div class="value">{{ data.totalGoods }}</div></div>
      <div class="stat-card warning"><div class="label">转化率</div><div class="value">{{ data.conversionRate }}%</div></div>
    </div>

    <div class="grid">
      <el-card shadow="never">
        <template #header><b>近 12 期销售趋势</b></template>
        <template v-if="data.salesTrend && data.salesTrend.length">
          <div class="bar-row" v-for="(v, i) in data.salesTrend" :key="i">
            <span class="bar-label">第{{ i + 1 }}期</span>
            <div class="bar-track"><div class="bar" :style="{ width: (v / maxSales * 100) + '%' }"></div></div>
            <span class="bar-value">{{ formatWan(v) }}</span>
          </div>
        </template>
        <el-empty v-else description="暂无成交数据" :image-size="70" />
      </el-card>
      <el-card shadow="never">
        <template #header><b>类目销售占比</b></template>
        <template v-if="data.categorySales && data.categorySales.length">
          <div v-for="c in data.categorySales" :key="c.name" class="cat-row">
            <span class="cat-name">{{ c.name }}</span>
            <el-progress :percentage="Math.round(c.value / maxCat * 100)" :format="() => formatWan(c.value)" :stroke-width="16" :color="'#3d7eff'" />
          </div>
        </template>
        <el-empty v-else description="暂无订单数据" :image-size="70" />
      </el-card>
    </div>

    <div class="todo-grid">
      <el-card shadow="never" class="todo" @click="$router.push('/admin/goods')">
        <div class="num">{{ data.pendingGoodsAudit }}</div><div class="txt">待审核商品</div>
      </el-card>
      <el-card shadow="never" class="todo" @click="$router.push('/admin/merchant')">
        <div class="num">{{ data.pendingMerchantAudit }}</div><div class="txt">待审核商家</div>
      </el-card>
      <el-card shadow="never" class="todo" @click="$router.push('/admin/logs')">
        <div class="num">180d</div><div class="txt">日志保留</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue';
import { getDashboardOverview } from '@/api/admin';
import { formatWan } from '@/utils/format';

const data = reactive({});
const maxSales = computed(() => Math.max(...(data.salesTrend || [1])));
const maxCat = computed(() => Math.max(...(data.categorySales || [{ value: 1 }]).map((c) => c.value)));

onMounted(async () => {
  Object.assign(data, await getDashboardOverview());
});
</script>

<style scoped>
.page { padding: 20px; }
.stat-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: var(--df-shadow); }
.label { color: var(--df-text-secondary); font-size: 13px; }
.value { font-size: 26px; font-weight: 700; margin-top: 8px; }
.warning .value { color: var(--df-warning); }
.grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-top: 16px; }
.bar-row { display: flex; align-items: center; gap: 10px; margin: 8px 0; }
.bar-label { width: 60px; font-size: 12px; color: var(--df-text-secondary); }
.bar-track { flex: 1; background: #f0f2f5; border-radius: 4px; height: 16px; }
.bar { height: 100%; background: linear-gradient(90deg,#3d7eff,#6fa0ff); border-radius: 4px; }
.bar-value { width: 60px; text-align: right; font-size: 12px; }
.cat-row { margin: 14px 0; }
.cat-name { margin-bottom: 6px; }
.todo-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 16px; }
.todo { text-align: center; cursor: pointer; }
.todo .num { font-size: 30px; font-weight: 700; color: var(--df-primary); }
.todo .txt { color: var(--df-text-secondary); margin-top: 6px; }
</style>
