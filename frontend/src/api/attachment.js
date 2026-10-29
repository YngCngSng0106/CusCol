import http from '../utils/request'

export function uploadAttachment(file, { bizType, bizId = 0, attachKind = 6 }) {
  const form = new FormData()
  form.append('file', file)
  form.append('bizType', bizType)
  form.append('bizId', bizId)
  form.append('attachKind', attachKind)
  return http.post('/attachment/upload', form)
}

export function listAttachments(bizType, bizId) {
  return http.get('/attachment/list', { params: { bizType, bizId } })
}

export function deleteAttachment(id) {
  return http.delete(`/attachment/${id}`)
}

export async function downloadAttachment(id, fileName) {
  const response = await http.get(`/attachment/${id}/download`, { responseType: 'blob' })
  const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName || 'attachment'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
