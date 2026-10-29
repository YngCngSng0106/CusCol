<template>
  <div class="page">
    <van-nav-bar title="合同详情" left-arrow @click-left="router.back()" />
    <div v-if="contract" class="page-body">
      <div class="card">
        <div class="row-between gap-8">
          <div class="flex-1">
            <div class="tiny muted">{{ contract.contractNo }}</div>
            <h2 class="detail-title" style="font-size: 18px; margin-top: 4px">{{ contract.contractName }}</h2>
            <div class="row gap-8" style="margin-top: 9px; flex-wrap: wrap">
              <span class="tag green">{{ labelOf(CONTRACT_STATUSES, contract.contractStatus) }}</span>
              <span class="tag orange">{{ labelOf(PAYMENT_STATUSES, contract.paymentStatus) }}</span>
            </div>
          </div>
          <van-icon name="orders-o" size="32" color="#dbe5ff" />
        </div>
        <div class="info-list">
          <div class="info-row"><span class="info-label">客户</span><span class="info-value">{{ contract.customerName }}</span></div>
          <div class="info-row"><span class="info-label">合同金额</span><span class="info-value" style="font-weight: 600">{{ formatMoney(contract.contractAmount) }}</span></div>
          <div class="info-row"><span class="info-label">已回款</span><span class="info-value">{{ formatMoney(contract.paidAmount) }}</span></div>
          <div class="info-row"><span class="info-label">签订日期</span><span class="info-value">{{ formatDate(contract.signDate) }}</span></div>
          <div class="info-row"><span class="info-label">有效期</span><span class="info-value">{{ formatDate(contract.startDate) }} ~ {{ formatDate(contract.endDate) }}</span></div>
          <div class="info-row"><span class="info-label">备注</span><span class="info-value">{{ contract.remark || '-' }}</span></div>
        </div>
      </div>

      <div class="card section">
        <div class="section-title" style="margin-bottom: 8px">合同附件</div>
        <AttachmentList :attachments="contract.attachments || []" />
      </div>
    </div>
    <div v-else class="page-body">
      <div class="skeleton-block"></div>
      <div class="skeleton-block"></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AttachmentList from '../../components/AttachmentList.vue'
import { getContract } from '../../api/contract'
import { CONTRACT_STATUSES, PAYMENT_STATUSES, labelOf } from '../../utils/constants'
import { formatDate, formatMoney } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const contract = ref(null)

onMounted(async () => {
  try {
    contract.value = await getContract(route.params.id)
  } catch (error) {
    // 错误提示已由请求层处理
  }
})
</script>
