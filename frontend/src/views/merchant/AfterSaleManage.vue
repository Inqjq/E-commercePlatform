<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>售后管理</b></template>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="goodsTitle" label="商品" min-width="200" />
        <el-table-column label="售后类型" width="100"><template #default="{ row }">{{ typeMap[row.type] }}</template></el-table-column>
        <el-table-column prop="reason" label="原因" width="140" />
        <el-table-column prop="price" label="退款金额" width="110"><template #default="{ row }">¥{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="AFTER_SALE_STATUS_MAP[row.status]?.type">{{ AFTER_SALE_STATUS_MAP[row.status]?.label }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="['PENDING', 'PROCESSING'].includes(row.status)">
              <el-button size="small" type="success" @click="audit(row, 'AGREED')">同意</el-button>
              <el-button size="small" type="danger" @click="audit(row, 'REJECTED')">拒绝</el-button>
            </template>
            <span v-else>已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无售后申请" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAfterSales, auditAfterSale } from '@/api/merchant';
import { AFTER_SALE_STATUS_MAP } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const list = ref([]);
const loading = ref(false);
const typeMap = { REFUND: '仅退款', REFUND_RETURN: '退货退款', EXCHANGE: '换货' };

async function fetch() {
  loading.value = true;
  try { list.value = await getAfterSales(); } finally { loading.value = false; }
}
async function audit(row, status) {
  await auditAfterSale(row.id, status);
  ElMessage.success(status === 'AGREED' ? '已同意' : '已拒绝');
  fetch();
}

onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
</style>
