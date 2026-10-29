<template>
  <div class="page layout-page">
    <div class="profile-hero">
      <div class="row gap-12" style="position: relative; z-index: 1">
        <div class="avatar-wrap" @click="openAvatar">
          <img v-if="avatar" :src="avatar" alt="头像" />
          <van-icon v-else name="user-o" size="34" color="#9db6ef" />
          <span class="avatar-edit"><van-icon name="photograph" /></span>
        </div>
        <div class="flex-1">
          <div style="font-size: 20px; font-weight: 700">{{ user?.username || '未登录' }}</div>
          <div style="font-size: 12px; opacity: 0.85; margin-top: 5px">
            {{ user?.city || '未设置城市' }}
          </div>
        </div>
      </div>
      <div class="row gap-12" style="margin-top: 18px; position: relative; z-index: 1; font-size: 12px; opacity: 0.9">
        <span><van-icon name="phone-o" /> {{ user?.phone || '-' }}</span>
        <span class="ellipsis"><van-icon name="envelop-o" /> {{ user?.email || '-' }}</span>
      </div>
    </div>

    <div class="page-body" style="padding-top: 14px">
      <div class="card" style="padding: 4px 0">
        <van-cell title="修改个人信息" icon="edit" is-link @click="router.push('/profile/edit')" />
        <van-cell title="修改密码" icon="lock" is-link @click="router.push('/profile/password')" />
        <van-cell title="消息通知" icon="bell" is-link :value="`${userStore.unreadCount} 条未读`" @click="router.push('/messages')" />
      </div>

      <div class="card" style="padding: 4px 0; margin-top: 12px">
        <van-cell title="注销登录" icon="revoke" @click="onLogout" />
      </div>

      <div class="tiny muted" style="text-align: center; margin-top: 26px">
        CustomerCollection v1.0.0<br />
        客户信息收集与维护
      </div>
    </div>

    <van-popup
      v-model:show="avatarPopup"
      position="bottom"
      round
      teleport="body"
      class="app-popup"
      overlay-class="app-overlay"
    >
      <div class="popup-head">
        <span>设置头像</span>
        <van-icon name="cross" size="20" class="muted" @click="closeAvatar" />
      </div>
      <div style="padding: 22px 18px calc(22px + env(safe-area-inset-bottom)); text-align: center">
        <div class="avatar-wrap" style="margin: 0 auto; width: 108px; height: 108px; border-color: #dfe8fb">
          <img v-if="previewUrl || avatar" :src="previewUrl || avatar" alt="头像预览" />
          <van-icon v-else name="user-o" size="48" color="#9db6ef" />
        </div>
        <div class="small muted" style="margin: 12px 0 18px">仅支持 JPG、PNG、GIF、WEBP、BMP 图片，不超过 5MB</div>
        <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange" />
        <div class="row gap-8">
          <van-button block round plain type="primary" @click="fileInput.click()">选择图片</van-button>
          <van-button block round type="primary" :loading="uploading" :disabled="!selectedFile" @click="confirmUpload">
            确定
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showFailToast, showSuccessToast, showToast } from 'vant'
import { uploadAvatar } from '../../api/user'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const avatar = computed(() => userStore.avatarUrl)

const avatarPopup = ref(false)
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const uploading = ref(false)

async function loadProfile() {
  try {
    await userStore.fetchProfile()
    userStore.refreshUnread()
  } catch (error) {
    // 登录态失效由请求层处理
  }
}

function openAvatar() {
  avatarPopup.value = true
}

function closeAvatar() {
  avatarPopup.value = false
  selectedFile.value = null
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

function onFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showFailToast('头像文件上传无效，请选择图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    showFailToast('头像不能超过5MB')
    return
  }
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

async function confirmUpload() {
  if (!selectedFile.value) {
    showToast('请先选择图片')
    return
  }
  uploading.value = true
  try {
    await uploadAvatar(selectedFile.value)
    await userStore.fetchProfile()
    showSuccessToast('头像已更新')
    closeAvatar()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    uploading.value = false
  }
}

async function onLogout() {
  try {
    await showConfirmDialog({ title: '注销登录', message: '确定退出当前账号吗？' })
  } catch (error) {
    return
  }
  await userStore.logout()
  router.replace('/login')
}

onMounted(loadProfile)
onBeforeUnmount(() => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
})
</script>
