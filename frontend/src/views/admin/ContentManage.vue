<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>首页轮播</b></template>
          <el-button type="primary" :icon="Plus" size="small" style="margin-bottom:12px" @click="addBanner">新增轮播</el-button>
          <div v-for="b in content.banners" :key="b.id" class="banner-item">
            <img :src="b.image" /><span class="t">{{ b.title }}</span><a @click="removeBanner(b)">删除</a>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>平台公告</b></template>
          <el-button type="primary" :icon="Plus" size="small" style="margin-bottom:12px" @click="noticeVisible = true">新增公告</el-button>
          <div v-for="n in content.notices" :key="n.id" class="notice-item"><div class="t">{{ n.title }}</div><a @click="content.notices = content.notices.filter((x) => x.id !== n.id)">删除</a></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="noticeVisible" title="新增公告" width="480px">
      <el-form :model="noticeForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="noticeForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="noticeForm.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="noticeVisible = false">取消</el-button><el-button type="primary" @click="saveNotice">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getContent } from '@/api/admin';

const content = reactive({ banners: [], notices: [] });
const noticeVisible = ref(false);
const noticeForm = reactive({ title: '', content: '' });
async function fetch() { Object.assign(content, await getContent()); }
function addBanner() { content.banners.push({ id: content.banners.length + 1, title: '新轮播', image: 'data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22400%22 height=%22180%22><rect width=%22400%22 height=%22180%22 fill=%22%233d7eff%22/></svg>', link: '/' }); }
function removeBanner(b) { content.banners = content.banners.filter((x) => x.id !== b.id); }
function saveNotice() { if (!noticeForm.title) return ElMessage.warning('请输入标题'); content.notices.push({ id: content.notices.length + 1, title: noticeForm.title, content: noticeForm.content }); noticeVisible.value = false; noticeForm.title = ''; noticeForm.content = ''; ElMessage.success('已发布'); }
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.banner-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
.banner-item img { width: 90px; height: 48px; object-fit: cover; border-radius: 4px; }
.banner-item .t { flex: 1; }
.banner-item a, .notice-item a { color: var(--df-danger); cursor: pointer; }
.notice-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
</style>
