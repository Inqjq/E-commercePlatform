<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><b>{{ isEdit ? '编辑商品' : '发布商品' }}</b></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:760px">
        <el-form-item label="商品名称" prop="title"><el-input v-model="form.title" placeholder="请输入商品名称" /></el-form-item>
        <el-form-item label="类目" prop="categoryId">
          <el-cascader v-model="categoryPath" :options="categories" :props="{ value: 'id', label: 'name', children: 'children' }" @change="onCategoryChange" />
        </el-form-item>
        <el-form-item label="品牌" prop="brandId"><el-select v-model="form.brandId" placeholder="选择品牌" clearable><el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" /></el-select></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.subtitle" placeholder="一句话卖点（选填）" /></el-form-item>
        <el-form-item label="商品规格" prop="skus">
          <div class="sku-list">
            <div v-for="(s, i) in form.skus" :key="i" class="sku-row">
              <el-input v-model="s.specText" placeholder="规格名，如 曜金黑" style="width:170px" />
              <el-input-number v-model="s.price" :min="0.01" :precision="2" controls-position="right" placeholder="价格" style="width:140px" />
              <el-input-number v-model="s.stock" :min="0" :precision="0" controls-position="right" placeholder="库存" style="width:130px" />
              <el-button text type="danger" :disabled="form.skus.length <= 1" @click="form.skus.splice(i, 1)">删除</el-button>
            </div>
            <el-button text type="primary" :icon="Plus" @click="addSku">添加规格</el-button>
          </div>
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input v-model="form.detail" type="textarea" :rows="6" placeholder="请输入商品详情（支持 HTML）" />
        </el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="save">保存并提交审核</el-button><el-button @click="$router.back()">取消</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getCategories, getBrands } from '@/api/goods';
import { createMerchantGoods, updateMerchantGoods, getMerchantGoods, getMerchantGoodsDetail } from '@/api/merchant';

const route = useRoute();
const router = useRouter();
const formRef = ref();
const isEdit = computed(() => !!route.params.id);
const saving = ref(false);
const categories = ref([]);
const brands = ref([]);
const categoryPath = ref([]);
const form = reactive({ id: null, title: '', subtitle: '', categoryId: null, brandId: null, detail: '', mainImage: '', skus: [emptySku()] });
const rules = {
  title: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择类目', trigger: 'change' }],
};

function emptySku() {
  return { id: undefined, specText: '', price: 1, stock: 0 };
}
function addSku() {
  form.skus.push(emptySku());
}
function onCategoryChange(a) {
  form.categoryId = a[a.length - 1];
}

async function save() {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    if (form.skus.some((s) => !s.specText || !(s.price > 0))) {
      return ElMessage.warning('请完整填写每个规格的名称和价格');
    }
    const payload = {
      title: form.title,
      subtitle: form.subtitle || undefined,
      categoryId: form.categoryId,
      brandId: form.brandId || undefined,
      mainImage: form.mainImage || `https://picsum.photos/seed/DF${Date.now()}/600/600`,
      detail: form.detail || undefined,
      skus: form.skus.map((s) => ({
        id: s.id || undefined,
        specText: s.specText,
        specJson: JSON.stringify({ 规格: s.specText }),
        price: s.price,
        stock: s.stock,
      })),
    };
    saving.value = true;
    try {
      if (isEdit.value) await updateMerchantGoods(form.id, payload);
      else await createMerchantGoods(payload);
      ElMessage.success('保存成功，已提交平台审核');
      router.push('/merchant/goods');
    } finally {
      saving.value = false;
    }
  });
}

onMounted(async () => {
  categories.value = await getCategories();
  brands.value = await getBrands();
  if (isEdit.value) {
    // 编辑：加载商品与全部 SKU（SKU 带 id，保存时原地更新不破坏订单引用）
    const data = await getMerchantGoodsDetail(route.params.id);
    const goods = data?.goods || {};
    form.id = goods.id;
    form.title = goods.title;
    form.subtitle = goods.subtitle;
    form.detail = goods.detail;
    form.mainImage = goods.mainImage;
    form.brandId = goods.brandId;
    if (goods.categoryId) {
      form.categoryId = goods.categoryId;
      categoryPath.value = [goods.categoryId];
    }
    form.skus = (data?.skus || []).length
      ? data.skus.map((s) => ({ id: s.id, specText: s.specText, price: Number(s.price), stock: s.stock ?? 0 }))
      : [emptySku()];
  }
});
</script>

<style scoped>
.page { padding: 20px; }
.sku-list { width: 100%; }
.sku-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
</style>
