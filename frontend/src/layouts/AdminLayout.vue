<template>
  <el-container class="backoffice">
    <el-aside :width="appStore.collapsed ? '64px' : '220px'" class="aside">
      <div class="brand">
        <img src="/favicon.svg" alt="logo" />
        <span v-show="!appStore.collapsed" class="brand-text admin">渡风平台管理中心</span>
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
import { Fold, Expand, CaretBottom, DataLine, User, Shop, Goods, Menu, Present, Picture, Key, Document } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAppStore } from '@/stores/app';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const appStore = useAppStore();
const userStore = useUserStore();

const menus = [
  { path: '/admin', title: '数据看板', icon: DataLine },
  { path: '/admin/user', title: '用户管理', icon: User },
  { path: '/admin/merchant', title: '商家管理', icon: Shop },
  { path: '/admin/goods', title: '商品审核', icon: Goods },
  { path: '/admin/category', title: '类目管理', icon: Menu },
  { path: '/admin/brand', title: '品牌管理', icon: Present },
  { path: '/admin/marketing', title: '营销管理', icon: Present },
  { path: '/admin/content', title: '内容管理', icon: Picture },
  { path: '/admin/role', title: '权限管理', icon: Key },
  { path: '/admin/logs', title: '日志审计', icon: Document },
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
.aside { background: #1d2740; transition: width .2s; overflow-x: hidden; }
.brand { height: 60px; display: flex; align-items: center; justify-content: center; gap: 10px; color: #fff; font-weight: 700; }
.brand img { width: 28px; height: 28px; }
.brand-text.admin { color: #7db3ff; }
.menu { border-right: none; background: transparent; --el-menu-bg-color: transparent; --el-menu-text-color: #b8c0cc; --el-menu-active-color: #fff; --el-menu-hover-bg-color: #2a3340; }
.menu :deep(.el-menu-item.is-active) { background: #3d7eff; }
.header { background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.left, .right { display: flex; align-items: center; gap: 12px; }
.collapse-btn { font-size: 20px; cursor: pointer; }
.page-title { font-size: 16px; font-weight: 600; }
.user { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.main { background: var(--df-bg-page); }
</style>
