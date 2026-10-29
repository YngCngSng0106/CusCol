<template>
  <div>
    <div v-if="files.length" class="uploader-list">
      <div v-for="file in files" :key="file.id || file.fileKey" class="attach-row">
        <img
          v-if="isImageAttachment(file) && attachmentUrl(file)"
          class="attach-thumb"
          :src="attachmentUrl(file)"
          alt=""
          @click="preview(file)"
        />
        <div v-else class="attach-thumb">
          <van-icon :name="isImageAttachment(file) ? 'photo-o' : 'description' " size="20" />
        </div>
        <div class="flex-1">
          <div class="attach-name ellipsis">{{ file.fileName }}</div>
          <div class="tiny muted">{{ file.fileSize ? formatSize(file.fileSize) : '' }}</div>
        </div>
        <van-icon name="cross" class="muted" size="18" @click="remove(file)" />
      </div>
    </div>

    <input
      ref="inputRef"
      type="file"
      :accept="accept"
      :multiple="maxCount > 1"
      hidden
      @change="onSelect"
    />
    <van-button
      class="upload-btn"
      icon="plus"
      :loading="uploading"
      :disabled="files.length >= maxCount"
      @click="inputRef.click()"
    >
      {{ files.length >= maxCount ? '已达到附件上限' : '添加附件' }}
    </van-button>
    <div class="tiny muted" style="margin-top: 6px">
      支持图片、PDF、Word、Excel、PPT、TXT、ZIP，图片单张不超过 10MB，文件不超过 50MB
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { showConfirmDialog, showFailToast, showImagePreview, showSuccessToast } from 'vant'
import { deleteAttachment, uploadAttachment } from '../api/attachment'
import { ATTACH_ACCEPT, attachmentUrl, isImageAttachment } from '../utils/constants'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  bizType: { type: Number, required: true },
  bizId: { type: [Number, String], default: 0 },
  attachKind: { type: Number, default: 6 },
  maxCount: { type: Number, default: 9 },
  accept: { type: String, default: ATTACH_ACCEPT },
  imageOnly: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const inputRef = ref(null)
const uploading = ref(false)
const files = computed(() => props.modelValue || [])

function formatSize(size) {
  if (!size) return ''
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(1)}MB`
}

async function onSelect(event) {
  const selected = Array.from(event.target.files || [])
  event.target.value = ''
  if (!selected.length) return
  const remaining = props.maxCount - files.value.length
  if (remaining <= 0) {
    showFailToast('附件数量已达上限')
    return
  }
  uploading.value = true
  const next = [...files.value]
  try {
    for (const file of selected.slice(0, remaining)) {
      if (props.imageOnly && !file.type.startsWith('image/')) {
        showFailToast('只能上传图片文件')
        continue
      }
      const attachment = await uploadAttachment(file, {
        bizType: props.bizType,
        bizId: props.bizId,
        attachKind: props.attachKind
      })
      next.push(attachment)
    }
    emit('update:modelValue', next)
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    uploading.value = false
  }
}

async function remove(file) {
  try {
    await showConfirmDialog({ title: '删除附件', message: `确定删除「${file.fileName}」吗？` })
  } catch (error) {
    return
  }
  try {
    if (file.id) {
      await deleteAttachment(file.id)
    }
    emit('update:modelValue', files.value.filter((item) => item.id !== file.id))
    showSuccessToast('已删除')
  } catch (error) {
    // 错误提示已由请求层处理
  }
}

function preview(file) {
  const url = file.fileUrl || file.thumbUrl
  if (url) {
    showImagePreview([url])
  }
}
</script>
