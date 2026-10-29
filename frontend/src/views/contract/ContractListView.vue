<template>
  <div class="page" :class="{ 'layout-page': !isScoped }">
    <van-nav-bar
      :title="isScoped ? '客户合同' : '合同'"
      :left-arrow="isScoped"
      @click-left="router.back()"
    />

    <ListQueryBar :active-count="activeCount" @search="resetAndLoad" @reset="onReset">
      <div class="query-grid">
        <van-field
          v-if="!isScoped"
          v-model="filters.customerName"
          label="客户名称"
          placeholder="请输入客户名称"
          clearable
        />
        <van-field v-model="filters.signDateStart" type="date" label="签约开始" />
        <van-field v-model="filters.signDateEnd" type="date" label="签约结束" />
        <PickerField
          v-model="filters.paymentStatus"
          label="回款状态"
          placeholder="请选择回款状态"
          :options="PAYMENT_FILTER_OPTIONS"
        />
      </div>
    </ListQueryBar>

    <div class="page-body" :class="{ 'has-fab': isScoped }" style="padding-top: 12px">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          :immediate-check="false"
          finished-text="没有更多合同了"
          @load="loadMore"
        >
          <div v-for="item in contracts" :key="item.id" class="list-card">
            <div class="row-between gap-8">
              <div class="flex-1">
                <div class="list-title ellipsis">{{ item.contractName }}</div>
                <div class="meta-row" style="margin-top: 7px">
                  <span>{{ item.contractNo }}</span>
                  <span>{{ item.customerName }}</span>
                </div>
              </div>
              <van-icon name="orders-o" size="22" color="#c7d5f5" />
            </div>
            <div class="row-between gap-8" style="margin-top: 10px">
              <div class="row gap-8">
                <span class="tag green">{{ labelOf(CONTRACT_STATUSES, item.contractStatus) }}</span>
                <span class="tag orange">{{ labelOf(PAYMENT_STATUSES, item.paymentStatus) }}</span>
              </div>
              <span style="font-weight: 600; color: var(--cc-primary)">{{ formatMoney(item.contractAmount) }}</span>
            </div>
            <div class="meta-row" style="margin-top: 8px">
              <span>签订：{{ formatDate(item.signDate) }}</span>
              <span>到期：{{ formatDate(item.endDate) }}</span>
            </div>
            <div class="row gap-8" style="justify-content: flex-end; margin-top: 10px">
              <van-button size="small" round plain type="primary" @click="router.push(`/contracts/${item.id}`)">查看</van-button>
              <van-button size="small" round type="primary" @click="openEdit(item)">修改</van-button>
            </div>
          </div>
        </van-list>
        <van-empty
          v-if="!loading && !contracts.length"
          :description="hasActiveFilters ? '没有符合条件的合同' : '暂无合同'"
        >
          <van-button v-if="hasActiveFilters" size="small" round @click="onReset">清空筛选</van-button>
        </van-empty>
      </van-pull-refresh>
    </div>

    <button v-if="isScoped" class="fab" @click="router.push(`/customers/${customerId}/contract/add`)">
      <van-icon name="plus" size="22" />
    </button>

    <van-popup
      v-model:show="editShow"
      position="bottom"
      round
      teleport="body"
      class="app-popup popup-sheet"
      overlay-class="app-overlay"
      :style="{ height: '92%' }"
    >
      <div class="popup-head">
        <span>修改合同</span>
        <van-icon name="cross" size="20" class="muted" @click="editShow = false" />
      </div>
      <div class="popup-body">
        <van-form @submit="onUpdate">
          <div style="padding: 12px 0">
            <ContractFormFields
              v-model:attachments="editAttachments"
              :form="editForm"
              :biz-id="editingId"
            />
          </div>
          <div class="submit-bar">
            <van-button block round type="primary" native-type="submit" :loading="submitting">
              保存修改
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import ContractFormFields from '../../components/ContractFormFields.vue'
import ListQueryBar from '../../components/ListQueryBar.vue'
import PickerField from '../../components/PickerField.vue'
import { getContract, pageContracts, updateContract } from '../../api/contract'
import { CONTRACT_STATUSES, PAYMENT_STATUSES, labelOf } from '../../utils/constants'
import { emptyToNull, formatDate, formatMoney, toDateInput } from '../../utils/format'

const PAYMENT_FILTER_OPTIONS = [{ text: '全部', value: null }, ...PAYMENT_STATUSES]

const route = useRoute()
const router = useRouter()
const customerId = computed(() => (route.params.customerId ? Number(route.params.customerId) : null))
const isScoped = computed(() => Boolean(customerId.value))

const filters = reactive(emptyFilters())
const activeCount = computed(
  () => Object.values(filters).filter((value) => value !== '' && value !== null && value !== undefined).length
)
const hasActiveFilters = computed(() => activeCount.value > 0)
const contracts = ref([])
const pageNo = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const editShow = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const editAttachments = ref([])
const editForm = reactive(emptyForm())

function emptyFilters() {
  return {
    customerName: '',
    signDateStart: '',
    signDateEnd: '',
    paymentStatus: null
  }
}

function emptyForm() {
  return {
    customerId: null,
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
  }
}

async function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const data = await pageContracts({
      pageNo: pageNo.value,
      pageSize: 10,
      customerId: customerId.value || undefined,
      customerName: isScoped.value ? undefined : filters.customerName.trim() || undefined,
      signDateStart: filters.signDateStart || undefined,
      signDateEnd: filters.signDateEnd || undefined,
      paymentStatus: filters.paymentStatus ?? undefined
    })
    if (pageNo.value === 1) {
      contracts.value = data.records
    } else {
      contracts.value.push(...data.records)
    }
    pageNo.value += 1
    finished.value = contracts.value.length >= data.total || data.records.length === 0
  } catch (error) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function resetAndLoad() {
  contracts.value = []
  pageNo.value = 1
  finished.value = false
  loading.value = false
  nextTick(() => loadMore())
}

async function onRefresh() {
  resetAndLoad()
}

function onReset() {
  Object.assign(filters, emptyFilters())
  resetAndLoad()
}

async function openEdit(item) {
  try {
    const data = await getContract(item.id)
    editingId.value = item.id
    Object.assign(editForm, {
      customerId: data.customerId,
      contractNo: data.contractNo,
      contractName: data.contractName,
      contractAmount: data.contractAmount,
      signDate: toDateInput(data.signDate),
      startDate: toDateInput(data.startDate),
      endDate: toDateInput(data.endDate),
      contractStatus: data.contractStatus,
      paymentStatus: data.paymentStatus,
      paidAmount: data.paidAmount,
      remark: data.remark || ''
    })
    editAttachments.value = data.attachments || []
    editShow.value = true
  } catch (error) {
    // 错误提示已由请求层处理
  }
}

async function onUpdate() {
  submitting.value = true
  try {
    await updateContract(editingId.value, {
      ...editForm,
      contractAmount: emptyToNull(editForm.contractAmount),
      paidAmount: editForm.paidAmount === '' ? 0 : editForm.paidAmount,
      signDate: emptyToNull(editForm.signDate),
      startDate: emptyToNull(editForm.startDate),
      endDate: emptyToNull(editForm.endDate),
      attachmentIds: editAttachments.value.map((item) => item.id).filter(Boolean)
    })
    showSuccessToast('合同已更新')
    editShow.value = false
    resetAndLoad()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}

watch(
  () => route.params.customerId,
  () => {
    Object.assign(filters, emptyFilters())
    resetAndLoad()
  }
)

onMounted(loadMore)
</script>
