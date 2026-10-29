<template>
  <div class="page">
    <van-nav-bar title="修改客户" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <van-form v-if="ready" @submit="onSubmit">
        <CustomerFormFields :form="form" />
        <div class="submit-bar">
          <van-button block round type="primary" native-type="submit" :loading="submitting">保 存</van-button>
        </div>
      </van-form>
      <div v-else>
        <div class="skeleton-block"></div>
        <div class="skeleton-block"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import CustomerFormFields from '../../components/CustomerFormFields.vue'
import { getCustomer, updateCustomer } from '../../api/customer'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const ready = ref(false)
const submitting = ref(false)
const form = reactive({
  customerName: '',
  customerLevel: null,
  industry: '',
  city: '',
  address: '',
  contactName: '',
  contactPhone: '',
  source: '',
  status: 1,
  remark: ''
})

async function load() {
  try {
    const data = await getCustomer(id)
    const customer = data.customer
    Object.assign(form, {
      customerName: customer.customerName,
      customerLevel: customer.customerLevel,
      industry: customer.industry,
      city: customer.city,
      address: customer.address,
      contactName: customer.contactName,
      contactPhone: customer.contactPhone,
      source: customer.source,
      status: customer.status,
      remark: customer.remark
    })
    ready.value = true
  } catch (error) {
    router.back()
  }
}

async function onSubmit() {
  submitting.value = true
  try {
    await updateCustomer(id, { ...form })
    showSuccessToast('客户信息已更新')
    router.back()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
