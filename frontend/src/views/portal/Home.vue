<template>
  <div class="home-page">
    <div class="container">
      <div class="hero df-flex">
        <el-carousel v-if="home.banners" class="banner" height="360px" :interval="4000" arrow="hover">
          <el-carousel-item v-for="b in home.banners" :key="b.id" @click="$router.push(b.link)">
            <img :src="b.image" :alt="b.title" class="banner-img" />
            <div class="banner-title">{{ b.title }}</div>
          </el-carousel-item>
        </el-carousel>
        <div class="category-side">
          <div class="cat-title">商品分类</div>
          <ul class="cat-list">
            <li v-for="cat in categories" :key="cat.id" @click="$router.push({ path: '/goods/list', query: { categoryId: cat.children?.[0]?.id } })">
              <div class="cat-name">{{ cat.name }}</div>
              <div class="cat-children">
                <span v-for="child in cat.children" :key="child.id" @click.stop="$router.push({ path: '/goods/list', query: { categoryId: child.id } })">{{ child.name }}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <el-alert class="notice" type="info" :closable="false" show-icon>
        <template #title>
          <span class="notice-title">平台公告</span>
        </template>
        <template #default>
          <a v-for="n in home.notices?.slice(0, 3)" :key="n.id" class="notice-item">{{ n.title }}</a>
        </template>
      </el-alert>

      <section class="section">
        <div class="section-head df-flex-between">
          <h3>热销商品</h3>
          <a @click="$router.push('/goods/list?sortBy=sales')">更多 ></a>
        </div>
        <div class="goods-grid">
          <GoodsCard v-for="g in home.hotGoods" :key="g.id" :goods="g" tag="热销" />
        </div>
      </section>

      <section class="section">
        <div class="section-head df-flex-between">
          <h3>新品推荐</h3>
          <a @click="$router.push('/goods/list')">更多 ></a>
        </div>
        <div class="goods-grid">
          <GoodsCard v-for="g in home.newGoods" :key="g.id" :goods="g" tag="新品" />
        </div>
      </section>

      <section class="section service">
        <div class="svc-item"><el-icon :size="26"><CircleCheck /></el-icon><div><h4>正品保障</h4><p>品牌授权 假一赔十</p></div></div>
        <div class="svc-item"><el-icon :size="26"><Van /></el-icon><div><h4>极速发货</h4><p>多地仓就近发货</p></div></div>
        <div class="svc-item"><el-icon :size="26"><Refresh /></el-icon><div><h4>七天退换</h4><p>无忧售后</p></div></div>
        <div class="svc-item"><el-icon :size="26"><Service /></el-icon><div><h4>专属客服</h4><p>7×24 小时</p></div></div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { CircleCheck, Van, Refresh, Service } from '@element-plus/icons-vue';
import GoodsCard from '@/components/goods/GoodsCard.vue';
import { getHome } from '@/api/home';
import { getCategories } from '@/api/goods';

const home = reactive({ banners: [], notices: [], hotGoods: [], newGoods: [] });
const categories = ref([]);

onMounted(async () => {
  try {
    const data = await getHome();
    Object.assign(home, data);
  } catch {}
  try {
    categories.value = await getCategories();
  } catch {}
});
</script>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 16px 20px; }
.hero { gap: 16px; align-items: stretch; }
.banner { flex: 1; border-radius: var(--df-radius); overflow: hidden; cursor: pointer; }
.banner-img { width: 100%; height: 100%; object-fit: cover; }
.banner-title { position: absolute; bottom: 16px; left: 16px; color: #fff; font-size: 22px; font-weight: 700; text-shadow: 0 2px 6px rgba(0,0,0,.4); }
.category-side { width: 220px; background: #fff; border-radius: var(--df-radius); box-shadow: var(--df-shadow); padding: 8px 0; flex-shrink: 0; }
.cat-title { font-size: 16px; font-weight: 700; padding: 8px 16px; }
.cat-list { list-style: none; padding: 0; margin: 0; }
.cat-list li { padding: 10px 16px; cursor: pointer; border-bottom: 1px solid #f0f0f0; }
.cat-list li:hover { background: #f7f9ff; }
.cat-name { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.cat-children { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-children span { font-size: 12px; color: var(--df-text-secondary); }
.cat-children span:hover { color: var(--df-primary); }
.notice { margin-top: 16px; }
.notice-title { font-weight: 600; margin-right: 12px; }
.notice-item { margin-right: 24px; font-size: 13px; color: var(--df-text-regular); }
.section { margin-top: 32px; }
.section-head h3 { margin: 0; font-size: 20px; font-weight: 700; }
.section-head a { color: var(--df-text-secondary); font-size: 14px; cursor: pointer; }
.section-head a:hover { color: var(--df-primary); }
.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-top: 16px; }
.service { display: flex; justify-content: space-between; background: #fff; border-radius: var(--df-radius); padding: 20px 24px; box-shadow: var(--df-shadow); }
.svc-item { display: flex; align-items: center; gap: 12px; color: var(--df-primary); }
.svc-item h4 { margin: 0; font-size: 15px; color: var(--df-text-main); }
.svc-item p { margin: 2px 0 0; font-size: 12px; color: var(--df-text-secondary); }
</style>
