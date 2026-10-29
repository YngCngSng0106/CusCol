<template>
  <div class="page">
    <van-nav-bar title="修改个人信息" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <van-form v-if="ready" @submit="onSubmit">
        <van-cell-group inset style="margin: 0; background: #f8faff; border-radius: 12px">
          <van-field
            v-model="form.username"
            label="用户名"
            placeholder="请输入用户名"
            required
            maxlength="20"
            :rules="[
              { required: true, message: '请输入用户名' },
              { validator: (value) => value.length >= 3, message: '用户名至少3位' }
            ]"
          />
          <van-field v-model="form.city" label="所在城市" placeholder="请输入所在城市" maxlength="50" />
          <van-field :model-value="profile?.phone" label="手机号" readonly />
          <van-field :model-value="profile?.email" label="邮箱" readonly />
        </van-cell-group>
        <div class="form-tip">手机号和邮箱暂不支持修改</div>
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
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { updateProfile } from '../../api/user'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const profile = ref(null)
const ready = ref(false)
const submitting = ref(false)
const form = reactive({ username: '', city: '' })

async function load() {
  try {
    const data = await userStore.fetchProfile()
    profile.value = data
    form.username = data.username
    form.city = data.city || ''
    ready.value = true
  } catch (error) {
    router.back()
  }
}

async function onSubmit() {
  submitting.value = true
  try {
    const data = await updateProfile({ ...form })
    userStore.setUser(data)
    showSuccessToast('个人信息已更新')
    router.back()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>
