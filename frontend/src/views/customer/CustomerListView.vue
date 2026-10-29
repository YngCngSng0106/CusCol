<template>
  <div class="page layout-page">
    <van-nav-bar title="客户" />
    <ListQueryBar :active-count="activeCount" @search="resetAndLoad" @reset="onReset">
      <div class="query-grid">
        <van-field v-model="filters.customerName" label="客户名称" placeholder="请输入客户名称" clearable />
        <van-field v-model="filters.contactName" label="联系人" placeholder="请输入联系人" clearable />
        <van-field
          v-model="filters.contactPhone"
          type="tel"
          label="联系电话"
          placeholder="请输入联系电话"
          clearable
        />
      </div>
    </ListQueryBar>

    <div class="page-body has-fab" style="padding-top: 12px">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          :immediate-check="false"
          finished-text="没有更多客户了"
          @load="loadMore"
        >
          <div
            v-for="item in customers"
            :key="item.id"
            class="list-card"
            @click="openDetail(item)"
          >
            <div class="row-between gap-8">
              <div class="flex-1">
                <div class="row gap-8">
                  <h3 class="list-title ellipsis">{{ item.customerName }}</h3>
                  <span v-if="item.izValid === 0" class="tag red">已屏蔽</span>
                </div>
                <div class="meta-row" style="margin-top: 7px">
                  <span v-if="item.customerLevel" class="tag">
                    {{ labelOf(CUSTOMER_LEVELS, item.customerLevel) }}
                  </span>
                  <span class="tag green">{{ labelOf(CUSTOMER_STATUSES, item.status) }}</span>
                  <span v-if="item.city">{{ item.city }}</span>
                </div>
              </div>
              <van-icon name="arrow" color="#c4c9d4" />
            </div>
            <div class="divider" style="margin: 10px 0"></div>
            <div class="meta-row">
              <span v-if="item.contactName">
                <van-icon name="contact-o" /> {{ item.contactName }}
              </span>
              <span v-if="item.contactPhone">
                <van-icon name="phone-o" /> {{ item.contactPhone }}
              </span>
              <span>
                <van-icon name="clock-o" />
                最近拜访：{{ item.lastVisitTime ? formatDate(item.lastVisitTime) : '暂无' }}
              </span>
            </div>
          </div>
        </van-list>
        <van-empty
          v-if="!loading && !customers.length"
          :description="hasActiveFilters ? '没有符合条件的客户' : '暂无客户'"
        >
          <van-button v-if="!hasActiveFilters" size="small" round type="primary" @click="showAdd = true">
            添加第一个客户
          </van-button>
          <van-button v-else size="small" round @click="onReset">清空筛选</van-button>
        </van-empty>
      </van-pull-refresh>
    </div>

    <button class="fab" @click="openAdd">
      <van-icon name="plus" size="22" />
    </button>

    <van-popup
      v-model:show="showAdd"
      position="bottom"
      round
      teleport="body"
      class="app-popup popup-sheet"
      overlay-class="app-overlay"
      :style="{ height: '90%' }"
    >
      <div class="popup-head">
        <span>添加客户</span>
        <van-icon name="cross" size="20" class="muted" @click="showAdd = false" />
      </div>
      <div class="popup-body">
        <van-form @submit="onAdd">
          <div style="padding: 12px 0">
            <CustomerFormFields :form="addForm" />
          </div>
          <div class="submit-bar">
            <van-button block round type="primary" native-type="submit" :loading="submitting">
              确认添加
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import CustomerFormFields from '../../components/CustomerFormFields.vue'
import ListQueryBar from '../../components/ListQueryBar.vue'
import { createCustomer, pageCustomers } from '../../api/customer'
import { CUSTOMER_LEVELS, CUSTOMER_STATUSES, labelOf } from '../../utils/constants'
import { formatDate } from '../../utils/format'

const router = useRouter()

const filters = reactive(emptyFilters())
const activeCount = computed(() => Object.values(filters).filter((value) => String(value ?? '').trim()).length)
const hasActiveFilters = computed(() => activeCount.value > 0)
const customers = ref([])
const pageNo = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const showAdd = ref(false)
const submitting = ref(false)
const addForm = reactive(emptyForm())

function emptyFilters() {
  return {
    customerName: '',
    contactName: '',
    contactPhone: ''
  }
}

function emptyForm() {
  return {
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
  }
}

async function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const data = await pageCustomers({
      pageNo: pageNo.value,
      pageSize: 10,
      customerName: filters.customerName.trim() || undefined,
      contactName: filters.contactName.trim() || undefined,
      contactPhone: filters.contactPhone.trim() || undefined
    })
    if (pageNo.value === 1) {
      customers.value = data.records
    } else {
      customers.value.push(...data.records)
    }
    pageNo.value += 1
    finished.value = customers.value.length >= data.total || data.records.length === 0
  } catch (error) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function resetAndLoad() {
  customers.value = []
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

function openAdd() {
  Object.assign(addForm, emptyForm())
  showAdd.value = true
}

function openDetail(item) {
  router.push(`/customers/${item.id}`)
}

async function onAdd() {
  submitting.value = true
  try {
    await createCustomer({ ...addForm })
    showSuccessToast('客户添加成功')
    showAdd.value = false
    resetAndLoad()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}

onMounted(loadMore)
</script>
