<template>
  <div class="page">
    <div class="toolbar">
      <div class="left df-flex">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width:220px" @keyup.enter="search" />
        <el-select v-model="status" placeholder="商品状态" clearable style="width:140px" @change="search">
          <el-option v-for="(v, k) in GOODS_STATUS_MAP" :key="k" :label="v.label" :value="k" />
        </el-select>
      </div>
      <div class="right">
        <el-button type="primary" :icon="Plus" @click="$router.push('/merchant/goods/edit')">发布商品</el-button>
      </div>
    </div>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="title" label="商品名称" min-width="240" />
        <el-table-column prop="categoryId" label="类目" :formatter="categoryName" width="120" />
        <el-table-column prop="price" label="价格" width="100"><template #default="{ row }">¥{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="GOODS_STATUS_MAP[row.status]?.type">{{ GOODS_STATUS_MAP[row.status]?.label }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="$router.push(`/merchant/goods/edit/${row.id}`)">编辑</el-button>
            <el-button v-if="row.status === 'ON_SALE'" text type="warning" @click="toggleStatus(row, 'OFF_SALE')">下架</el-button>
            <el-button v-else text type="success" @click="toggleStatus(row, 'ON_SALE')">上架</el-button>
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
import { Plus } from '@element-plus/icons-vue';
import { getMerchantGoods, updateMerchantGoodsStatus } from '@/api/merchant';
import { GOODS_STATUS_MAP } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const keyword = ref('');
const status = ref('');
const query = reactive({ page: 1, size: 10 });

const categoryMap = {};

async function fetch() {
  loading.value = true;
  try {
    const data = await getMerchantGoods({ page: query.page, size: query.size, keyword: keyword.value, status: status.value });
    list.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}
function search() { query.page = 1; fetch(); }
async function toggleStatus(row, st) {
  await updateMerchantGoodsStatus(row.id, st);
  ElMessage.success(st === 'ON_SALE' ? '已上架' : '已下架');
  fetch();
}
function categoryName(row) { return { 11: '手机', 12: '笔记本', 21: '电视', 22: '冰箱', 31: '男装', 32: '女装' }[row.categoryId] || row.categoryId; }

onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.left { gap: 12px; }
.pager { display: flex; justify-content: center; margin-top: 16px; }
</style>
