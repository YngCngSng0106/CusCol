<template>
  <div class="page">
    <van-nav-bar title="修改密码" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <van-form @submit="onSubmit">
        <van-cell-group inset style="margin: 0; background: #f8faff; border-radius: 12px">
          <van-field
            v-model="form.oldPassword"
            type="password"
            label="旧密码"
            placeholder="请输入旧密码"
            required
            :rules="[{ required: true, message: '请输入旧密码' }]"
          />
          <van-field
            v-model="form.newPassword"
            type="password"
            label="新密码"
            placeholder="6-32位新密码"
            required
            :rules="[
              { required: true, message: '请输入新密码' },
              { validator: (value) => value.length >= 6, message: '新密码至少6位' }
            ]"
          />
          <van-field
            v-model="form.confirmPassword"
            type="password"
            label="确认密码"
            placeholder="请再次输入新密码"
            required
            :rules="[
              { required: true, message: '请确认新密码' },
              { validator: (value) => value === form.newPassword, message: '两次输入的密码不一致' }
            ]"
          />
        </van-cell-group>
        <div class="form-tip">修改成功后需要重新登录</div>
        <div class="submit-bar">
          <van-button block round type="primary" native-type="submit" :loading="submitting">确认修改</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { changePassword } from '../../api/user'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

async function onSubmit() {
  submitting.value = true
  try {
    await changePassword({ ...form })
    showSuccessToast('密码修改成功，请重新登录')
    await userStore.logout()
    router.replace('/login')
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}
</script>
