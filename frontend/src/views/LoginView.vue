<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="row gap-12">
        <div class="brand-logo">CC</div>
        <div>
          <div class="auth-title">CusCol</div>
          <div class="auth-sub" style="margin-bottom: 0">客户信息收集与维护</div>
        </div>
      </div>

      <van-form class="login-form" @submit="onSubmit">
        <van-cell-group inset style="margin: 22px 0 0; background: #f8faff; border-radius: 12px">
          <van-field
            v-model="form.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            left-icon="user-o"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            left-icon="lock"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
            v-model="form.captchaCode"
            name="captchaCode"
            label="验证码"
            placeholder="请输入"
            left-icon="shield-o"
            maxlength="4"
            center
            class="captcha-field"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <div class="captcha-box" @click="loadCaptcha">
                <img
                  v-if="captcha.image"
                  class="captcha-img"
                  :src="captcha.image"
                  alt="验证码"
                />
                <span v-else class="captcha-placeholder">点击加载</span>
                <van-icon name="replay" class="captcha-refresh" size="15" />
              </div>
            </template>
          </van-field>
        </van-cell-group>

        <div style="padding: 20px 0 6px">
          <van-button block round type="primary" native-type="submit" :loading="loading">登 录</van-button>
        </div>
      </van-form>

      <div class="auth-footer">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </div>
      <div class="auth-footer" style="margin-top: 4px">
        演示账号：demo / 123456
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getCaptcha } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: ''
})
const captcha = reactive({ key: '', image: '' })
const loading = ref(false)

async function loadCaptcha() {
  try {
    const data = await getCaptcha()
    captcha.key = data.key
    captcha.image = data.image
    form.captchaKey = data.key
    form.captchaCode = ''
  } catch (error) {
    captcha.image = ''
  }
}

async function onSubmit() {
  if (!form.captchaKey) {
    showFailToast('请刷新验证码')
    await loadCaptcha()
    return
  }
  loading.value = true
  try {
    await userStore.login({ ...form })
    showSuccessToast('登录成功')
    router.replace(String(route.query.redirect || '/messages'))
  } catch (error) {
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>
