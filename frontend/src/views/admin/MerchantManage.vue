<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>商家入驻审核</b></template>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="name" label="商家名称" min-width="140" />
        <el-table-column prop="licenseNo" label="营业执照号" width="210" />
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="contact" label="联系电话" width="130" />
        <el-table-column prop="categoryIds" label="经营类目" width="120" />
        <el-table-column prop="auditStatus" label="审核状态" width="110"><template #default="{ row }"><el-tag :type="AUDIT_STATUS_MAP[row.auditStatus]?.type">{{ AUDIT_STATUS_MAP[row.auditStatus]?.label }}</el-tag></template></el-table-column>
        <el-table-column prop="auditTime" label="审核时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button @click="viewDetail(row)">查看资料</el-button>
            <template v-if="row.auditStatus === 'PENDING'">
              <el-button type="success" size="small" @click="audit(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" size="small" @click="audit(row, 'REJECTED')">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><el-pagination background layout="prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="fetch" /></div>
    </el-card>

    <el-dialog v-model="detailVisible" title="商家入驻资料" width="560px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="企业名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ detail.licenseNo }}</el-descriptions-item>
        <el-descriptions-item label="法定代表人">{{ detail.legalPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contact }}</el-descriptions-item>
        <el-descriptions-item label="经营类目">{{ detail.categoryIds }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button type="primary" @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMerchants, auditMerchant } from '@/api/admin';
import { AUDIT_STATUS_MAP } from '@/utils/constants';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const detailVisible = ref(false);
const detail = ref(null);
const query = reactive({ page: 1, size: 10 });

async function fetch() {
  loading.value = true;
  try { const d = await getMerchants({ page: query.page, size: query.size }); list.value = d.records; total.value = d.total; } finally { loading.value = false; }
}
function viewDetail(row) { detail.value = row; detailVisible.value = true; }
async function audit(row, st) {
  await auditMerchant({ id: row.id, auditStatus: st });
  ElMessage.success(st === 'APPROVED' ? '已通过审核' : '已驳回');
  fetch();
}
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
