<template>
  <el-container class="backoffice">
    <el-aside :width="appStore.collapsed ? '64px' : '220px'" class="aside">
      <div class="brand">
        <img src="/favicon.svg" alt="logo" />
        <span v-show="!appStore.collapsed">渡风商家端</span>
      </div>
      <el-menu :default-active="$route.path" :collapse="appStore.collapsed" router class="menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="appStore.toggleCollapsed"><Fold v-if="!appStore.collapsed" /><Expand v-else /></el-icon>
          <span class="page-title">{{ $route.meta.title }}</span>
        </div>
        <div class="right">
          <el-dropdown @command="onCommand">
            <span class="user"><el-avatar :size="28" :src="userStore.avatar" /><span>{{ userStore.nickname }}</span><el-icon><CaretBottom /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="portal">返回商城</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { Fold, Expand, CaretBottom, Select, Goods, Box, List, Service, Present } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAppStore } from '@/stores/app';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const appStore = useAppStore();
const userStore = useUserStore();

const menus = [
  { path: '/merchant', title: '工作台', icon: Select },
  { path: '/merchant/goods', title: '商品管理', icon: Goods },
  { path: '/merchant/stock', title: '库存管理', icon: Box },
  { path: '/merchant/orders', title: '订单管理', icon: List },
  { path: '/merchant/after-sales', title: '售后管理', icon: Service },
  { path: '/merchant/marketing', title: '营销管理', icon: Present },
];

function onCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout().then(() => { ElMessage.success('已退出登录'); router.push('/login'); });
  } else if (cmd === 'portal') {
    router.push('/');
  }
}
</script>

<style scoped>
.backoffice { height: 100%; }
.aside { background: #1f2731; transition: width .2s; overflow-x: hidden; }
.brand { height: 60px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #fff; font-weight: 700; }
.brand img { width: 28px; height: 28px; }
.menu { border-right: none; background: transparent; --el-menu-bg-color: transparent; --el-menu-text-color: #b8c0cc; --el-menu-active-color: #fff; --el-menu-hover-bg-color: #2a3340; }
.menu :deep(.el-menu-item.is-active) { background: var(--df-primary); }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.left, .right { display: flex; align-items: center; gap: 12px; }
.collapse-btn { font-size: 20px; cursor: pointer; }
.page-title { font-size: 16px; font-weight: 600; }
.user { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.main { background: var(--df-bg-page); }
</style>
