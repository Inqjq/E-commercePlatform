<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>类目管理</b></template>
      <el-button type="primary" :icon="Plus" style="margin-bottom:16px" @click="openEdit()">新增一级类目</el-button>
      <el-table :data="categories" row-key="id" border default-expand-all>
        <el-table-column prop="name" label="类目名称" width="260" />
        <el-table-column label="级别" width="100"><template #default="{ row }">{{ row.children?.length ? '一级类目' : '二级类目' }}</template></el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button v-if="!row.children?.length" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openEdit(null, row)">新增子类目</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑类目' : '新增类目'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级类目"><el-input :model-value="parentName" disabled /></el-form-item>
        <el-form-item label="类目名称"><el-input v-model="form.name" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/admin';

const categories = ref([]);
const dialogVisible = ref(false);
const parentName = ref('顶级');
const form = reactive({ id: null, parentId: 0, name: '' });

async function fetch() {
  const d = await getCategories();
  categories.value = d.map((c) => ({ ...c, children: c.children?.map((ch) => ({ ...ch, children: null })) }));
}
function openEdit(row, parent) {
  if (row) { Object.assign(form, { id: row.id, parentId: 0, name: row.name }); parentName.value = '顶级'; }
  else { Object.assign(form, { id: null, parentId: parent?.id || 0, name: '' }); parentName.value = parent?.name || '顶级'; }
  dialogVisible.value = true;
}
async function save() {
  if (!form.name.trim()) return ElMessage.warning('请输入类目名称');
  if (form.id) await updateCategory(form.id, form);
  else await createCategory(form);
  ElMessage.success('保存成功');
  dialogVisible.value = false;
  fetch();
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除类目「${row.name}」吗？`, '提示', { type: 'warning' });
  await deleteCategory(row.children?.length ? row.id : row.id);
  ElMessage.success('已删除');
  fetch();
}
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
</style>
