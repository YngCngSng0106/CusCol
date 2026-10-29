<template>
  <van-cell-group inset style="margin: 0; background: #f8faff; border-radius: 12px">
    <van-field
      v-model="form.customerName"
      label="客户名称"
      placeholder="请输入客户名称"
      required
      maxlength="100"
      :rules="[{ required: true, message: '请输入客户名称' }]"
    />
    <PickerField
      v-model="form.customerLevel"
      label="客户等级"
      placeholder="请选择客户等级"
      :options="CUSTOMER_LEVELS"
    />
    <van-field v-model="form.industry" label="所属行业" placeholder="请输入所属行业" maxlength="50" />
    <van-field v-model="form.city" label="城市" placeholder="请输入城市" maxlength="50" />
    <van-field v-model="form.address" label="详细地址" placeholder="请输入详细地址" maxlength="255" />
    <van-field v-model="form.contactName" label="联系人" placeholder="请输入联系人" maxlength="50" />
    <van-field
      v-model="form.contactPhone"
      type="tel"
      label="联系电话"
      placeholder="请输入联系电话"
      maxlength="20"
    />
    <van-field v-model="form.source" label="客户来源" placeholder="如展会、转介绍" maxlength="50" />
    <PickerField
      v-model="form.status"
      label="客户状态"
      placeholder="请选择客户状态"
      :options="CUSTOMER_STATUSES"
    />
    <van-field
      v-model="form.remark"
      label="备注"
      type="textarea"
      rows="3"
      autosize
      maxlength="500"
      show-word-limit
      placeholder="补充说明"
    />
  </van-cell-group>
</template>

<script setup>
import PickerField from './PickerField.vue'
import { CUSTOMER_LEVELS, CUSTOMER_STATUSES } from '../utils/constants'

defineProps({
  form: { type: Object, required: true }
})
</script>
