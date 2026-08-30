<template>
  <div class="page">
    <div class="toolbar df-flex-between">
      <b>营销活动</b>
      <el-button type="primary" :icon="Plus" @click="dialogVisible = true">新建活动</el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column label="活动类型" width="120"><template #default="{ row }">{{ typeMap[row.type] }}</template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="130" />
        <el-table-column prop="endTime" label="结束时间" width="130" />
        <el-table-column prop="goodsCount" label="参与商品" width="100" />
        <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 'ON' ? 'success' : 'info'">{{ row.status === 'ON' ? '进行中' : '已暂停' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }"><el-button size="small" :type="row.status === 'ON' ? 'warning' : 'success'" @click="toggle(row)">{{ row.status === 'ON' ? '暂停' : '开始' }}</el-button><el-button size="small" text type="danger">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建营销活动" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="活动名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="活动类型"><el-select v-model="form.type"><el-option label="优惠券" value="COUPON" /><el-option label="限时秒杀" value="FLASH" /><el-option label="满减促销" value="PMT" /></el-select></el-form-item>
        <el-form-item label="起止时间"><el-date-picker v-model="range" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="create">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getMarketing } from '@/api/merchant';

const list = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const range = ref([]);
const form = reactive({ name: '', type: 'COUPON' });
const typeMap = { COUPON: '优惠券', FLASH: '限时秒杀', PMT: '满减促销' };

async function fetch() {
  loading.value = true;
  try { list.value = await getMarketing(); } finally { loading.value = false; }
}
function toggle(row) { row.status = row.status === 'ON' ? 'OFF' : 'ON'; ElMessage.success('操作成功'); }
function create() {
  if (!form.name) return ElMessage.warning('请输入活动名称');
  list.value.unshift({ id: list.value.length + 1, name: form.name, type: form.type, startTime: '', endTime: '', goodsCount: 0, status: 'OFF' });
  ElMessage.success('活动已创建');
  dialogVisible.value = false;
}

onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
