<template>
  <div class="container" v-loading="loading">
    <h2 class="page-title">确认订单</h2>

    <div class="section df-card">
      <div class="head"><el-icon><Location /></el-icon> 收货地址</div>
      <div class="addresses">
        <div v-for="a in addresses" :key="a.id" class="addr" :class="{ active: selectedAddressId === a.id }" @click="selectedAddressId = a.id">
          <div class="receiver">{{ a.receiver }} <span>{{ a.phone }}</span></div>
          <div class="detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
          <el-tag v-if="a.isDefault" size="small" type="primary">默认</el-tag>
        </div>
        <div class="addr add" @click="$router.push('/profile/address')"><el-icon><Plus /></el-icon> 新增地址</div>
      </div>
    </div>

    <div class="section df-card">
      <div class="head"><el-icon><Goods /></el-icon> 商品清单</div>
      <div v-for="(item, i) in items" :key="i" class="goods-item">
        <img :src="item.image" />
        <div class="info">
          <div class="title">{{ item.title }}</div>
          <div class="spec">{{ item.spec }}</div>
        </div>
        <div class="price">¥{{ formatPrice(item.price) }}</div>
        <div class="qty">x{{ item.quantity }}</div>
        <div class="subtotal">¥{{ formatPrice(item.price * item.quantity) }}</div>
      </div>
    </div>

    <div class="section df-card">
      <div class="head"><el-icon><Wallet /></el-icon> 配送与留言</div>
      <div class="form-row">
        <span>配送方式</span>
        <el-radio-group v-model="delivery"><el-radio value="express">快递配送</el-radio><el-radio value="pickup">上门自提</el-radio></el-radio-group>
      </div>
      <div class="form-row">
        <span>订单备注</span>
        <el-input v-model="remark" placeholder="选填，给商家留言" maxlength="200" />
      </div>
    </div>

    <div class="summary df-card">
      <div class="line"><span>商品金额</span><span>¥{{ formatPrice(subtotal) }}</span></div>
      <div class="line"><span>运费</span><span>免运费</span></div>
      <div class="line total"><span>应付总额</span><span class="price">¥{{ formatPrice(subtotal) }}</span></div>
      <div class="submit-row">
        <span class="count">共 <b>{{ totalCount }}</b> 件</span>
        <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Location, Goods, Wallet, Plus } from '@element-plus/icons-vue';
import { getAddressList } from '@/api/address';
import { createOrder } from '@/api/order';
import { useCartStore } from '@/stores/cart';
import { formatPrice } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const loading = ref(false);
const submitting = ref(false);
const addresses = ref([]);
const selectedAddressId = ref(null);
const delivery = ref('express');
const remark = ref('');

// 订单项：购物车选中项 或 立即购买（价格与优惠一律以后端结算为准，此处仅展示）
const items = ref([]);

const subtotal = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0));
const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0));

onMounted(async () => {
  loading.value = true;
  try {
    addresses.value = await getAddressList();
    selectedAddressId.value = addresses.value.find((a) => a.isDefault)?.id || addresses.value[0]?.id || null;
    if (route.query.type === 'buyNow') {
      const buyNow = JSON.parse(sessionStorage.getItem('df_buy_now') || 'null');
      if (buyNow) items.value = [buyNow];
    } else {
      await cartStore.fetchCart();
      items.value = cartStore.checkedItems.map((i) => ({ goodsId: i.goodsId, skuId: i.skuId, title: i.title, spec: i.spec, image: i.image, price: i.price, quantity: i.quantity, shopId: i.shopId, shopName: i.shopName }));
    }
  } finally {
    loading.value = false;
  }
});

async function submitOrder() {
  if (!items.value.length) return ElMessage.warning('没有可结算的商品');
  if (!selectedAddressId.value) return ElMessage.warning('请选择收货地址');
  submitting.value = true;
  try {
    const isBuyNow = route.query.type === 'buyNow';
    // 契约见后端 OrderCreateRequest：金额由后端按 SKU 重新计算，前端不传价格
    const result = await createOrder({
      addressId: selectedAddressId.value,
      fromCart: !isBuyNow,
      items: isBuyNow
        ? items.value.map((i) => ({ skuId: i.skuId, quantity: i.quantity }))
        : undefined,
      remark: remark.value || undefined,
    });
    sessionStorage.removeItem('df_buy_now');
    if (!isBuyNow) await cartStore.fetchCart();
    ElMessage.success('订单创建成功');
    // 按店铺拆单时只对首笔收银，其余在订单列表继续支付
    router.replace(`/pay/${result.orderNo}`);
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.page-title { margin: 0 0 16px; }
.section { margin-bottom: 16px; padding: 16px 20px; }
.head { display: flex; align-items: center; gap: 8px; font-weight: 600; margin-bottom: 16px; }
.addresses { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.addr { border: 1px solid #e4e7ed; border-radius: var(--df-radius-sm); padding: 12px; cursor: pointer; position: relative; }
.addr.active { border-color: var(--df-primary); background: #f0f6ff; }
.addr .receiver { font-weight: 600; margin-bottom: 6px; }
.addr .receiver span { color: var(--df-text-secondary); font-weight: 400; }
.addr .detail { font-size: 13px; color: var(--df-text-regular); }
.addr .el-tag { position: absolute; top: 10px; right: 10px; }
.addr.add { display: flex; align-items: center; justify-content: center; gap: 6px; color: var(--df-primary); border-style: dashed; }
.goods-item { display: flex; align-items: center; gap: 16px; padding: 12px 0; border-bottom: 1px solid #f7f7f7; }
.goods-item img { width: 60px; height: 60px; border-radius: 6px; object-fit: cover; }
.goods-item .info { flex: 1; }
.goods-item .spec { font-size: 12px; color: var(--df-text-secondary); }
.goods-item .price, .goods-item .qty { width: 80px; }
.goods-item .subtotal { width: 100px; font-weight: 600; }
.form-row { display: flex; align-items: center; margin-bottom: 14px; }
.form-row > span { width: 90px; color: var(--df-text-secondary); }
.summary { padding: 16px 20px; }
.line { display: flex; justify-content: space-between; margin: 10px 0; color: var(--df-text-regular); }
.line.total { font-size: 16px; font-weight: 600; color: var(--df-text-main); }
.line.total .price { color: var(--df-danger); font-size: 24px; font-weight: 700; }
.submit-row { display: flex; align-items: center; justify-content: flex-end; gap: 20px; margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.submit-row .count b { color: var(--df-danger); }
</style>
