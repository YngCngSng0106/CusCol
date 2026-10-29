<template>
  <div class="layout-shell">
    <main class="layout-main">
      <router-view />
    </main>
    <van-tabbar route :fixed="false" class="app-tabbar" safe-area-inset-bottom>
      <van-tabbar-item replace to="/messages" icon="chat-o" :badge="unreadBadge">最近消息</van-tabbar-item>
      <van-tabbar-item replace to="/customers" icon="friends-o">客户</van-tabbar-item>
      <van-tabbar-item replace to="/contracts" icon="orders-o">合同</van-tabbar-item>
      <van-tabbar-item replace to="/visits" icon="notes-o">拜访记录</van-tabbar-item>
      <van-tabbar-item replace to="/profile" icon="user-o">个人</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const unreadBadge = computed(() => (userStore.unreadCount > 0 ? Math.min(userStore.unreadCount, 99) : ''))
let timer = null

onMounted(async () => {
  try {
    await userStore.fetchProfile()
  } catch (error) {
    // 登录态失效时由请求层统一跳转
  }
  userStore.refreshUnread()
  timer = window.setInterval(() => {
    userStore.refreshUnread()
  }, 60000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>
