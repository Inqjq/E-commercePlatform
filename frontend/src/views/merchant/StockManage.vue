<template>
  <div class="page">
    <el-alert title="库存预警商品" type="warning" :closable="false" show-icon class="alert" />
    <div class="warn-grid">
      <div v-for="w in warnings" :key="w.id" class="warn-card">
        <div class="title">{{ w.title }}</div>
        <div class="spec">规格：{{ w.sku }}</div>
        <div class="stock">当前库存：<b :class="{ danger: w.stock < w.threshold }">{{ w.stock }}</b> / 预警线 {{ w.threshold }}</div>
        <el-input-number v-model.lazy="w.stock" :min="0" size="small" />
        <el-button type="success" size="small" @click="replenish(w)">补货</el-button>
      </div>
    </div>

    <el-card shadow="never" class="mt">
      <template #header><b>全部 SKU 库存</b></template>
      <el-table :data="skuList" border>
        <el-table-column prop="title" label="商品名称" min-width="220" />
        <el-table-column prop="sku" label="规格" width="120" />
        <el-table-column prop="code" label="SKU 编码" width="120" />
        <el-table-column prop="stock" label="库存" width="120">
          <template #default="{ row }"><el-input-number v-model="row.stock" :min="0" size="small" @change="saveStock(row)" /></template>
        </el-table-column>
        <el-table-column prop="threshold" label="预警线" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getStockWarnings, getMerchantGoods } from '@/api/merchant';

const warnings = ref([]);
const skuList = ref([]);

function replenish(w) {
  w.stock += 100;
  ElMessage.success(`已为「${w.title}」补货 100 件`);
}
function saveStock(row) {
  ElMessage.success('库存已更新');
}

onMounted(async () => {
  warnings.value = await getStockWarnings();
  const data = await getMerchantGoods({ page: 1, size: 100 });
  skuList.value = data.records.map((g) => ({ id: g.id, title: g.title, sku: '标准版', code: `SKU${g.id}0`, stock: g.stock, threshold: 20 }));
});
</script>

<style scoped>
.page { padding: 20px; }
.alert { margin-bottom: 16px; }
.warn-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px; }
.warn-card { background: #fff; border-radius: 8px; padding: 16px; box-shadow: var(--df-shadow); }
.warn-card .title { font-weight: 600; }
.warn-card .spec { font-size: 12px; color: var(--df-text-secondary); margin: 6px 0; }
.warn-card .stock { margin-bottom: 12px; }
.danger { color: var(--df-danger); }
.mt { margin-top: 8px; }
</style>
