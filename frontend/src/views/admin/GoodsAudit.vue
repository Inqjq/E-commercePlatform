<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>商品审核</b></template>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width:220px" @keyup.enter="search" />
        <el-select v-model="auditStatus" placeholder="审核状态" clearable style="width:150px" @change="search">
          <el-option label="待审核" :value="1" /><el-option label="已通过" :value="2" /><el-option label="已驳回" :value="3" />
        </el-select>
        <el-select v-model="status" placeholder="商品状态" clearable style="width:140px" @change="search">
          <el-option label="在售" :value="2" /><el-option label="已下架" :value="3" /><el-option label="待审核" :value="1" /><el-option label="已驳回" :value="4" />
        </el-select>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="96" show-overflow-tooltip />
        <el-table-column prop="title" label="商品名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="品牌" width="120"><template #default="{ row }">{{ row.brandName || '—' }}</template></el-table-column>
        <el-table-column label="价格" width="110"><template #default="{ row }">¥{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="审核状态" width="110"><template #default="{ row }"><el-tag :type="auditType[row.auditStatus]" :effect="row.auditStatus === 1 ? 'light' : 'plain'">{{ auditLabel[row.auditStatus] }}</el-tag></template></el-table-column>
        <el-table-column label="商品状态" width="110"><template #default="{ row }"><el-tag :type="goodsType[row.status]">{{ goodsLabel[row.status] }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <template v-if="row.auditStatus === 1 || row.auditStatus === 0">
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
          <el-descriptions-item label="品牌">{{ detail.brandName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ formatPrice(detail.price) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ auditLabel[detail.auditStatus] }}</el-descriptions-item>
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
const auditStatus = ref('');
const status = ref('');
const query = reactive({ page: 1, size: 10 });
const detailVisible = ref(false);
const detail = ref(null);
const safeDetail = computed(() => sanitizeHtml(detail.value?.detail));

// 审核状态：0 未提交 1 待审核 2 通过 3 驳回
const auditLabel = { 0: '未提交', 1: '待审核', 2: '已通过', 3: '已驳回' };
const auditType = { 1: 'warning', 2: 'success', 3: 'danger' };
// 商品状态：0 草稿 1 待审核 2 在售 3 已下架 4 已驳回
const goodsLabel = { 0: '草稿', 1: '待审核', 2: '在售', 3: '已下架', 4: '已驳回' };
const goodsType = { 2: 'success', 3: 'info', 1: 'warning', 4: 'danger' };

async function fetch() {
  loading.value = true;
  try {
    const d = await getAdminGoods({
      page: query.page,
      size: query.size,
      keyword: keyword.value,
      auditStatus: auditStatus.value,
      status: status.value,
    });
    list.value = d.records;
    total.value = d.total;
  } finally {
    loading.value = false;
  }
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
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
