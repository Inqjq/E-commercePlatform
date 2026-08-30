<template>
  <div class="container">
    <div class="profile-card df-card">
      <el-avatar :size="72" :src="userStore.avatar" class="avatar" />
      <div class="meta">
        <div class="name">{{ userStore.nickname }}</div>
        <div class="phone">{{ userStore.userInfo?.phone }}</div>
        <div class="intro">{{ userStore.userInfo?.intro || '这个人很懒，什么都没写' }}</div>
      </div>
      <div class="stat">
        <div><b>{{ favorites.length }}</b><span>收藏</span></div>
        <div><b>{{ coupons.length }}</b><span>优惠券</span></div>
        <div><b>{{ orders.length }}</b><span>订单</span></div>
        <div><b>{{ userStore.userInfo?.score || 0 }}</b><span>积分</span></div>
      </div>
    </div>

    <el-tabs v-model="active" class="tabs-card" type="border-card">
      <el-tab-pane label="基本信息" name="info">
        <el-form :model="editForm" label-width="90px" style="max-width:480px">
          <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
          <el-form-item label="性别"><el-radio-group v-model="editForm.gender"><el-radio :value="1">男</el-radio><el-radio :value="0">女</el-radio></el-radio-group></el-form-item>
          <el-form-item label="个人简介"><el-input v-model="editForm.intro" type="textarea" :rows="3" /></el-form-item>
          <el-form-item><el-button type="primary" @click="saveProfile">保存</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="我的订单" name="orders">
        <div class="order-shortcuts">
          <a v-for="o in orderShortcuts" :key="o.key" @click="$router.push({ path: '/orders', query: { status: o.key } })">{{ o.label }}</a>
        </div>
        <div class="recent-orders">
          <div v-for="o in orders.slice(0, 3)" :key="o.id" class="ro-item df-flex-between" @click="$router.push(`/orders/${o.orderNo}`)">
            <div>订单 {{ o.orderNo }} · {{ ORDER_STATUS_MAP[o.status]?.label }}</div>
            <span class="red">¥{{ formatPrice(o.totalAmount) }}</span>
          </div>
          <el-empty v-if="!orders.length" description="暂无订单" :image-size="80" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="收货地址" name="address">
        <el-button type="primary" @click="$router.push('/profile/address')">管理地址</el-button>
      </el-tab-pane>
      <el-tab-pane label="优惠券" name="coupon">
        <el-button type="primary" @click="$router.push('/coupon')">查看优惠券</el-button>
      </el-tab-pane>
      <el-tab-pane label="我的收藏" name="favorite">
        <div class="fav-grid">
          <div v-for="f in favorites" :key="f.id" class="fav-item" @click="$router.push(`/goods/${f.goodsId}`)">
            <img :src="f.image" />
            <div class="title">{{ f.title }}</div>
            <div class="price">¥{{ formatPrice(f.price) }}</div>
          </div>
          <el-empty v-if="!favorites.length" description="暂无收藏" :image-size="80" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="消息中心" name="message">
        <div v-for="m in messages" :key="m.id" class="msg" :class="{ unread: !m.read }">
          <div class="msg-head df-flex-between"><span class="msg-title">{{ m.title }}</span><span class="msg-time">{{ m.createTime }}</span></div>
          <p>{{ m.content }}</p>
        </div>
        <el-empty v-if="!messages.length" description="暂无消息" :image-size="80" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { updateProfile, getFavorites, getMessages } from '@/api/auth';
import { getOrders } from '@/api/order';
import { getMyCoupons } from '@/api/coupon';
import { ORDER_STATUS_MAP } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const router = useRouter();
const userStore = useUserStore();
const active = ref('info');
const orders = ref([]);
const favorites = ref([]);
const coupons = ref([]);
const messages = ref([]);
const editForm = reactive({ nickname: '', gender: 1, intro: '' });
const orderShortcuts = [
  { key: 'PENDING_PAYMENT', label: '待付款' },
  { key: 'PENDING_SHIP', label: '待发货' },
  { key: 'SHIPPED', label: '待收货' },
  { key: 'COMPLETED', label: '已完成' },
];

onMounted(async () => {
  const u = userStore.userInfo || {};
  editForm.nickname = u.nickname || '';
  editForm.gender = u.gender ?? 1;
  editForm.intro = u.intro || '';
  try { orders.value = await getOrders({}); } catch {}
  try { favorites.value = await getFavorites(); } catch {}
  try { coupons.value = await getMyCoupons(); } catch {}
  try { messages.value = await getMessages(); } catch {}
});

async function saveProfile() {
  await userStore.fetchProfile().catch(() => {});
  userStore.updateProfile({ ...editForm });
  await updateProfile({ ...editForm });
  ElMessage.success('已保存');
}
</script>

<style scoped>
.container { max-width: 1080px; margin: 0 auto; padding: 20px; }
.profile-card { display: flex; align-items: center; gap: 24px; padding: 24px 28px; margin-bottom: 16px; }
.avatar { background: var(--df-primary); }
.meta { flex: 1; }
.name { font-size: 20px; font-weight: 700; }
.phone { color: var(--df-text-secondary); margin-top: 4px; }
.intro { color: var(--df-text-regular); margin-top: 4px; }
.stat { display: flex; gap: 32px; }
.stat > div { text-align: center; }
.stat b { display: block; font-size: 22px; }
.stat span { font-size: 12px; color: var(--df-text-secondary); }
.tabs-card { background: #fff; border-radius: 8px; }
.order-shortcuts { display: flex; gap: 12px; margin-bottom: 16px; }
.order-shortcuts a { background: #f5f7fa; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
.order-shortcuts a:hover { color: var(--df-primary); }
.recent-orders .ro-item { padding: 12px; border: 1px solid #f0f0f0; border-radius: 6px; margin-bottom: 10px; cursor: pointer; }
.red { color: var(--df-danger); font-weight: 600; }
.fav-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
.fav-item { cursor: pointer; }
.fav-item img { width: 100%; height: 120px; object-fit: cover; border-radius: 6px; }
.fav-item .title { font-size: 13px; margin-top: 6px; }
.fav-item .price { color: var(--df-danger); }
.msg { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.msg.unread .msg-title { font-weight: 700; }
.msg-title { font-size: 15px; }
.msg-time { color: var(--df-text-secondary); font-size: 12px; }
.msg p { margin: 6px 0 0; color: var(--df-text-regular); }
</style>
