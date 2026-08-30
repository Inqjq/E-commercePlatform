<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="brand"><img src="/favicon.svg" /><span>渡风电商平台</span></div>
      <h2>注册新账号</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleRegister">
        <el-form-item prop="phone"><el-input v-model="form.phone" placeholder="手机号" size="large" :prefix-icon="Iphone" /></el-form-item>
        <el-form-item prop="code">
          <div class="sms-row">
            <el-input v-model="form.code" placeholder="短信验证码" size="large" :prefix-icon="Key" />
            <el-button size="large" :disabled="counting > 0" @click="sendCode">{{ counting > 0 ? `${counting}s` : '获取验证码' }}</el-button>
          </div>
        </el-form-item>
        <el-form-item prop="username"><el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" /></el-form-item>
        <el-form-item prop="password"><el-input v-model="form.password" type="password" placeholder="设置密码" show-password size="large" :prefix-icon="Lock" /></el-form-item>
        <el-form-item prop="confirm"><el-input v-model="form.confirm" type="password" placeholder="确认密码" show-password size="large" :prefix-icon="Lock" /></el-form-item>
        <el-checkbox v-model="form.agree">我已阅读并同意《用户协议》《隐私政策》</el-checkbox>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleRegister">注 册</el-button>
      </el-form>
      <div class="link">已有账号？<a @click="$router.push('/login')">去登录</a></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Iphone, Key } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { sendSms } from '@/api/auth';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref();
const loading = ref(false);
const counting = ref(0);
const form = reactive({ phone: '', code: '', username: '', password: '', confirm: '', agree: false });
const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirm: [{ required: true, message: '请再次输入密码', trigger: 'blur' }],
};

async function handleRegister() {
  if (!form.agree) return ElMessage.warning('请先同意用户协议');
  if (form.password !== form.confirm) return ElMessage.warning('两次密码不一致');
  loading.value = true;
  try {
    // 契约见后端 RegisterRequest：手机号绑定必须携带 verifyCode
    await userStore.register({
      username: form.username,
      password: form.password,
      phone: form.phone,
      verifyCode: form.code,
    });
    ElMessage.success('注册成功，已自动登录');
    router.push('/');
  } finally {
    loading.value = false;
  }
}

function sendCode() {
  if (!form.phone) return ElMessage.warning('请先输入手机号');
  sendSms({ phone: form.phone, scene: 'register' });
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
.submit { width: 100%; margin-top: 8px; }
.sms-row { display: flex; gap: 10px; width: 100%; }
.link { text-align: center; margin-top: 16px; font-size: 14px; }
.link a { color: var(--df-primary); cursor: pointer; }
</style>
