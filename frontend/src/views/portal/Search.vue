<template>
  <div class="container">
    <div class="search-hero">
      <div class="hero-logo">渡风搜索</div>
      <div class="search-box">
        <el-autocomplete v-model="keyword" :fetch-suggestions="fetchSuggest" placeholder="搜索商品 / 品牌" clearable class="input" @keyup.enter="doSearch" @select="onSelect">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-autocomplete>
        <el-button type="primary" class="btn" @click="doSearch">搜索</el-button>
      </div>
      <div class="hot">
        <span class="hot-label">热门搜索：</span>
        <span v-for="k in hotWords" :key="k" class="hot-word" @click="quickSearch(k)">{{ k }}</span>
      </div>
      <div v-if="history.length" class="history">
        <span class="hot-label">搜索历史：</span>
        <el-tag v-for="h in history" :key="h" closable size="small" @close="removeHistory(h)" @click="quickSearch(h)">{{ h }}</el-tag>
        <a class="clear" @click="clearHistory">清空</a>
      </div>
    </div>

    <div v-if="searched" class="results">
      <div class="result-title">“{{ query }}” 的搜索结果（共 {{ total }} 条）</div>
      <div v-loading="loading" class="goods-grid">
        <GoodsCard v-for="g in list" :key="g.id" :goods="g" />
      </div>
      <el-empty v-if="!loading && !list.length" description="没有找到相关商品" />
      <div class="pager">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="page" @current-change="fetchResults" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import GoodsCard from '@/components/goods/GoodsCard.vue';
import { getGoodsList, getSearchSuggest } from '@/api/goods';

const keyword = ref('');
const query = ref('');
const searched = ref(false);
const loading = ref(false);
const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = 12;
const history = ref(JSON.parse(localStorage.getItem('df_search_history') || '[]'));
const hotWords = ['iPhone 15', '华为', '美的电视', '连衣裙', '小米', '空调'];

async function fetchSuggest(q, cb) {
  if (!q) return cb(hotWords.map((w) => ({ value: w })));
  const data = await getSearchSuggest(q);
  cb(data.map((d) => ({ value: d.keyword })));
}

async function fetchResults() {
  loading.value = true;
  try {
    const data = await getGoodsList({ page: page.value, size, keyword: query.value });
    list.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function saveHistory(kw) {
  if (!kw) return;
  history.value = [kw, ...history.value.filter((h) => h !== kw)].slice(0, 8);
  localStorage.setItem('df_search_history', JSON.stringify(history.value));
}

function doSearch() {
  const kw = keyword.value.trim();
  if (!kw) return;
  query.value = kw;
  searched.value = true;
  page.value = 1;
  saveHistory(kw);
  fetchResults();
}
function quickSearch(kw) {
  keyword.value = kw;
  doSearch();
}
function onSelect(item) {
  keyword.value = item.value;
  doSearch();
}
function removeHistory(h) {
  history.value = history.value.filter((x) => x !== h);
  localStorage.setItem('df_search_history', JSON.stringify(history.value));
}
function clearHistory() {
  history.value = [];
  localStorage.removeItem('df_search_history');
}

onMounted(() => {
  const q = new URLSearchParams(window.location.search).get('keyword');
  if (q) { keyword.value = q; doSearch(); }
});
</script>

<style scoped>
.container { max-width: 1080px; margin: 0 auto; padding: 20px; }
.search-hero { text-align: center; padding: 40px 0 24px; }
.hero-logo { font-size: 28px; font-weight: 700; color: var(--df-primary); margin-bottom: 24px; }
.search-box { display: flex; justify-content: center; gap: 8px; max-width: 640px; margin: 0 auto; }
.search-box .input { flex: 1; }
.search-box .btn { width: 100px; }
.hot { margin-top: 18px; }
.hot-label { color: var(--df-text-secondary); }
.hot-word, .history .el-tag { margin: 0 8px; cursor: pointer; color: var(--df-text-regular); }
.hot-word:hover { color: var(--df-primary); }
.history { margin-top: 14px; }
.history .clear { margin-left: 16px; color: var(--df-text-secondary); font-size: 12px; cursor: pointer; }
.results { margin-top: 20px; }
.result-title { font-size: 15px; margin-bottom: 16px; }
.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; min-height: 160px; }
.pager { display: flex; justify-content: center; margin-top: 20px; }
</style>
