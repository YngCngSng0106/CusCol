import http from '../utils/request'

export function getProfile() {
  return http.get('/user/profile')
}

export function updateProfile(data) {
  return http.put('/user/profile', data)
}

export function changePassword(data) {
  return http.put('/user/password', data)
}

export function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  return http.post('/user/avatar', form)
}
