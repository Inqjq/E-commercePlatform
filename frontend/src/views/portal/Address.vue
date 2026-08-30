<template>
  <div class="container">
    <h2 class="page-title">收货地址</h2>
    <div class="addr-list">
      <div v-for="a in list" :key="a.id" class="addr-card df-card">
        <div class="info">
          <div class="receiver">{{ a.receiver }} <span class="phone">{{ maskPhone(a.phone) }}</span><el-tag v-if="a.isDefault" size="small" type="primary">默认</el-tag></div>
          <div class="detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detail }}</div>
        </div>
        <div class="ops">
          <el-button v-if="!a.isDefault" text type="primary" @click="setDefault(a)">设为默认</el-button>
          <el-button text @click="openEdit(a)">编辑</el-button>
          <el-button text type="danger" @click="remove(a)">删除</el-button>
        </div>
      </div>
      <div class="add-btn" @click="openEdit()"><el-icon><Plus /></el-icon> 新增收货地址</div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="收货人" prop="receiver"><el-input v-model="form.receiver" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader v-model="form.region" :options="regionOptions" :props="{ value: 'name', label: 'name' }" clearable />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail"><el-input v-model="form.detail" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="设为默认"><el-switch v-model="form.isDefault" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address';
import { maskPhone } from '@/utils/format';

const list = ref([]);
const dialogVisible = ref(false);
const formRef = ref();
const emptyForm = { id: null, receiver: '', phone: '', region: [], detail: '', isDefault: false };
const form = reactive({ ...emptyForm });
const rules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确手机号', trigger: 'blur' }],
  region: [{ required: true, message: '请选择省市区', trigger: 'change' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
};
const regionOptions = [
  { name: '广东省', children: [{ name: '深圳市', children: [{ name: '南山区' }, { name: '福田区' }] }] },
  { name: '上海市', children: [{ name: '上海市', children: [{ name: '浦东新区' }, { name: '徐汇区' }] }] },
  { name: '北京市', children: [{ name: '北京市', children: [{ name: '海淀区' }, { name: '朝阳区' }] }] },
];

async function fetchList() {
  list.value = await getAddressList();
}

function openEdit(a) {
  Object.assign(form, a ? { ...a, region: [a.province, a.city, a.district] } : { ...emptyForm });
  dialogVisible.value = true;
}

function save() {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    // 后端 AddressRequest.isDefault 为 Integer（1/0），el-switch 绑定的是布尔值，需转换
    const payload = { receiver: form.receiver, phone: form.phone, province: form.region[0], city: form.region[1], district: form.region[2], detail: form.detail, isDefault: form.isDefault ? 1 : 0 };
    if (form.id) await updateAddress(form.id, payload);
    else await addAddress(payload);
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetchList();
  });
}

async function remove(a) {
  await ElMessageBox.confirm('确认删除该地址吗？', '提示', { type: 'warning' });
  await deleteAddress(a.id);
  ElMessage.success('已删除');
  fetchList();
}

async function setDefault(a) {
  await setDefaultAddress(a.id);
  ElMessage.success('已设为默认');
  fetchList();
}

onMounted(fetchList);
</script>

<style scoped>
.container { max-width: 900px; margin: 0 auto; padding: 20px; }
.page-title { margin: 0 0 16px; }
.addr-card { display: flex; align-items: center; justify-content: space-between; padding: 20px; margin-bottom: 12px; }
.receiver { font-weight: 600; margin-bottom: 6px; }
.phone { color: var(--df-text-secondary); margin: 0 8px; }
.detail { color: var(--df-text-regular); }
.ops { display: flex; }
.add-btn { display: flex; align-items: center; justify-content: center; gap: 6px; border: 1px dashed #dcdfe6; border-radius: 8px; padding: 20px; cursor: pointer; color: var(--df-text-secondary); }
.add-btn:hover { color: var(--df-primary); border-color: var(--df-primary); }
</style>
