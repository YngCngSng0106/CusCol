import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import * as userApi from '../api/user'
import * as messageApi from '../api/message'
import { TOKEN_KEY } from '../utils/request'

const USER_KEY = 'cc_user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (error) {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser(),
    unreadCount: 0
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.user?.username || '未登录',
    avatarUrl: (state) => state.user?.avatarThumbUrl || state.user?.avatarUrl || ''
  },
  actions: {
    setSession(token, user) {
      this.token = token || ''
      this.user = user || null
      if (token) {
        localStorage.setItem(TOKEN_KEY, token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
      if (user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user))
      }
    },
    setUser(user) {
      this.user = user
      if (user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user))
      }
    },
    async login(payload) {
      const data = await authApi.login(payload)
      this.setSession(data.token, data.user)
      return data
    },
    async fetchProfile() {
      const profile = await userApi.getProfile()
      this.setUser(profile)
      return profile
    },
    async refreshUnread() {
      if (!this.token) {
        this.unreadCount = 0
        return 0
      }
      try {
        this.unreadCount = Number(await messageApi.unreadCount()) || 0
      } catch (error) {
        this.unreadCount = 0
      }
      return this.unreadCount
    },
    async logout() {
      try {
        await authApi.logout()
      } catch (error) {
        // 忽略退出接口异常，本地会话照常清理
      }
      this.token = ''
      this.user = null
      this.unreadCount = 0
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
