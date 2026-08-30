<template>
  <div class="page">
    <div class="layout">
      <el-card shadow="never" class="left">
        <template #header><div class="df-flex-between"><b>角色列表</b><el-button type="primary" size="small" :icon="Plus" @click="openRole()">新增</el-button></div></template>
        <div v-for="r in roles" :key="r.id" class="role-item" :class="{ active: currentRole?.id === r.id }" @click="select(r)">
          <div>{{ r.name }}</div><div class="code">{{ r.code }}</div>
        </div>
      </el-card>
      <el-card shadow="never" class="right">
        <template #header><b>{{ currentRole ? `权限配置：${currentRole.name}` : '请选择角色' }}</b></template>
        <template v-if="currentRole">
          <el-tree ref="treeRef" :data="treeData" show-checkbox node-key="id" :default-checked-keys="currentRole.menus" :props="{ label: 'name', children: 'children' }" />
          <div class="save"><el-button type="primary" @click="save">保存权限</el-button></div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getRoles, getPermissions, createRole, updateRole } from '@/api/admin';

const roles = ref([]);
const permissions = ref([]);
const currentRole = ref(null);
const treeRef = ref();
const treeData = computed(() => buildTree(permissions.value));

function buildTree(list) {
  const map = {};
  const roots = [];
  list.forEach((p) => { map[p.id] = { ...p, children: [] }; });
  list.forEach((p) => { if (p.parentId) map[p.parentId]?.children?.push(map[p.id]); else roots.push(map[p.id]); });
  return roots;
}

async function fetch() { roles.value = await getRoles(); permissions.value = await getPermissions(); currentRole.value = roles.value[0] || null; }
function select(r) { currentRole.value = r; }
function openRole() {
  const name = window.prompt('请输入角色名称');
  if (!name) return;
  roles.value.push({ id: roles.value.length + 1, name, code: 'NEW_ROLE', menus: [], description: '' });
  ElMessage.success('已新增角色');
}
function save() {
  const checked = treeRef.value.getCheckedKeys().concat(treeRef.value.getHalfCheckedKeys());
  currentRole.value.menus = checked;
  ElMessage.success('权限已保存');
}
onMounted(fetch);
</script>

<style scoped>
.page { padding: 20px; }
.layout { display: grid; grid-template-columns: 300px 1fr; gap: 16px; }
.role-item { padding: 12px; border-radius: 6px; cursor: pointer; border: 1px solid #f0f0f0; margin-bottom: 8px; }
.role-item.active { border-color: var(--df-primary); background: #f0f6ff; }
.role-item .code { font-size: 12px; color: var(--df-text-secondary); }
.save { margin-top: 16px; }
</style>
