import { defineStore } from 'pinia';
import { getCart, addToCart, updateCart, removeCart, checkCart } from '@/api/cart';

export const useCartStore = defineStore('cart', {
  state: () => ({
    groups: [],
    loading: false,
  }),
  getters: {
    totalCount: (state) => state.groups.reduce((sum, g) => sum + g.items.reduce((n, i) => n + i.quantity, 0), 0),
    checkedItems() {
      return this.groups.flatMap((g) => g.items.filter((i) => i.checked));
    },
    checkedAmount() {
      return this.checkedItems.reduce((sum, i) => sum + i.price * i.quantity, 0);
    },
    checkedCount() {
      return this.checkedItems.reduce((sum, i) => sum + i.quantity, 0);
    },
  },
  actions: {
    async fetchCart() {
      this.loading = true;
      try {
        this.groups = await getCart();
      } finally {
        this.loading = false;
      }
    },
    async add(payload) {
      await addToCart(payload);
      await this.fetchCart();
    },
    async update(id, data) {
      await updateCart(id, data);
      await this.fetchCart();
    },
    async remove(id) {
      await removeCart(id);
      await this.fetchCart();
    },
    async check(payload) {
      // 未传 ids 表示全选：收集所有条目 id（后端仅支持单条勾选，由 api 层并发逐条调用）
      const ids = payload.ids || this.groups.flatMap((g) => g.items.map((i) => i.id));
      await checkCart({ ids, checked: payload.checked });
      await this.fetchCart();
    },
    async clear() {
      this.groups = [];
    },
  },
});
