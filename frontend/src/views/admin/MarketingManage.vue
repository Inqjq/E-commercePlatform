<template>
  <div class="page">
    <div class="toolbar df-flex-between"><b>营销活动</b><el-button type="primary" :icon="Plus" @click="dialogVisible = true">新建活动</el-button></div>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column label="类型" width="110"><template #default="{ row }">{{ typeMap[row.type] }}</template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="130" />
        <el-table-column prop="endTime" label="结束时间" width="130" />
        <el-table-column prop="goodsCount" label="商品数" width="90" />
        <el-table-column prop="status" label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 'ON' ? 'success' : 'info'">{{ row.status === 'ON' ? '进行中' : '已停止' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }"><el-button size="small" :type="row.status === 'ON' ? 'warning' : 'success'" @click="toggle(row)">{{ row.status === 'ON' ? '停止' : '启动' }}</el-button><el-button size="small" text type="danger">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getMarketing } from '@/api/admin';

const list = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const typeMap = { COUPON: '优惠券', FLASH: '限时秒杀', PMT: '满减促销' };
async function fetch() { loading.value = true; try { list.value = await getMarketing(); } finally { loading.value = false; } }
function toggle(row) { row.status = row.status === 'ON' ? 'OFF' : 'ON'; ElMessage.success('操作成功'); }
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; }
</style>
