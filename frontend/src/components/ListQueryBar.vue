<template>
  <div class="query-bar">
    <div class="query-bar__head">
      <div class="query-bar__title">
        <van-icon name="filter-o" size="15" />
        <span>筛选条件</span>
        <span v-if="activeCount > 0" class="query-bar__count">{{ activeCount }}</span>
        <span v-else class="tiny muted">未设置</span>
      </div>
      <button class="query-bar__toggle" type="button" @click="expanded = !expanded">
        {{ expanded ? '收起' : '筛选' }}
        <van-icon :name="expanded ? 'arrow-up' : 'arrow-down'" size="12" />
      </button>
    </div>
    <div v-show="expanded" class="query-bar__body">
      <slot />
      <div class="query-bar__actions">
        <van-button size="small" round plain type="primary" @click="onReset">重置</van-button>
        <van-button size="small" round type="primary" @click="onSearch">查询</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  activeCount: { type: Number, default: 0 }
})
const emit = defineEmits(['search', 'reset'])
const expanded = ref(false)

function onSearch() {
  expanded.value = false
  emit('search')
}

function onReset() {
  emit('reset')
}
</script>
