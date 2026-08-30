<template>
  <div class="page">
    <div class="tabs">
      <span v-for="t in tabs" :key="t.key" :class="{ active: status === t.key }" @click="switchTab(t.key)">{{ t.label }}</span>
    </div>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="receiver" label="收货人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="amount" label="订单金额" width="120"><template #default="{ row }">¥{{ formatPrice(row.amount) }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="ORDER_STATUS_MAP[row.status]?.type">{{ ORDER_STATUS_MAP[row.status]?.label }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING_SHIP'" type="primary" size="small" @click="openShip(row)">发货</el-button>
            <el-button text size="small" @click="ElMessage.info('详情页开发中')">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无订单" />
    </el-card>

    <el-dialog v-model="shipVisible" title="订单发货" width="480px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="物流公司"><el-select v-model="shipForm.company"><el-option label="顺丰速运" value="顺丰速运" /><el-option label="圆通速递" value="圆通速递" /><el-option label="中通快递" value="中通快递" /></el-select></el-form-item>
        <el-form-item label="运单号"><el-input v-model="shipForm.no" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="shipVisible = false">取消</el-button><el-button type="primary" @click="doShip">确认发货</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMerchantOrders, shipOrder } from '@/api/merchant';
import { ORDER_STATUS_MAP } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const list = ref([]);
const loading = ref(false);
const status = ref('ALL');
const shipVisible = ref(false);
const shipForm = reactive({ company: '顺丰速运', no: '' });
const tabs = [
  { key: 'ALL', label: '全部' },
  { key: 'PENDING_SHIP', label: '待发货' },
  { key: 'SHIPPED', label: '已发货' },
  { key: 'COMPLETED', label: '已完成' },
];

async function fetch() {
  loading.value = true;
  try { list.value = await getMerchantOrders({ status: status.value }); } finally { loading.value = false; }
}
function switchTab(k) { status.value = k; fetch(); }
function openShip(row) { shipForm.no = ''; shipForm.orderNo = row.orderNo; shipVisible.value = true; }
async function doShip() {
  if (!shipForm.no) return ElMessage.warning('请填写运单号');
  await shipOrder(shipForm.orderNo, { company: shipForm.company, no: shipForm.no });
  ElMessage.success('发货成功');
  shipVisible.value = false;
  fetch();
}

onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.tabs { display: flex; gap: 6px; margin-bottom: 16px; background: #fff; padding: 8px; border-radius: 8px; width: fit-content; }
.tabs span { padding: 8px 20px; cursor: pointer; border-radius: 6px; }
.tabs span.active { background: var(--df-primary); color: #fff; }
</style>
