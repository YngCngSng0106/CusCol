<template>
  <div class="page">
    <van-nav-bar title="添加拜访记录" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <div v-if="customerName" class="card" style="margin-bottom: 12px; padding: 12px 14px">
        <div class="tiny muted">正在为客户记录拜访</div>
        <div class="list-title" style="margin-top: 3px">{{ customerName }}</div>
      </div>
      <van-form @submit="onSubmit">
        <VisitFormFields v-model:attachments="attachments" :form="form" :biz-id="0" />
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
import VisitFormFields from '../../components/VisitFormFields.vue'
import { getCustomer } from '../../api/customer'
import { createVisit } from '../../api/visit'
import { emptyToNull, toDateTimeInput } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const customerId = Number(route.params.customerId)
const customerName = ref('')
const submitting = ref(false)
const attachments = ref([])
const form = reactive({
  customerId,
  visitType: 1,
  visitTime: toDateTimeInput(new Date()),
  visitLocation: '',
  visitPurpose: '',
  visitContent: '',
  visitResult: '',
  nextVisitTime: ''
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
    await createVisit({
      ...form,
      nextVisitTime: emptyToNull(form.nextVisitTime),
      attachmentIds: attachments.value.map((item) => item.id).filter(Boolean)
    })
    showSuccessToast('拜访记录添加成功')
    router.back()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadCustomer)
</script>
