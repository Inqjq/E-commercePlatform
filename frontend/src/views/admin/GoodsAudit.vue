<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>商品审核</b></template>
      <div class="toolbar df-flex">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width:220px" @keyup.enter="search" />
        <el-select v-model="status" placeholder="状态" clearable style="width:150px" @change="search">
          <el-option label="待审核" value="PENDING" /><el-option label="已通过" value="APPROVED" /><el-option label="已驳回" value="REJECTED" />
        </el-select>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="商品名称" min-width="240" />
        <el-table-column prop="brandId" label="品牌" width="80" />
        <el-table-column prop="price" label="价格" width="110"><template #default="{ row }">¥{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="statusType[row.status]">{{ statusLabel[row.status] }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <template v-if="row.status === 'ON_SALE' || row.status === 'PENDING'">
              <el-button size="small" type="success" @click="audit(row, 'APPROVED')">通过</el-button>
              <el-button size="small" type="danger" @click="audit(row, 'REJECTED')">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><el-pagination background layout="prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="fetch" /></div>
    </el-card>

    <el-dialog v-model="detailVisible" title="商品详情" width="640px">
      <template v-if="detail">
        <el-image :src="detail.mainImage" fit="cover" style="width:220px;height:220px;border-radius:8px" />
        <el-descriptions :column="2" border style="margin-top:16px">
          <el-descriptions-item label="商品名称" :span="2">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ formatPrice(detail.price) }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ detail.stock }}</el-descriptions-item>
          <el-descriptions-item label="详情" :span="2"><div v-html="safeDetail"></div></el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer><el-button type="primary" @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAdminGoods, auditGoods } from '@/api/admin';
import { formatPrice } from '@/utils/format';
import { sanitizeHtml } from '@/utils/sanitize';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const keyword = ref('');
const status = ref('PENDING');
const query = reactive({ page: 1, size: 10 });
const detailVisible = ref(false);
const detail = ref(null);
// 富文本经白名单消毒后再渲染，防存储型 XSS
const safeDetail = computed(() => sanitizeHtml(detail.value?.detail));
const statusLabel = { ON_SALE: '在售', OFF_SALE: '已下架', PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' };
const statusType = { ON_SALE: 'success', OFF_SALE: 'info', PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' };

async function fetch() {
  loading.value = true;
  try { const d = await getAdminGoods({ page: query.page, size: query.size, keyword: keyword.value, status: status.value }); list.value = d.records; total.value = d.total; } finally { loading.value = false; }
}
function search() { query.page = 1; fetch(); }
function viewDetail(row) { detail.value = row; detailVisible.value = true; }
async function audit(row, st) {
  await auditGoods({ id: row.id, auditStatus: st });
  ElMessage.success(st === 'APPROVED' ? '已通过' : '已驳回');
  fetch();
}
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { margin-bottom: 16px; gap: 12px; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
