export const CUSTOMER_LEVELS = [
  { text: 'A类客户', value: 1 },
  { text: 'B类客户', value: 2 },
  { text: 'C类客户', value: 3 }
]

export const CUSTOMER_STATUSES = [
  { text: '潜在客户', value: 1 },
  { text: '跟进中', value: 2 },
  { text: '已成交', value: 3 },
  { text: '已流失', value: 4 }
]

export const VISIT_TYPES = [
  { text: '上门拜访', value: 1 },
  { text: '电话沟通', value: 2 },
  { text: '线上沟通', value: 3 }
]

export const PROJECT_STATUSES = [
  { text: '意向', value: 1 },
  { text: '进行中', value: 2 },
  { text: '已完成', value: 3 },
  { text: '已终止', value: 4 }
]

export const CONTRACT_STATUSES = [
  { text: '草稿', value: 1 },
  { text: '生效中', value: 2 },
  { text: '已完成', value: 3 },
  { text: '已作废', value: 4 }
]

export const PAYMENT_STATUSES = [
  { text: '未回款', value: 0 },
  { text: '部分回款', value: 1 },
  { text: '已回款', value: 2 }
]

export const ATTACH_KINDS = [
  { text: '图片', value: 1 },
  { text: '合同正本', value: 2 },
  { text: '补充协议', value: 3 },
  { text: '发票', value: 4 },
  { text: '拜访现场资料', value: 5 },
  { text: '其他资料', value: 6 }
]

export const ATTACH_ACCEPT = [
  'image/jpeg',
  'image/png',
  'image/gif',
  'image/webp',
  'image/bmp',
  '.pdf',
  '.doc',
  '.docx',
  '.xls',
  '.xlsx',
  '.ppt',
  '.pptx',
  '.txt',
  '.zip',
  '.rar'
].join(',')

export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp,image/bmp'

export function labelOf(options, value, fallback = '-') {
  const item = options.find((option) => option.value === value)
  return item ? item.text : fallback
}

export function isImageAttachment(file) {
  if (!file) return false
  if (file.mimeType && file.mimeType.startsWith('image/')) return true
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(String(file.fileExt || '').toLowerCase())
}

export function attachmentUrl(file) {
  if (!file) return ''
  return file.thumbUrl || file.fileUrl || ''
}
