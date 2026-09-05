<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand"><img src="/favicon.svg" /><span>渡风电商平台</span></div>
      <h2>欢迎登录</h2>
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
import { reactive, ref } from 'vue';
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
    // 有显式 redirect（如被登录拦截的结算页）优先；否则按角色进入各自系统
    const redirect = route.query.redirect;
    if (redirect) {
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
.brand { display: flex; align-items: center; gap: 10px; justify-content: center; color: var(--df-primary); font-weight: 700; font-size: 18px; margin-bottom: 8px; }
.brand img { width: 30px; }
h2 { text-align: center; margin: 8px 0 24px; font-weight: 600; }
.submit { width: 100%; margin-top: 4px; }
.sms-row { display: flex; gap: 10px; width: 100%; }
.links { display: flex; justify-content: space-between; margin-top: 16px; font-size: 14px; }
.links a { cursor: pointer; color: var(--df-primary); }
.trial { text-align: center; font-size: 12px; color: var(--df-text-secondary); margin-top: 16px; }
</style>
