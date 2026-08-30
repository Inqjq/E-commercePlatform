<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>用户管理</b></template>
      <div class="toolbar df-flex">
        <el-input v-model="keyword" placeholder="搜索用户名/手机号" clearable style="width:260px" @keyup.enter="search" />
        <el-select v-model="status" placeholder="状态" clearable style="width:140px" @change="search">
          <el-option label="正常" value="NORMAL" /><el-option label="已禁用" value="DISABLED" /><el-option label="已冻结" value="FROZEN" />
        </el-select>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column prop="orderCount" label="订单数" width="90" />
        <el-table-column prop="totalAmount" label="累计消费" width="120"><template #default="{ row }">¥{{ formatPrice(row.totalAmount) }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="statusType[row.status]">{{ statusLabel[row.status] }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="['DISABLED', 'FROZEN'].includes(row.status)" size="small" type="success" @click="changeStatus(row, 'NORMAL')">启用</el-button>
            <el-button v-else size="small" type="danger" @click="changeStatus(row, 'DISABLED')">禁用</el-button>
            <el-button size="small" text type="primary">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><el-pagination background layout="prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="fetch" /></div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAdminUsers, updateUserStatus } from '@/api/admin';
import { formatPrice } from '@/utils/format';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const keyword = ref('');
const status = ref('');
const query = reactive({ page: 1, size: 10 });
const statusLabel = { NORMAL: '正常', DISABLED: '已禁用', FROZEN: '已冻结' };
const statusType = { NORMAL: 'success', DISABLED: 'danger', FROZEN: 'warning' };

async function fetch() {
  loading.value = true;
  try { const d = await getAdminUsers({ page: query.page, size: query.size, keyword: keyword.value, status: status.value }); list.value = d.records; total.value = d.total; } finally { loading.value = false; }
}
function search() { query.page = 1; fetch(); }
async function changeStatus(row, st) {
  await updateUserStatus(row.id, st);
  ElMessage.success('操作成功');
  fetch();
}
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; gap: 12px; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
