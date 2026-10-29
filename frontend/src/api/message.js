import http from '../utils/request'

export function pageMessages(params) {
  return http.get('/message/page', { params })
}

export function unreadCount() {
  return http.get('/message/unread-count')
}

export function markRead(id) {
  return http.put(`/message/${id}/read`)
}

export function markAllRead() {
  return http.put('/message/read-all')
}

export function runReminder() {
  return http.post('/message/reminder/run')
}
