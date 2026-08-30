import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
  state: () => ({
    title: import.meta.env.VITE_APP_TITLE || '渡风电商平台',
    collapsed: false,
    online: true,
  }),
  actions: {
    toggleCollapsed() {
      this.collapsed = !this.collapsed;
    },
  },
});
