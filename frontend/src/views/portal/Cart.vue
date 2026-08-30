<template>
  <div class="container" v-loading="cartStore.loading">
    <h2 class="page-title">我的购物车</h2>
    <template v-if="cartStore.groups.length">
      <div v-for="group in cartStore.groups" :key="group.shopId" class="group-card df-card">
        <div class="group-head">
          <el-checkbox :model-value="groupAllChecked(group)" @change="(v) => toggleGroup(group, v)">全选</el-checkbox>
          <span class="shop-name"><el-icon><Shop /></el-icon>{{ group.shopName }}</span>
        </div>
        <div v-for="item in group.items" :key="item.id" class="item">
          <el-checkbox :model-value="item.checked" @change="(v) => cartStore.check({ ids: [item.id], checked: v })" />
          <img :src="item.image" class="item-img" @click="$router.push(`/goods/${item.goodsId}`)" />
          <div class="item-info">
            <div class="title">{{ item.title }}</div>
            <div class="spec">{{ item.spec }}</div>
          </div>
          <div class="price">¥{{ formatPrice(item.price) }}</div>
          <el-input-number :model-value="item.quantity" :min="1" :max="item.stock" size="small" @change="(v) => cartStore.update(item.id, { quantity: v })" />
          <div class="subtotal">¥{{ formatPrice(item.price * item.quantity) }}</div>
          <el-button text type="danger" @click="cartStore.remove(item.id)">删除</el-button>
        </div>
        <div class="group-total">小计：¥{{ formatPrice(group.items.filter(i => i.checked).reduce((s, i) => s + i.price * i.quantity, 0)) }}</div>
      </div>

      <div class="settle-bar df-card df-flex-between">
        <el-checkbox :model-value="allChecked" @change="toggleAll">全选</el-checkbox>
        <div class="right">
          <span>已选 <b>{{ cartStore.checkedCount }}</b> 件商品</span>
          <span class="total">合计：<span class="price">¥{{ formatPrice(cartStore.checkedAmount) }}</span></span>
          <el-button type="danger" size="large" :disabled="!cartStore.checkedCount" @click="goCheckout">去结算</el-button>
        </div>
      </div>
    </template>
    <el-empty v-else description="购物车还是空的，去逛逛吧">
      <el-button type="primary" @click="$router.push('/goods/list')">去购物</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Shop } from '@element-plus/icons-vue';
import { useCartStore } from '@/stores/cart';
import { formatPrice } from '@/utils/format';

const router = useRouter();
const cartStore = useCartStore();

const allChecked = computed(() => {
  const items = cartStore.groups.flatMap((g) => g.items);
  return items.length > 0 && items.every((i) => i.checked);
});

function groupAllChecked(group) {
  return group.items.length > 0 && group.items.every((i) => i.checked);
}

function toggleGroup(group, checked) {
  cartStore.check({ ids: group.items.map((i) => i.id), checked });
}

function toggleAll(checked) {
  cartStore.check({ checked });
}

function goCheckout() {
  router.push('/checkout');
}

onMounted(() => cartStore.fetchCart());
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.page-title { margin: 0 0 16px; }
.group-card { margin-bottom: 16px; padding: 12px 16px; }
.group-head { display: flex; align-items: center; gap: 16px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }
.shop-name { display: flex; align-items: center; gap: 6px; font-weight: 600; }
.item { display: flex; align-items: center; gap: 16px; padding: 16px 0; border-bottom: 1px solid #f7f7f7; }
.item-img { width: 72px; height: 72px; object-fit: cover; border-radius: 6px; cursor: pointer; }
.item-info { flex: 1; }
.item-info .title { font-size: 14px; }
.item-info .spec { font-size: 12px; color: var(--df-text-secondary); margin-top: 4px; }
.price { width: 90px; }
.subtotal { width: 90px; color: var(--df-danger); font-weight: 600; }
.group-total { text-align: right; padding: 12px 0 4px; color: var(--df-text-secondary); font-size: 13px; }
.settle-bar { position: sticky; bottom: 12px; padding: 12px 20px; }
.right { display: flex; align-items: center; gap: 20px; }
.right b { color: var(--df-danger); }
.total .price { color: var(--df-danger); font-size: 22px; font-weight: 700; }
</style>
