<template>
  <div class="auth-page">
    <div class="auth-card" :class="'id-' + identity.toLowerCase()">
      <div class="brand"><img src="/favicon.svg" /><span>{{ brandText }}</span></div>
      <h2>{{ loginTitle }}</h2>
      <div v-if="end === 'USER'" class="identity">
        <el-radio-group v-model="identity" class="identity-group">
          <el-radio-button value="USER">商城用户</el-radio-button>
          <el-radio-button value="MERCHANT">商家</el-radio-button>
          <el-radio-button value="ADMIN">平台管理员</el-radio-button>
        </el-radio-group>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账号密码" name="password">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
            <el-form-item prop="account"><el-input v-model="form.account" placeholder="手机号 / 邮箱 / 用户名" size="large" :prefix-icon="User" /></el-form-item>
            <el-form-item prop="password"><el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" :prefix-icon="Lock" /></el-form-item>
            <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="短信登录" name="sms">
          <el-form :model="form" label-position="top" @keyup.enter="handleLogin">
            <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" size="large" :prefix-icon="Iphone" /></el-form-item>
            <el-form-item label="验证码">
              <div class="sms-row">
                <el-input v-model="form.code" placeholder="6 位验证码" size="large" :prefix-icon="Key" />
                <el-button size="large" :disabled="counting > 0" @click="sendCode">{{ counting > 0 ? `${counting}s` : '获取验证码' }}</el-button>
              </div>
            </el-form-item>
            <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="links">
        <a @click="$router.push('/register')">注册新账号</a>
        <a>忘记密码？</a>
      </div>
      <p v-if="isDev" class="trial">演示环境：任意账号 / 密码即可登录</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Iphone, Key } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { sendSms } from '@/api/auth';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const isDev = import.meta.env.DEV;
const formRef = ref();
const activeTab = ref('password');
const identity = ref(route.meta?.end || 'USER');
const loginTitle = computed(() => ({ USER: '欢迎登录 · 商城', MERCHANT: '商家中心登录', ADMIN: '平台管理中心登录' }[identity.value] || '欢迎登录'));
const brandText = computed(() => ({ USER: '渡风商城', MERCHANT: '渡风商家中心', ADMIN: '渡风平台管理中心' }[identity.value] || '渡风电商平台'));
const loading = ref(false);
const counting = ref(0);
const form = reactive({ account: '', password: '', phone: '', code: '' });
const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function handleLogin() {
  if (activeTab.value === 'password') {
    if (!form.account || !form.password) return ElMessage.warning('请输入账号和密码');
  } else {
    if (!form.phone || !form.code) return ElMessage.warning('请输入手机号和验证码');
  }
  loading.value = true;
  try {
    await userStore.login({ account: form.account, password: form.password, phone: form.phone, code: form.code });
    ElMessage.success('登录成功');
    const roles = userStore.userInfo?.roles || [];
    const redirect = route.query.redirect;
    // 只能跳本角色所属区域，否则回各自系统首页
    let allowed = false;
    if (redirect) {
      allowed = roles.includes('ADMIN') ? redirect.startsWith('/admin')
        : roles.includes('MERCHANT') ? redirect.startsWith('/merchant')
        : true;
    }
    if (allowed) {
      router.push(redirect);
    } else {
      router.push(roles.includes('ADMIN') ? '/admin' : roles.includes('MERCHANT') ? '/merchant' : '/');
    }
  } finally {
    loading.value = false;
  }
}

function sendCode() {
  if (!form.phone) return ElMessage.warning('请先输入手机号');
  sendSms({ phone: form.phone, scene: 'login' });
  ElMessage.success('验证码已发送');
  counting.value = 60;
  const timer = setInterval(() => {
    counting.value -= 1;
    if (counting.value <= 0) clearInterval(timer);
  }, 1000);
}
</script>

<style scoped>
.auth-page { min-height: 100%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg,#3d7eff22,#f5f7fa); }
.auth-card { width: 420px; background: #fff; border-radius: 12px; padding: 36px; box-shadow: var(--df-shadow); }
.auth-card.id-merchant { border-top: 4px solid #e8a33d; }
.auth-card.id-admin { border-top: 4px solid #3d7eff; }
.auth-card.id-user { border-top: 4px solid #3d7eff; }
.identity { margin: 0 0 18px; }
.identity-group { display: flex; }
.identity-group :deep(.el-radio-button__inner) { font-size: 14px; }
.brand { display: flex; align-items: center; gap: 10px; justify-content: center; color: var(--df-primary); font-weight: 700; font-size: 18px; margin-bottom: 8px; }
.brand img { width: 30px; }
h2 { text-align: center; margin: 8px 0 24px; font-weight: 600; }
.submit { width: 100%; margin-top: 4px; }
.sms-row { display: flex; gap: 10px; width: 100%; }
.links { display: flex; justify-content: space-between; margin-top: 16px; font-size: 14px; }
.links a { cursor: pointer; color: var(--df-primary); }
.trial { text-align: center; font-size: 12px; color: var(--df-text-secondary); margin-top: 16px; }
</style>
