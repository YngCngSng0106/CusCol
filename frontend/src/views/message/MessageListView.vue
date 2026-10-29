<template>
  <div class="page layout-page">
    <van-nav-bar title="最近消息">
      <template #right>
        <span v-if="messages.some((item) => !item.izRead)" class="nav-right-text" @click="onReadAll">
          全部已读
        </span>
      </template>
    </van-nav-bar>

    <div class="filter-tabs">
      <button class="filter-chip" :class="{ active: activeTab === 'all' }" @click="switchTab('all')">
        全部消息
      </button>
      <button class="filter-chip" :class="{ active: activeTab === 'unread' }" @click="switchTab('unread')">
        未读消息
      </button>
    </div>

    <div class="page-body" style="padding-top: 0">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          :immediate-check="false"
          finished-text="没有更多消息了"
          @load="loadMore"
        >
          <div
            v-for="item in messages"
            :key="item.id"
            class="message-card"
            @click="openMessage(item)"
          >
            <div class="message-icon">
              <van-icon name="bell" />
            </div>
            <div class="flex-1">
              <div class="row-between gap-8">
                <div class="message-title flex-1">
                  {{ item.title }}
                </div>
                <span v-if="!item.izRead" class="badge-dot"></span>
              </div>
              <div class="message-content">{{ item.content }}</div>
              <div class="message-time">{{ relativeTime(item.createTime) }}</div>
            </div>
          </div>
        </van-list>
        <van-empty v-if="!loading && !messages.length" description="暂无消息">
          <div class="small muted">客户超过设定时间未拜访时，提醒会出现在这里</div>
        </van-empty>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { markAllRead, markRead, pageMessages } from '../../api/message'
import { useUserStore } from '../../stores/user'
import { relativeTime } from '../../utils/format'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('all')
const messages = ref([])
const pageNo = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

async function loadMore() {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const params = { pageNo: pageNo.value, pageSize: 10 }
    if (activeTab.value === 'unread') {
      params.izRead = 0
    }
    const data = await pageMessages(params)
    if (pageNo.value === 1) {
      messages.value = data.records
    } else {
      messages.value.push(...data.records)
    }
    pageNo.value += 1
    finished.value = messages.value.length >= data.total || data.records.length === 0
    if (data.records.length === 0) {
      finished.value = true
    }
  } catch (error) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function reset() {
  messages.value = []
  pageNo.value = 1
  finished.value = false
  loading.value = false
  nextTick(() => loadMore())
}

function switchTab(tab) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  reset()
}

async function onRefresh() {
  reset()
}

async function openMessage(item) {
  if (!item.izRead) {
    try {
      await markRead(item.id)
      item.izRead = 1
      userStore.refreshUnread()
    } catch (error) {
      return
    }
  }
  if (item.bizType === 1 && item.bizId) {
    router.push(`/customers/${item.bizId}`)
  }
}

async function onReadAll() {
  try {
    await markAllRead()
    messages.value.forEach((item) => {
      item.izRead = 1
    })
    userStore.refreshUnread()
    showSuccessToast('已全部标记为已读')
  } catch (error) {
    showFailToast('操作失败')
  }
}

onMounted(() => {
  loadMore()
})
</script>
