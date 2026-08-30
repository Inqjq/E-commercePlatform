<template>
  <div class="page">
    <el-tabs v-model="tab">
      <el-tab-pane label="操作日志" name="oper">
        <el-card shadow="never"><el-table :data="operLogs" border>
          <el-table-column label="操作人" width="120"><template #default="{ row }">{{ row.operatorName || row.operator }}</template></el-table-column>
          <el-table-column prop="module" label="模块" width="140" />
          <el-table-column prop="action" label="操作" width="140" />
          <el-table-column prop="target" label="对象" width="120" />
          <el-table-column label="内容" min-width="280" show-overflow-tooltip><template #default="{ row }">{{ row.detail || row.content }}</template></el-table-column>
          <el-table-column prop="ip" label="IP" width="140" />
          <el-table-column prop="createTime" label="时间" width="180" />
        </el-table></el-card>
      </el-tab-pane>
      <el-tab-pane label="登录日志" name="login">
        <el-card shadow="never"><el-table :data="loginLogs" border>
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="ip" label="IP" width="140" />
          <el-table-column prop="browser" label="浏览器" width="140" />
          <el-table-column prop="os" label="系统" width="140" />
          <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">{{ row.status === 'SUCCESS' ? '成功' : '失败' }}</el-tag></template></el-table-column>
          <el-table-column prop="loginTime" label="登录时间" width="180" />
        </el-table></el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getOperLogs, getLoginLogs } from '@/api/admin';

const tab = ref('oper');
const operLogs = ref([]);
const loginLogs = ref([]);
onMounted(async () => {
  operLogs.value = (await getOperLogs()).records;
  loginLogs.value = (await getLoginLogs()).records;
});
</script>

<style scoped>
.page { padding: 20px; }
</style>
