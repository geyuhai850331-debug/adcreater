<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="68px" class="-mb-15px">
      <el-form-item label="用户 ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户 ID"
          clearable
          class="!w-160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交易类型" prop="type">
        <el-select
          v-model="queryParams.type"
          placeholder="请选择"
          clearable
          class="!w-140px"
        >
          <el-option label="充值" value="RECHARGE" />
          <el-option label="消费" value="CONSUME" />
          <el-option label="退款" value="REFUND" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择"
          clearable
          class="!w-120px"
        >
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="待处理" value="PENDING" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="dateRange">
        <el-date-picker
          v-model="queryParams.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <div class="flex items-center mb-16px">
      <span class="font-bold text-14px mr-8px">快速充值：</span>
      <el-input-number
        v-model="quickRecharge.userId"
        :min="1"
        placeholder="用户 ID"
        class="!w-160px"
      />
      <el-input-number
        v-model="quickRecharge.amount"
        :min="1"
        :max="999999"
        placeholder="数量"
        class="!w-160px ml-12px"
      />
      <el-input
        v-model="quickRecharge.remark"
        placeholder="备注"
        clearable
        class="!w-200px ml-12px"
      />
      <el-button
        type="success"
        :loading="quickRechargeLoading"
        @click="handleQuickRecharge"
        class="ml-12px"
      >
        <Icon icon="ep:wallet" />充值
      </el-button>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户 ID" width="100" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag
            :type="(row.type === 'RECHARGE' ? 'success' : row.type === 'REFUND' ? 'warning' : 'info') as any"
            size="small"
          >
            {{ typeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="数量" width="120">
        <template #default="{ row }">
          <span :style="{ color: row.type === 'RECHARGE' ? '#67C23A' : '#F56C6C' }">
            {{ row.type === 'CONSUME' ? '-' : '+' }}{{ row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="balanceAfter" label="操作后余额" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 'SUCCESS' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'"
            size="small"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="showDetail(row)">
            <Icon icon="ep:view" />详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.pageSize"
      @pagination="fetchRecords"
    />
  </ContentWrap>

  <el-dialog v-model="detailVisible" title="交易详情" width="480px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="交易 ID">{{ detail.id }}</el-descriptions-item>
      <el-descriptions-item label="用户 ID">{{ detail.userId }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ typeLabel(detail.type) }}</el-descriptions-item>
      <el-descriptions-item label="数量">{{ detail.amount }}</el-descriptions-item>
      <el-descriptions-item label="操作前余额">{{ detail.balanceBefore }}</el-descriptions-item>
      <el-descriptions-item label="操作后余额">{{ detail.balanceAfter }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
      <el-descriptions-item label="备注">{{ detail.remark }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { getTransactionPage, recharge } from '@/api/adcreater/billing'

defineOptions({ name: 'AdCreaterBilling' })

const message = useMessage()

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  page: 1,
  pageSize: 20,
  userId: '',
  type: '' as string,
  status: '' as string,
  dateRange: null as [Date, Date] | null
})

const typeMap: Record<string, string> = { RECHARGE: '充值', CONSUME: '消费', REFUND: '退款' }
const statusMap: Record<string, string> = { SUCCESS: '成功', FAILED: '失败', PENDING: '待处理' }

function typeLabel(type: string): string {
  return typeMap[type] || type
}
function statusLabel(status: string): string {
  return statusMap[status] || status
}

async function fetchRecords() {
  loading.value = true
  try {
    const params: any = { page: queryParams.page, pageSize: queryParams.pageSize }
    if (queryParams.userId) params.userId = queryParams.userId
    if (queryParams.type) params.type = queryParams.type
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.dateRange) {
      params.startDate = queryParams.dateRange[0]?.toISOString()
      params.endDate = queryParams.dateRange[1]?.toISOString()
    }
    const res = await getTransactionPage(params)
    if (res) {
      tableData.value = res.list || res.records || []
      total.value = res.total ?? 0
    }
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.page = 1
  fetchRecords()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const quickRechargeLoading = ref(false)
const quickRecharge = reactive({ userId: null as number | null, amount: 100, remark: '' })

async function handleQuickRecharge() {
  if (!quickRecharge.userId || quickRecharge.userId < 1) {
    message.warning('请输入有效的用户 ID')
    return
  }
  if (!quickRecharge.amount || quickRecharge.amount < 1) {
    message.warning('请输入有效的充值数量')
    return
  }
  quickRechargeLoading.value = true
  try {
    await recharge({
      userId: quickRecharge.userId,
      amount: quickRecharge.amount,
      remark: quickRecharge.remark
    })
    message.success('充值成功')
    quickRecharge.remark = ''
    fetchRecords()
  } catch {
    // error handled by interceptor
  } finally {
    quickRechargeLoading.value = false
  }
}

const detailVisible = ref(false)
const detail = ref<any>({})

function showDetail(row: any) {
  detail.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchRecords()
})
</script>
