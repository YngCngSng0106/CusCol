<template>
  <div class="page">
    <van-nav-bar title="拜访详情" left-arrow @click-left="router.back()" />
    <div v-if="visit" class="page-body">
      <div class="card">
        <div class="row-between gap-8">
          <div>
            <div class="tiny muted">拜访客户</div>
            <div class="list-title" style="font-size: 18px; margin-top: 3px">{{ visit.customerName }}</div>
          </div>
          <span class="tag">{{ labelOf(VISIT_TYPES, visit.visitType) }}</span>
        </div>
        <div class="info-list">
          <div class="info-row"><span class="info-label">拜访时间</span><span class="info-value">{{ formatDateTime(visit.visitTime) }}</span></div>
          <div class="info-row"><span class="info-label">拜访地点</span><span class="info-value">{{ visit.visitLocation || '-' }}</span></div>
          <div class="info-row"><span class="info-label">拜访目的</span><span class="info-value">{{ visit.visitPurpose || '-' }}</span></div>
          <div class="info-row"><span class="info-label">下次拜访</span><span class="info-value">{{ formatDateTime(visit.nextVisitTime) }}</span></div>
        </div>
      </div>

      <div class="card section">
        <div class="section-title" style="margin-bottom: 8px">拜访内容</div>
        <div style="font-size: 14px; line-height: 1.8; white-space: pre-wrap">{{ visit.visitContent }}</div>
      </div>

      <div class="card section">
        <div class="section-title" style="margin-bottom: 8px">拜访结果</div>
        <div style="font-size: 14px; line-height: 1.8; white-space: pre-wrap">{{ visit.visitResult || '暂无记录' }}</div>
      </div>

      <div class="card section">
        <div class="section-title" style="margin-bottom: 8px">现场资料</div>
        <AttachmentList :attachments="visit.attachments || []" />
      </div>
    </div>
    <div v-else class="page-body">
      <div class="skeleton-block"></div>
      <div class="skeleton-block"></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AttachmentList from '../../components/AttachmentList.vue'
import { getVisit } from '../../api/visit'
import { VISIT_TYPES, labelOf } from '../../utils/constants'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const visit = ref(null)

onMounted(async () => {
  try {
    visit.value = await getVisit(route.params.id)
  } catch (error) {
    // 错误提示已由请求层处理
  }
})
</script>
