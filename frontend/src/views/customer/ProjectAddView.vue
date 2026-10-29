<template>
  <div class="page">
    <van-nav-bar title="录入项目" left-arrow @click-left="router.back()" />
    <div class="page-body">
      <van-form @submit="onSubmit">
        <van-cell-group inset style="margin: 0; background: #f8faff; border-radius: 12px">
          <van-field
            v-model="form.projectName"
            label="项目名称"
            placeholder="请输入项目名称"
            required
            maxlength="100"
            :rules="[{ required: true, message: '请输入项目名称' }]"
          />
          <van-field v-model="form.projectType" label="项目类型" placeholder="如数字化改造" maxlength="50" />
          <van-field v-model="form.projectAmount" type="number" label="项目金额" placeholder="请输入项目金额">
            <template #extra><span class="muted small">元</span></template>
          </van-field>
          <PickerField
            v-model="form.projectStatus"
            label="项目状态"
            placeholder="请选择项目状态"
            :options="PROJECT_STATUSES"
          />
          <van-field v-model="form.startDate" type="date" label="开始日期" />
          <van-field v-model="form.endDate" type="date" label="结束日期" />
          <van-field
            v-model="form.projectDesc"
            label="项目说明"
            type="textarea"
            rows="4"
            autosize
            maxlength="1000"
            show-word-limit
            placeholder="项目背景、范围、目标等"
          />
        </van-cell-group>
        <div class="submit-bar">
          <van-button block round type="primary" native-type="submit" :loading="submitting">
            确认添加
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import PickerField from '../../components/PickerField.vue'
import { addProject } from '../../api/customer'
import { PROJECT_STATUSES } from '../../utils/constants'
import { emptyToNull } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const customerId = Number(route.params.id)
const submitting = ref(false)
const form = reactive({
  projectName: '',
  projectType: '',
  projectAmount: '',
  projectStatus: 1,
  startDate: '',
  endDate: '',
  projectDesc: ''
})

async function onSubmit() {
  submitting.value = true
  try {
    await addProject(customerId, {
      ...form,
      projectAmount: emptyToNull(form.projectAmount),
      startDate: emptyToNull(form.startDate),
      endDate: emptyToNull(form.endDate)
    })
    showSuccessToast('项目录入成功')
    router.back()
  } catch (error) {
    // 错误提示已由请求层处理
  } finally {
    submitting.value = false
  }
}
</script>
