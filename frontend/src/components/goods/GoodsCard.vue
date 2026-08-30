<template>
  <div class="goods-card" @click="$router.push(`/goods/${goods.id}`)">
    <div class="image">
      <img :src="goods.mainImage" :alt="goods.title" />
      <span v-if="tag" class="tag">{{ tag }}</span>
    </div>
    <div class="body">
      <div class="title" :title="goods.title">{{ goods.title }}</div>
      <div class="subtitle">{{ goods.subtitle || goods.service?.join(' · ') || '正品保障' }}</div>
      <div class="meta">
        <span class="price"><span class="symbol">¥</span>{{ formatPrice(goods.price) }}</span>
        <span class="sales">已售 {{ goods.sales }} 件</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatPrice } from '@/utils/format';

defineProps({
  goods: { type: Object, required: true },
  tag: { type: String, default: '' },
});
</script>

<style scoped>
.goods-card { background: #fff; border-radius: var(--df-radius); overflow: hidden; cursor: pointer; box-shadow: var(--df-shadow); transition: transform .2s, box-shadow .2s; }
.goods-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.1); }
.image { position: relative; height: 200px; background: #f0f4ff; }
.image img { width: 100%; height: 100%; object-fit: cover; }
.tag { position: absolute; top: 8px; left: 8px; background: var(--df-danger); color: #fff; font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.body { padding: 12px; }
.title { font-size: 14px; color: var(--df-text-main); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.subtitle { font-size: 12px; color: var(--df-text-secondary); margin-top: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.price { color: var(--df-danger); font-weight: 700; font-size: 18px; }
.symbol { font-size: 12px; }
.sales { font-size: 12px; color: var(--df-text-secondary); }
</style>
