import http from '../utils/request'

export function getCaptcha() {
  return http.get('/auth/captcha')
}

export function login(data) {
  return http.post('/auth/login', data)
}

export function register(data) {
  return http.post('/auth/register', data)
}

export function sendEmailCode(email, scene = 1) {
  return http.post('/auth/email-code', { email, scene })
}

export function logout() {
  return http.post('/auth/logout')
}
