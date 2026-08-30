<template>
  <div class="container" v-loading="loading">
    <h2 class="page-title">申请售后</h2>
    <div v-if="order.orderNo" class="form-card df-card">
      <div class="order-info">
        <span class="order-no">订单号：{{ order.orderNo }}</span>
        <el-tag>{{ ORDER_STATUS_MAP[order.status]?.label }}</el-tag>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="售后类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="REFUND">仅退款</el-radio>
            <el-radio value="REFUND_RETURN">退货退款</el-radio>
            <el-radio value="EXCHANGE">换货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :max="order.payAmount" :precision="2" />
          <span class="tip">最高可退 ¥{{ formatPrice(order.payAmount) }}</span>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择">
            <el-option label="不想要了" value="不想要了" />
            <el-option label="尺寸/颜色不合适" value="尺寸/颜色不合适" />
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品与描述不符" value="商品与描述不符" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="form.desc" type="textarea" :rows="4" placeholder="请补充说明，便于商家/平台处理" />
        </el-form-item>
        <el-form-item label="上传凭证">
          <el-upload action="#" :auto-upload="false" list-type="picture-card" :on-change="onFileChange">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-empty v-else description="订单不存在" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getOrderDetail } from '@/api/order';
import { createReview } from '@/api/review';
import { ORDER_STATUS_MAP } from '@/utils/constants';
import { formatPrice } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const order = ref({});
const formRef = ref();
const form = reactive({ type: 'REFUND', amount: 0, reason: '', desc: '' });
const rules = {
  type: [{ required: true, message: '请选择售后类型', trigger: 'change' }],
  amount: [{ required: true, message: '请填写退款金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择申请原因', trigger: 'change' }],
};

onMounted(async () => {
  loading.value = true;
  try {
    order.value = await getOrderDetail(route.params.orderNo);
    form.amount = order.value.payAmount;
  } finally {
    loading.value = false;
  }
});

function onFileChange() {}
function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    await createReview({ type: 'after_sale', orderNo: order.value.orderNo, ...form });
    ElMessage.success('售后申请已提交');
    router.push('/orders');
  });
}
</script>

<style scoped>
.container { max-width: 900px; margin: 0 auto; padding: 20px; }
.page-title { margin: 0 0 16px; }
.form-card { padding: 24px; }
.order-info { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.tip { margin-left: 12px; color: var(--df-text-secondary); font-size: 13px; }
</style>
