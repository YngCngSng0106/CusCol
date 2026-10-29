<template>
  <div class="page" :class="{ 'layout-page': !isScoped }">
    <van-nav-bar
      :title="isScoped ? '客户拜访记录' : '拜访记录'"
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
        <van-field v-model="filters.visitDateStart" type="date" label="拜访开始" />
        <van-field v-model="filters.visitDateEnd" type="date" label="拜访结束" />
      </div>
    </ListQueryBar>

    <div class="page-body" :class="{ 'has-fab': isScoped }" style="padding-top: 12px">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          :immediate-check="false"
          finished-text="没有更多拜访记录了"
          @load="loadMore"
        >
          <div v-for="item in visits" :key="item.id" class="list-card">
            <div class="row-between gap-8">
              <div class="flex-1">
                <div class="row gap-8">
                  <span class="list-title ellipsis">{{ item.customerName || '客户' }}</span>
                  <span class="tag">{{ labelOf(VISIT_TYPES, item.visitType) }}</span>
                </div>
                <div class="list-sub clamp-2">{{ item.visitContent }}</div>
              </div>
              <van-icon name="notes-o" size="22" color="#c7d5f5" />
            </div>
            <div class="meta-row" style="margin-top: 9px">
              <span><van-icon name="clock-o" /> {{ formatDateTime(item.visitTime) }}</span>
              <span v-if="item.visitResult" class="ellipsis" style="max-width: 50%">结果：{{ item.visitResult }}</span>
            </div>
            <div class="row gap-8" style="justify-content: flex-end; margin-top: 10px">
              <van-button size="small" round plain type="primary" @click="viewDetail(item)">查看</van-button>
              <van-button size="small" round type="primary" @click="openEdit(item)">修改</van-button>
            </div>
          </div>
        </van-list>
        <van-empty
          v-if="!loading && !visits.length"
          :description="hasActiveFilters ? '没有符合条件的拜访记录' : '暂无拜访记录'"
        >
          <van-button v-if="hasActiveFilters" size="small" round @click="onReset">清空筛选</van-button>
        </van-empty>
      </van-pull-refresh>
    </div>

    <button v-if="isScoped" class="fab" @click="router.push(`/customers/${customerId}/visit/add`)">
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
        <span>修改拜访记录</span>
        <van-icon name="cross" size="20" class="muted" @click="editShow = false" />
      </div>
      <div class="popup-body">
        <van-form @submit="onUpdate">
          <div style="padding: 12px 0">
            <VisitFormFields
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
import ListQueryBar from '../../components/ListQueryBar.vue'
import VisitFormFields from '../../components/VisitFormFields.vue'
import { getVisit, pageVisits, updateVisit } from '../../api/visit'
import { VISIT_TYPES, labelOf } from '../../utils/constants'
import { emptyToNull, formatDateTime, toDateTimeInput } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const customerId = computed(() => (route.params.customerId ? Number(route.params.customerId) : null))
const isScoped = computed(() => Boolean(customerId.value))

const filters = reactive(emptyFilters())
const activeCount = computed(
  () => Object.values(filters).filter((value) => value !== '' && value !== null && value !== undefined).length
)
const hasActiveFilters = computed(() => activeCount.value > 0)
const visits = ref([])
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
    visitDateStart: '',
    visitDateEnd: ''
  }
}

function emptyForm() {
  return {
    customerId: null,
    visitType: 1,
    visitTime: '',
    visitLocation: '',
    visitPurpose: '',
    visitContent: '',
    visitResult: '',
    nextVisitTime: ''
  }
}

async function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const data = await pageVisits({
      pageNo: pageNo.value,
      pageSize: 10,
      customerId: customerId.value || undefined,
      customerName: isScoped.value ? undefined : filters.customerName.trim() || undefined,
      visitDateStart: filters.visitDateStart || undefined,
      visitDateEnd: filters.visitDateEnd || undefined
    })
    if (pageNo.value === 1) {
      visits.value = data.records
    } else {
      visits.value.push(...data.records)
    }
    pageNo.value += 1
    finished.value = visits.value.length >= data.total || data.records.length === 0
  } catch (error) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function resetAndLoad() {
  visits.value = []
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

function viewDetail(item) {
  router.push(`/visits/${item.id}`)
}

async function openEdit(item) {
  try {
    const data = await getVisit(item.id)
    editingId.value = item.id
    Object.assign(editForm, {
      customerId: data.customerId,
      visitType: data.visitType,
      visitTime: toDateTimeInput(data.visitTime),
      visitLocation: data.visitLocation || '',
      visitPurpose: data.visitPurpose || '',
      visitContent: data.visitContent || '',
      visitResult: data.visitResult || '',
      nextVisitTime: toDateTimeInput(data.nextVisitTime)
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
    await updateVisit(editingId.value, {
      ...editForm,
      nextVisitTime: emptyToNull(editForm.nextVisitTime),
      attachmentIds: editAttachments.value.map((item) => item.id).filter(Boolean)
    })
    showSuccessToast('拜访记录已更新')
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
