<template>
  <van-field
    readonly
    is-link
    :label="label"
    :model-value="displayText"
    :placeholder="placeholder"
    :required="required"
    @click="open"
  />
  <van-popup
    v-model:show="show"
    position="bottom"
    round
    teleport="body"
    class="app-popup"
    overlay-class="app-overlay"
  >
    <van-picker
      v-model="pickerValue"
      :columns="options"
      :title="label"
      @confirm="onConfirm"
      @cancel="show = false"
    />
  </van-popup>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, String], default: null },
  options: { type: Array, default: () => [] },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '请选择' },
  required: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const show = ref(false)
const pickerValue = ref([])

const displayText = computed(() => {
  const item = props.options.find((option) => option.value === props.modelValue)
  return item ? item.text : ''
})

watch(
  () => props.modelValue,
  (value) => {
    pickerValue.value = value === null || value === undefined ? [] : [value]
  },
  { immediate: true }
)

function open() {
  show.value = true
}

function onConfirm({ selectedOptions }) {
  const option = selectedOptions[0]
  if (option) {
    emit('update:modelValue', option.value)
  }
  show.value = false
}
</script>
