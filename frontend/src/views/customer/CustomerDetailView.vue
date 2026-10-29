<template>
  <div class="page">
    <van-nav-bar title="客户详情" left-arrow @click-left="router.back()" />

    <div v-if="detail" class="page-body" style="padding-bottom: 30px">
      <div class="detail-hero">
        <div class="row-between gap-8">
          <div class="flex-1">
            <h2 class="detail-title">{{ detail.customer.customerName }}</h2>
            <div class="row gap-8" style="margin-top: 9px; flex-wrap: wrap">
              <span v-if="detail.customer.customerLevel" class="tag">
                {{ labelOf(CUSTOMER_LEVELS, detail.customer.customerLevel) }}
              </span>
              <span class="tag green">{{ labelOf(CUSTOMER_STATUSES, detail.customer.status) }}</span>
              <span v-if="detail.customer.izValid === 0" class="tag red">已屏蔽</span>
            </div>
          </div>
          <van-icon name="friends-o" size="34" color="#dbe5ff" />
        </div>

        <div class="info-list">
          <div class="info-row"><span class="info-label">所属行业</span><span class="info-value">{{ detail.customer.industry || '-' }}</span></div>
          <div class="info-row"><span class="info-label">所在城市</span><span class="info-value">{{ detail.customer.city || '-' }}</span></div>
          <div class="info-row"><span class="info-label">详细地址</span><span class="info-value">{{ detail.customer.address || '-' }}</span></div>
          <div class="info-row">
            <span class="info-label">联系人</span>
            <span class="info-value">
              {{ detail.customer.contactName || '-' }}
              <a v-if="detail.customer.contactPhone" :href="`tel:${detail.customer.contactPhone}`" class="small" style="margin-left: 8px">
                <van-icon name="phone-o" /> 拨号
              </a>
            </span>
          </div>
          <div class="info-row"><span class="info-label">联系电话</span><span class="info-value">{{ detail.customer.contactPhone || '-' }}</span></div>
          <div class="info-row"><span class="info-label">客户来源</span><span class="info-value">{{ detail.customer.source || '-' }}</span></div>
          <div class="info-row">
            <span class="info-label">最近拜访</span>
            <span class="info-value">{{ formatDateTime(detail.customer.lastVisitTime) }}</span>
          </div>
          <div class="info-row"><span class="info-label">备注</span><span class="info-value">{{ detail.customer.remark || '-' }}</span></div>
        </div>
      </div>

      <div class="card section">
        <div class="action-grid">
          <button class="action-item" @click="router.push(`/customers/${id}/edit`)">
            <span class="action-icon"><van-icon name="edit" /></span>
            修改资料
          </button>
          <button class="action-item" @click="router.push(`/customers/${id}/project/add`)">
            <span class="action-icon"><van-icon name="add-o" /></span>
            录入项目
          </button>
          <button class="action-item" @click="router.push(`/customers/${id}/visit/add`)">
            <span class="action-icon"><van-icon name="notes-o" /></span>
            添加拜访
          </button>
          <button class="action-item" @click="router.push(`/customers/${id}/contract/add`)">
            <span class="action-icon"><van-icon name="orders-o" /></span>
            添加合同
          </button>
          <button class="action-item" @click="router.push(`/customers/${id}/visits`)">
            <span class="action-icon"><van-icon name="records" /></span>
            拜访记录
          </button>
          <button class="action-item" @click="router.push(`/customers/${id}/contracts`)">
            <span class="action-icon"><van-icon name="label-o" /></span>
            合同列表
          </button>
          <button class="action-item" @click="toggleValid">
            <span class="action-icon"><van-icon :name="detail.customer.izValid === 0 ? 'eye-o' : 'closed-eye'" /></span>
            {{ detail.customer.izValid === 0 ? '恢复客户' : '屏蔽客户' }}
          </button>
          <button class="action-item" @click="onDelete">
            <span class="action-icon danger"><van-icon name="delete-o" /></span>
            删除客户
          </button>
        </div>
      </div>

      <div class="section">
        <div class="section-head">
          <span class="section-title">客户项目</span>
          <span class="section-extra" @click="router.push(`/customers/${id}/project/add`)">录入项目</span>
        </div>
        <div v-if="detail.projects?.length" class="card">
          <div v-for="project in detail.projects" :key="project.id" class="project-row">
            <div class="row-between gap-8">
              <div class="list-title" style="font-size: 14px">{{ project.projectName }}</div>
              <span class="tag orange">{{ labelOf(PROJECT_STATUSES, project.projectStatus) }}</span>
            </div>
            <div class="meta-row" style="margin-top: 6px">
              <span>{{ formatMoney(project.projectAmount) }}</span>
              <span>{{ project.projectType || '未分类' }}</span>
              <span>{{ formatDate(project.startDate) }} ~ {{ formatDate(project.endDate) }}</span>
            </div>
            <div v-if="project.projectDesc" class="list-sub">{{ project.projectDesc }}</div>
          </div>
        </div>
        <div v-else class="card small muted">暂无项目记录</div>
      </div>

      <div class="section">
        <div class="section-head">
          <span class="section-title">最近拜访</span>
          <span class="section-extra" @click="router.push(`/customers/${id}/visits`)">查看全部</span>
        </div>
        <div v-if="detail.visits?.length" class="card">
          <div v-for="visit in detail.visits" :key="visit.id" class="record-row" @click="router.push(`/visits/${visit.id}`)">
            <div class="row-between gap-8">
              <span class="list-title" style="font-size: 14px">{{ labelOf(VISIT_TYPES, visit.visitType) }}</span>
              <span class="small muted">{{ formatDateTime(visit.visitTime) }}</span>
            </div>
            <div class="list-sub clamp-2">{{ visit.visitContent }}</div>
          </div>
        </div>
        <div v-else class="card small muted">暂无拜访记录</div>
      </div>

      <div class="section">
        <div class="section-head">
          <span class="section-title">最近合同</span>
          <span class="section-extra" @click="router.push(`/customers/${id}/contracts`)">查看全部</span>
        </div>
        <div v-if="detail.contracts?.length" class="card">
          <div v-for="contract in detail.contracts" :key="contract.id" class="record-row" @click="router.push(`/contracts/${contract.id}`)">
            <div class="row-between gap-8">
              <span class="list-title ellipsis" style="font-size: 14px">{{ contract.contractName }}</span>
              <span class="tag green">{{ labelOf(CONTRACT_STATUSES, contract.contractStatus) }}</span>
            </div>
            <div class="meta-row" style="margin-top: 6px">
              <span>{{ contract.contractNo }}</span>
              <span>{{ formatMoney(contract.contractAmount) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="card small muted">暂无合同</div>
      </div>

      <div v-if="detail.attachments?.length" class="section">
        <div class="section-head"><span class="section-title">客户附件</span></div>
        <div class="card"><AttachmentList :attachments="detail.attachments" /></div>
      </div>
    </div>

    <div v-else class="page-body">
      <div class="skeleton-block"></div>
      <div class="skeleton-block"></div>
      <div class="skeleton-block"></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import AttachmentList from '../../components/AttachmentList.vue'
import { deleteCustomer, getCustomer, setCustomerValid } from '../../api/customer'
import {
  CONTRACT_STATUSES,
  CUSTOMER_LEVELS,
  CUSTOMER_STATUSES,
  PROJECT_STATUSES,
  VISIT_TYPES,
  labelOf
} from '../../utils/constants'
import { formatDate, formatDateTime, formatMoney } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)
const detail = ref(null)

async function load() {
  try {
    detail.value = await getCustomer(id)
  } catch (error) {
    // 错误提示已由请求层处理
  }
}

async function toggleValid() {
  const blocking = detail.value.customer.izValid !== 0
  try {
    await showConfirmDialog({
      title: blocking ? '屏蔽客户' : '恢复客户',
      message: blocking
        ? '屏蔽后该客户将不再参与拜访超期提醒，确定继续吗？'
        : '确定恢复该客户吗？'
    })
  } catch (error) {
    return
  }
  try {
    await setCustomerValid(id, blocking ? 0 : 1)
    showSuccessToast(blocking ? '客户已屏蔽' : '客户已恢复')
    await load()
  } catch (error) {
    // 错误提示已由请求层处理
  }
}

async function onDelete() {
  try {
    await showConfirmDialog({
      title: '删除客户',
      message: '删除后客户将不再展示，确定删除吗？',
      confirmButtonText: '确认删除',
      confirmButtonColor: '#e5484d'
    })
  } catch (error) {
    return
  }
  try {
    await deleteCustomer(id)
    showSuccessToast('客户已删除')
    router.replace('/customers')
  } catch (error) {
    // 错误提示已由请求层处理
  }
}

onMounted(load)
</script>

<style scoped>
.project-row,
.record-row {
  padding: 11px 0;
  border-bottom: 1px solid var(--cc-line);
}

.project-row:last-child,
.record-row:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.project-row:first-child,
.record-row:first-child {
  padding-top: 0;
}
</style>
