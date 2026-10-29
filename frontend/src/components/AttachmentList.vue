<template>
  <div>
    <div v-if="attachments.length">
      <div v-for="file in attachments" :key="file.id" class="attach-row">
        <img
          v-if="isImageAttachment(file) && attachmentUrl(file)"
          class="attach-thumb"
          :src="attachmentUrl(file)"
          alt=""
          @click="preview(file)"
        />
        <div v-else class="attach-thumb">
          <van-icon :name="isImageAttachment(file) ? 'photo-o' : 'description'" size="20" />
        </div>
        <div class="flex-1" @click="download(file)">
          <div class="attach-name ellipsis">{{ file.fileName }}</div>
          <div class="tiny muted">{{ file.attachKindText || formatSize(file.fileSize) }}</div>
        </div>
        <van-icon name="down" size="18" color="#2f6fed" @click="download(file)" />
      </div>
    </div>
    <div v-else class="small muted" style="padding: 4px 0">暂无附件</div>
  </div>
</template>

<script setup>
import { showFailToast, showImagePreview } from 'vant'
import { downloadAttachment } from '../api/attachment'
import { attachmentUrl, isImageAttachment } from '../utils/constants'

defineProps({
  attachments: { type: Array, default: () => [] }
})

function formatSize(size) {
  if (!size) return ''
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(1)}MB`
}

function preview(file) {
  const url = file.fileUrl || file.thumbUrl
  if (url) {
    showImagePreview([url])
  }
}

async function download(file) {
  try {
    await downloadAttachment(file.id, file.fileName)
  } catch (error) {
    showFailToast('附件下载失败')
  }
}
</script>
