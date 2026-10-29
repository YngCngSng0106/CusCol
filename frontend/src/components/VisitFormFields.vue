<template>
  <van-cell-group inset style="margin: 0; background: #f8faff; border-radius: 12px">
    <PickerField
      v-model="form.visitType"
      label="拜访方式"
      placeholder="请选择拜访方式"
      :options="VISIT_TYPES"
      required
    />
    <van-field
      v-model="form.visitTime"
      type="datetime-local"
      label="拜访时间"
      required
      :rules="[{ required: true, message: '请选择拜访时间' }]"
    />
    <van-field v-model="form.visitLocation" label="拜访地点" placeholder="请输入拜访地点" maxlength="255" />
    <van-field v-model="form.visitPurpose" label="拜访目的" placeholder="请输入拜访目的" maxlength="200" />
    <van-field
      v-model="form.visitContent"
      label="拜访内容"
      type="textarea"
      rows="4"
      autosize
      maxlength="2000"
      show-word-limit
      required
      placeholder="请记录本次拜访沟通内容"
      :rules="[{ required: true, message: '请输入拜访内容' }]"
    />
    <van-field
      v-model="form.visitResult"
      label="拜访结果"
      type="textarea"
      rows="2"
      autosize
      maxlength="500"
      placeholder="本次拜访达成的结果"
    />
    <van-field v-model="form.nextVisitTime" type="datetime-local" label="下次拜访" />
  </van-cell-group>

  <div class="form-label">现场照片 / 附件</div>
  <AttachmentUploader
    :model-value="attachments"
    :biz-type="3"
    :biz-id="bizId"
    :attach-kind="5"
    :max-count="9"
    @update:model-value="$emit('update:attachments', $event)"
  />
</template>

<script setup>
import PickerField from './PickerField.vue'
import AttachmentUploader from './AttachmentUploader.vue'
import { VISIT_TYPES } from '../utils/constants'

defineProps({
  form: { type: Object, required: true },
  attachments: { type: Array, default: () => [] },
  bizId: { type: [Number, String], default: 0 }
})
defineEmits(['update:attachments'])
</script>
