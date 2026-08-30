<template>
  <div class="container" v-loading="loading">
    <template v-if="goods.id">
      <div class="detail-card df-card">
        <div class="gallery">
          <el-image :src="currentImage" fit="cover" class="main-img" :preview-src-list="goods.images" />
          <div class="thumbs">
            <img v-for="(img, i) in goods.images" :key="i" :src="img" :class="{ active: currentImage === img }" @click="currentImage = img" />
          </div>
        </div>
        <div class="info">
          <h1 class="title">{{ goods.title }}</h1>
          <div class="subtitle">{{ goods.subtitle }}</div>
          <div class="price-box">
            <span class="cur-price"><span class="yuan">¥</span>{{ formatPrice(selectedSku?.price ?? goods.price) }}</span>
            <span class="market">市场价 ¥{{ formatPrice(goods.marketPrice) }}</span>
          </div>
          <div class="stats df-flex">
            <span>销量 {{ goods.sales }}</span>
            <span>库存 {{ selectedSku?.stock ?? goods.stock }}</span>
          </div>
          <div class="service">
            <span v-for="s in goods.service" :key="s">{{ s }}</span>
          </div>

          <div class="field">
            <label>规格</label>
            <div class="skus">
              <span v-for="sku in goods.skus" :key="sku.id" :class="{ active: selectedSku?.id === sku.id }" @click="selectedSku = sku">{{ sku.specName }}</span>
            </div>
          </div>

          <div class="field quantity">
            <label>数量</label>
            <el-input-number v-model="quantity" :min="1" :max="selectedSku?.stock || 99" />
          </div>

          <div class="actions">
            <el-button type="warning" size="large" @click="handleAddCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="handleBuy">立即购买</el-button>
            <el-button size="large" :icon="Star" @click="handleFavorite">收藏</el-button>
          </div>
        </div>
      </div>

      <div class="detail-tabs df-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="rich" v-html="safeDetail"></div>
          </el-tab-pane>
          <el-tab-pane label="商品评价" name="reviews">
            <div class="review-list">
              <div v-for="r in goods.reviews" :key="r.id" class="review-item">
                <div class="df-flex" style="gap:10px">
                  <el-avatar :size="36" :src="r.avatar" />
                  <div>
                    <div>{{ r.nickname }}</div>
                    <el-rate :model-value="r.score" disabled />
                  </div>
                  <span class="time">{{ r.createTime }}</span>
                </div>
                <p>{{ r.content }}</p>
              </div>
              <el-empty v-if="!goods.reviews?.length" description="暂无评价" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="服务保障" name="service">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="正品保障">品牌授权，支持官方验机</el-descriptions-item>
              <el-descriptions-item label="七天无理由">签收后 7 天内支持无理由退货</el-descriptions-item>
              <el-descriptions-item label="运费说明">单笔满 99 元包邮，偏远地区除外</el-descriptions-item>
              <el-descriptions-item label="售后政策">支持官方售后，质保一年</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Star } from '@element-plus/icons-vue';
import { getGoodsDetail, getGoodsReviews } from '@/api/goods';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import { formatPrice } from '@/utils/format';
import { sanitizeHtml } from '@/utils/sanitize';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const loading = ref(false);
const goods = ref({});
const selectedSku = ref(null);
const quantity = ref(1);
const currentImage = ref('');
const activeTab = ref('detail');
// 富文本经白名单消毒后再渲染，防存储型 XSS
const safeDetail = computed(() => sanitizeHtml(goods.value.detail));

onMounted(async () => {
  loading.value = true;
  try {
    const data = await getGoodsDetail(route.params.id);
    goods.value = data;
    if (data.skus?.length) selectedSku.value = data.skus[0];
    currentImage.value = data.mainImage;
    // 评价与商品详情分接口返回
    getGoodsReviews(route.params.id)
      .then((list) => { goods.value.reviews = list; })
      .catch(() => {});
  } finally {
    loading.value = false;
  }
});

function requireLogin() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return false;
  }
  return true;
}

async function handleAddCart() {
  if (!requireLogin()) return;
  await cartStore.add({
    skuId: selectedSku.value.id,
    goodsId: goods.value.id,
    shopId: goods.value.shopId,
    shopName: goods.value.shopId === 2 ? '优选生活馆' : '渡风自营旗舰店',
    title: goods.value.title,
    spec: selectedSku.value.specName,
    image: goods.value.mainImage,
    price: selectedSku.value.price,
    quantity: quantity.value,
    stock: selectedSku.value.stock,
  });
  ElMessage.success('已加入购物车');
}

function handleBuy() {
  if (!requireLogin()) return;
  const item = { goodsId: goods.value.id, skuId: selectedSku.value.id, title: goods.value.title, spec: selectedSku.value.specName, image: goods.value.mainImage, price: selectedSku.value.price, quantity: quantity.value, shopId: goods.value.shopId, shopName: goods.value.shopId === 2 ? '优选生活馆' : '渡风自营旗舰店' };
  sessionStorage.setItem('df_buy_now', JSON.stringify(item));
  router.push('/checkout?type=buyNow');
}

function handleFavorite() {
  if (!requireLogin()) return;
  ElMessage.success('已收藏');
}
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 16px 20px; }
.detail-card { display: flex; gap: 28px; padding: 28px; }
.gallery { width: 400px; flex-shrink: 0; }
.main-img { width: 400px; height: 400px; border-radius: var(--df-radius); background: #f0f4ff; }
.thumbs { display: flex; gap: 8px; margin-top: 10px; }
.thumbs img { width: 64px; height: 64px; object-fit: cover; border-radius: 6px; cursor: pointer; border: 2px solid transparent; }
.thumbs img.active { border-color: var(--df-primary); }
.info { flex: 1; }
.title { font-size: 22px; margin: 0 0 10px; }
.subtitle { color: var(--df-text-secondary); margin-bottom: 16px; }
.price-box { background: #f5f7fa; padding: 16px; border-radius: var(--df-radius-sm); display: flex; align-items: baseline; gap: 16px; }
.cur-price { color: var(--df-danger); font-size: 28px; font-weight: 700; }
.yuan { font-size: 16px; }
.market { color: var(--df-text-secondary); text-decoration: line-through; font-size: 14px; }
.stats { gap: 24px; color: var(--df-text-secondary); font-size: 13px; margin: 14px 0; }
.service { display: flex; gap: 8px; }
.service span { background: #f0f6ff; color: var(--df-primary); font-size: 12px; padding: 3px 10px; border-radius: 4px; }
.field { margin: 18px 0; }
.field label { color: var(--df-text-secondary); font-size: 14px; margin-right: 12px; }
.skus { display: flex; flex-wrap: wrap; gap: 10px; }
.skus span { border: 1px solid #ddd; padding: 6px 14px; border-radius: 4px; cursor: pointer; font-size: 14px; }
.skus span.active { border-color: var(--df-primary); color: var(--df-primary); background: #f0f6ff; }
.quantity { display: flex; align-items: center; }
.actions { display: flex; gap: 12px; margin-top: 24px; }
.detail-tabs { margin-top: 20px; padding: 20px; }
.rich { color: var(--df-text-regular); line-height: 1.8; }
.review-list { max-width: 800px; }
.review-item { padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.review-item .time { margin-left: auto; color: var(--df-text-secondary); font-size: 12px; }
.review-item p { margin: 10px 0 0; }
</style>
