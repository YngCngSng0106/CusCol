import dayjs from 'dayjs'

export function formatDateTime(value, template = 'YYYY-MM-DD HH:mm') {
  if (!value) return '-'
  return dayjs(value).format(template)
}

export function formatDate(value) {
  return formatDateTime(value, 'YYYY-MM-DD')
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  if (Number.isNaN(number)) return '-'
  return `¥${number.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function relativeTime(value) {
  if (!value) return ''
  const target = dayjs(value)
  const now = dayjs()
  const minutes = now.diff(target, 'minute')
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = now.diff(target, 'hour')
  if (hours < 24) return `${hours}小时前`
  const days = now.diff(target, 'day')
  if (days < 7) return `${days}天前`
  return target.format('MM-DD HH:mm')
}

export function toDateTimeInput(value) {
  if (!value) return ''
  return dayjs(value).format('YYYY-MM-DDTHH:mm')
}

export function toDateInput(value) {
  if (!value) return ''
  return dayjs(value).format('YYYY-MM-DD')
}

export function emptyToNull(value) {
  return value === '' || value === undefined ? null : value
}
