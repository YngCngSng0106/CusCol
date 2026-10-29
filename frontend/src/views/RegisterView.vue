<template>
  <div class="page">
    <van-nav-bar title="注册账号" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <div class="card" style="padding: 18px 14px">
        <div class="auth-title" style="font-size: 20px; margin-top: 0">创建新账号</div>
        <div class="auth-sub">填写基础信息并完成邮箱验证</div>

        <van-form @submit="onSubmit">
          <van-cell-group inset style="background: #f8faff; border-radius: 12px">
            <van-field
              v-model="form.username"
              label="用户名"
              placeholder="3-20位用户名"
              left-icon="user-o"
              :rules="[{ required: true, message: '请输入用户名' }]"
            />
            <van-field
              v-model="form.password"
              type="password"
              label="密码"
              placeholder="6-32位密码"
              left-icon="lock"
              :rules="[{ required: true, message: '请输入密码' }]"
            />
            <van-field
              v-model="form.phone"
              type="tel"
              label="手机号"
              placeholder="请输入11位手机号"
              left-icon="phone-o"
              maxlength="11"
              :rules="[
                { required: true, message: '请输入手机号' },
                { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
              ]"
            />
            <van-field
              v-model="form.email"
              type="email"
              label="邮箱"
              placeholder="用于接收验证码"
              left-icon="envelop-o"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { pattern: /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/, message: '邮箱格式不正确' }
              ]"
            />
            <van-field
              v-model="form.code"
              label="邮箱验证码"
              placeholder="请输入6位验证码"
              left-icon="shield-o"
              maxlength="6"
              :rules="[{ required: true, message: '请输入邮箱验证码' }]"
            >
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  plain
                  :disabled="countdown > 0 || sending"
                  :loading="sending"
                  @click="onSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </van-button>
              </template>
            </van-field>
          </van-cell-group>

          <div style="padding: 22px 0 4px">
            <van-button block round type="primary" native-type="submit" :loading="loading">注 册</van-button>
          </div>
        </van-form>

        <div class="auth-footer">
          已有账号？
          <router-link to="/login">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { register, sendEmailCode } from '../api/auth'

const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  code: ''
})
const sending = ref(false)
const loading = ref(false)
const countdown = ref(0)
let timer = null

function startCountdown() {
  countdown.value = 60
  timer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      window.clearInterval(timer)
      timer = null
    }
  }, 1000)
}

async function onSendCode() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    showFailToast('请先输入正确的手机号')
    return
  }
  if (!/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(form.email)) {
    showFailToast('请先输入正确的邮箱')
    return
  }
  sending.value = true
  try {
    const data = await sendEmailCode(form.email, 1)
    if (data.devCode) {
      form.code = data.devCode
      showToast(`开发模式验证码：${data.devCode}`)
    } else {
      showSuccessToast('验证码已发送')
    }
    startCountdown()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  loading.value = true
  try {
    await register({ ...form })
    showSuccessToast('注册成功，请登录')
    router.replace('/login')
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>
