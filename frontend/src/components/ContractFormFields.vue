<template>
  <van-cell-group inset style="margin: 0; background: #f8faff; border-radius: 12px">
    <van-field
      v-model="form.contractNo"
      label="合同编号"
      placeholder="请输入合同编号"
      required
      maxlength="50"
      :rules="[{ required: true, message: '请输入合同编号' }]"
    />
    <van-field
      v-model="form.contractName"
      label="合同名称"
      placeholder="请输入合同名称"
      required
      maxlength="200"
      :rules="[{ required: true, message: '请输入合同名称' }]"
    />
    <van-field
      v-model="form.contractAmount"
      type="number"
      label="合同金额"
      placeholder="请输入合同金额"
      required
      :rules="[{ required: true, message: '请输入合同金额' }]"
    >
      <template #extra><span class="muted small">元</span></template>
    </van-field>
    <van-field v-model="form.signDate" type="date" label="签订日期" />
    <van-field v-model="form.startDate" type="date" label="生效日期" />
    <van-field v-model="form.endDate" type="date" label="到期日期" />
    <PickerField
      v-model="form.contractStatus"
      label="合同状态"
      placeholder="请选择合同状态"
      :options="CONTRACT_STATUSES"
    />
    <PickerField
      v-model="form.paymentStatus"
      label="回款状态"
      placeholder="请选择回款状态"
      :options="PAYMENT_STATUSES"
    />
    <van-field v-model="form.paidAmount" type="number" label="已回款" placeholder="请输入已回款金额">
      <template #extra><span class="muted small">元</span></template>
    </van-field>
    <van-field
      v-model="form.remark"
      label="备注"
      type="textarea"
      rows="2"
      autosize
      maxlength="500"
      placeholder="补充说明"
    />
  </van-cell-group>

  <div class="form-label">合同附件</div>
  <AttachmentUploader
    :model-value="attachments"
    :biz-type="4"
    :biz-id="bizId"
    :attach-kind="2"
    :max-count="10"
    @update:model-value="$emit('update:attachments', $event)"
  />
</template>

<script setup>
import PickerField from './PickerField.vue'
import AttachmentUploader from './AttachmentUploader.vue'
import { CONTRACT_STATUSES, PAYMENT_STATUSES } from '../utils/constants'

defineProps({
  form: { type: Object, required: true },
  attachments: { type: Array, default: () => [] },
  bizId: { type: [Number, String], default: 0 }
})
defineEmits(['update:attachments'])
</script>
