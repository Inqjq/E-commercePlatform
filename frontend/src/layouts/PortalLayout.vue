<template>
  <div class="portal-layout">
    <header class="portal-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <img src="/favicon.svg" alt="logo" class="logo-img" />
          <span class="logo-text">渡风电商</span>
        </router-link>

        <nav class="nav">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/goods/list" class="nav-item">全部商品</router-link>
          <router-link to="/goods/list?sortBy=sales" class="nav-item">热销</router-link>
          <router-link to="/coupon" class="nav-item">优惠券</router-link>
        </nav>

        <div class="search-box">
          <el-input v-model="keyword" placeholder="搜索商品 / 品牌" clearable @keyup.enter="doSearch">
            <template #append>
              <el-button :icon="Search" @click="doSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="actions">
          <router-link to="/cart" class="action">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
            <span>购物车</span>
          </router-link>
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="onUserCommand">
              <span class="user-entry">
                <el-avatar :size="30" :src="userStore.avatar" />
                <span class="user-name">{{ userStore.nickname }}</span>
                <el-icon><CaretBottom /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="portal-main">
      <router-view />
    </main>

    <footer class="portal-footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4>购物指南</h4>
          <p>购物流程</p><p>会员介绍</p><p>常见问题</p>
        </div>
        <div class="footer-col">
          <h4>配送方式</h4>
          <p>上门自提</p><p>快递配送</p><p>运费说明</p>
        </div>
        <div class="footer-col">
          <h4>售后服务</h4>
          <p>退换货政策</p><p>价格保护</p><p>取消订单</p>
        </div>
        <div class="footer-col">
          <h4>关于我们</h4>
          <p>公司简介</p><p>联系客服</p><p>加入我们</p>
        </div>
      </div>
      <div class="footer-copy">© 2026 渡风电商平台 · 仅用于技术演示</div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, ShoppingCart, CaretBottom } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();
const keyword = ref('');

function doSearch() {
  router.push({ path: '/goods/list', query: { keyword: keyword.value } });
}

function onUserCommand(cmd) {
  if (cmd === 'profile') router.push('/profile');
  else if (cmd === 'orders') router.push('/orders');
  else if (cmd === 'logout') {
    ElMessage.success('已退出登录');
    userStore.logout().then(() => {
      // 同步清空购物车状态，避免角标残留上一账号数据
      cartStore.clear();
      router.push('/');
    });
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) cartStore.fetchCart().catch(() => {});
});
</script>

<style scoped>
.portal-layout { min-height: 100%; display: flex; flex-direction: column; }
.portal-header { position: sticky; top: 0; z-index: 100; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: var(--df-header-height); display: flex; align-items: center; gap: 24px; }
.logo { display: flex; align-items: center; gap: 8px; color: var(--df-primary); font-weight: 700; font-size: 20px; }
.logo-img { width: 32px; height: 32px; }
.nav { display: flex; gap: 18px; }
.nav-item { color: var(--df-text-regular); font-size: 15px; padding: 6px 0; }
.nav-item.router-link-exact-active { color: var(--df-primary); font-weight: 600; border-bottom: 2px solid var(--df-primary); }
.search-box { flex: 1; max-width: 420px; }
.actions { display: flex; align-items: center; gap: 16px; }
.action { display: flex; flex-direction: column; align-items: center; color: var(--df-text-regular); font-size: 12px; }
.user-entry { display: flex; align-items: center; gap: 6px; cursor: pointer; outline: none; }
.user-name { font-size: 14px; color: var(--df-text-main); }
.portal-main { flex: 1; }
.portal-footer { background: #2b2f36; color: #a8abb2; margin-top: 40px; }
.footer-inner { max-width: 1200px; margin: 0 auto; padding: 32px 20px; display: flex; gap: 48px; }
.footer-col h4 { color: #fff; margin-bottom: 12px; }
.footer-col p { margin: 6px 0; font-size: 13px; cursor: pointer; }
.footer-col p:hover { color: #fff; }
.footer-copy { text-align: center; padding: 16px; border-top: 1px solid #3a3f47; font-size: 12px; }
</style>
