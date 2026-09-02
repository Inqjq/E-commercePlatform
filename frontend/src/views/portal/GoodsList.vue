<template>
  <div class="container">
    <div class="search-bar df-card">
      <div class="row">
        <span class="label">分类</span>
        <div class="options">
          <span :class="{ active: !query.categoryId }" @click="setCategory(null)">全部</span>
          <span v-for="cat in categories" :key="cat.id" :class="{ active: String(query.categoryId) === String(cat.id) || cat.children?.some(c => String(c.id) === String(query.categoryId)) }" @click="setCategory(cat.id)">{{ cat.name }}</span>
          <template v-for="cat in categories" :key="'sub'+cat.id">
            <span v-for="child in cat.children" :key="child.id" :class="{ active: String(query.categoryId) === String(child.id) }" class="sub" @click="setCategory(child.id)">{{ child.name }}</span>
          </template>
        </div>
      </div>
      <div class="row">
        <span class="label">品牌</span>
        <div class="options">
          <span :class="{ active: !query.brandId }" @click="setBrand(null)">全部</span>
          <span v-for="b in brands" :key="b.id" :class="{ active: String(query.brandId) === String(b.id) }" @click="setBrand(b.id)">{{ b.name }}</span>
        </div>
      </div>
      <div class="row">
        <span class="label">价格</span>
        <div class="options">
          <el-input v-model="priceMin" size="small" placeholder="最低价" style="width:100px" />
          <span class="dash">-</span>
          <el-input v-model="priceMax" size="small" placeholder="最高价" style="width:100px" />
          <el-button size="small" type="primary" @click="applyPrice">确定</el-button>
        </div>
      </div>
    </div>

    <div class="toolbar df-card df-flex-between">
      <div class="sort-tabs">
        <span v-for="s in sorts" :key="s.key" :class="{ active: query.sortBy === s.key }" @click="setSort(s.key)">{{ s.label }}</span>
      </div>
      <span class="total">共 {{ total }} 件商品</span>
    </div>

    <div v-loading="loading" class="goods-list">
      <template v-if="list.length">
        <GoodsCard v-for="g in list" :key="g.id" :goods="g" />
      </template>
      <el-empty v-else description="暂无相关商品" />
    </div>

    <div class="pager">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="query.size" v-model:current-page="query.page" @current-change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import GoodsCard from '@/components/goods/GoodsCard.vue';
import { getGoodsList, getCategories, getBrands } from '@/api/goods';

const route = useRoute();
const router = useRouter();
const list = ref([]);
const total = ref(0);
const loading = ref(false);
const categories = ref([]);
const brands = ref([]);
const priceMin = ref('');
const priceMax = ref('');
const sorts = [
  { key: '', label: '综合' },
  { key: 'sales', label: '销量' },
  { key: 'price', label: '价格升序' },
  { key: 'price_desc', label: '价格降序' },
];
const query = reactive({ page: 1, size: 12, keyword: '', categoryId: '', brandId: '', sortBy: '' });

function syncQuery() {
  query.keyword = route.query.keyword || '';
  query.categoryId = route.query.categoryId || '';
  query.brandId = route.query.brandId || '';
  query.sortBy = route.query.sortBy || '';
  query.page = Number(route.query.page) || 1;
  priceMin.value = route.query.minPrice || '';
  priceMax.value = route.query.maxPrice || '';
}

async function fetchList() {
  loading.value = true;
  try {
    const data = await getGoodsList({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      brandId: query.brandId || undefined,
      sortBy: query.sortBy || undefined,
      minPrice: priceMin.value || undefined,
      maxPrice: priceMax.value || undefined,
    });
    list.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function setCategory(id) {
  query.categoryId = id || '';
  query.page = 1;
  fetchList();
}
function setBrand(id) {
  query.brandId = id || '';
  query.page = 1;
  fetchList();
}
function setSort(key) {
  query.sortBy = key;
  query.page = 1;
  fetchList();
}
function applyPrice() {
  query.page = 1;
  fetchList();
}

watch(() => route.query, () => {
  syncQuery();
  fetchList();
});

onMounted(async () => {
  syncQuery();
  fetchList();
  // 分类/品牌为筛选辅助数据，任一失败不应阻断页面主体加载
  Promise.all([
    getCategories().then((v) => { categories.value = v || []; }).catch(() => {}),
    getBrands().then((v) => { brands.value = v || []; }).catch(() => {}),
  ]);
});
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 16px 20px; }
.search-bar { padding: 16px; margin-bottom: 16px; }
.row { display: flex; align-items: center; margin-bottom: 12px; }
.row:last-child { margin-bottom: 0; }
.label { width: 60px; color: var(--df-text-secondary); font-size: 14px; flex-shrink: 0; }
.options { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.options span { font-size: 14px; color: var(--df-text-regular); cursor: pointer; padding: 2px 8px; border-radius: 4px; }
.options span.sub { color: var(--df-text-secondary); font-size: 13px; }
.options span.active { background: var(--df-primary); color: #fff; }
.options span:hover { color: var(--df-primary); }
.options span.active:hover { color: #fff; }
.dash { margin: 0 4px; }
.toolbar { padding: 12px 16px; margin-bottom: 16px; }
.sort-tabs { display: flex; gap: 24px; }
.sort-tabs span { font-size: 15px; cursor: pointer; color: var(--df-text-regular); }
.sort-tabs span.active { color: var(--df-primary); font-weight: 600; }
.total { font-size: 13px; color: var(--df-text-secondary); }
.goods-list { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; min-height: 200px; }
.pager { display: flex; justify-content: center; margin-top: 24px; }
</style>
