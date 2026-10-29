<template>
  <div class="page">
    <van-nav-bar title="添加合同" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <div v-if="customerName" class="card" style="margin-bottom: 12px; padding: 12px 14px">
        <div class="tiny muted">合同所属客户</div>
        <div class="list-title" style="margin-top: 3px">{{ customerName }}</div>
      </div>
      <van-form @submit="onSubmit">
        <ContractFormFields v-model:attachments="attachments" :form="form" :biz-id="0" />
        <div class="submit-bar">
          <van-button block round type="primary" native-type="submit" :loading="submitting">
            确认添加
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import ContractFormFields from '../../components/ContractFormFields.vue'
import { getCustomer } from '../../api/customer'
import { createContract } from '../../api/contract'
import { emptyToNull } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const customerId = Number(route.params.customerId)
const customerName = ref('')
const submitting = ref(false)
const attachments = ref([])
const form = reactive({
  customerId,
  contractNo: '',
  contractName: '',
  contractAmount: '',
  signDate: '',
  startDate: '',
  endDate: '',
  contractStatus: 1,
  paymentStatus: 0,
  paidAmount: '',
  remark: ''
})

async function loadCustomer() {
  try {
    const data = await getCustomer(customerId)
    customerName.value = data.customer.customerName
  } catch (error) {
    router.back()
  }
}

async function onSubmit() {
  submitting.value = true
  try {
    await createContract({
      ...form,
      contractAmount: emptyToNull(form.contractAmount),
      paidAmount: form.paidAmount === '' ? 0 : form.paidAmount,
      signDate: emptyToNull(form.signDate),
      startDate: emptyToNull(form.startDate),
      endDate: emptyToNull(form.endDate),
      attachmentIds: attachments.value.map((item) => item.id).filter(Boolean)
    })
    showSuccessToast('合同添加成功')
    router.back()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadCustomer)
</script>
