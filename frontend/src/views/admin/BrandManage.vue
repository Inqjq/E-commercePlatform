<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>品牌管理</b></template>
      <el-button type="primary" :icon="Plus" style="margin-bottom:16px" @click="dialogVisible = true">新增品牌</el-button>
      <el-table :data="brands" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="品牌名称" width="200" />
        <el-table-column prop="logo" label="Logo" width="100"><template #default="{ row }"><img :src="row.logo" style="width:40px;height:40px;border-radius:6px" /></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default><el-button size="small" @click="dialogVisible = true">编辑</el-button><el-button size="small" type="danger">删除</el-button></template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增品牌" width="440px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="品牌名称"><el-input v-model="form.name" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getBrands } from '@/api/admin';

const brands = ref([]);
const dialogVisible = ref(false);
const form = reactive({ name: '' });
async function fetch() { brands.value = await getBrands(); }
function save() {
  if (!form.name) return ElMessage.warning('请输入品牌名称');
  brands.value.push({ id: brands.value.length + 1, name: form.name, logo: '' });
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  form.name = '';
}
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
</style>
