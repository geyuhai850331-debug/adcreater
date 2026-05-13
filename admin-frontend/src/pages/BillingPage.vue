<template>
  <div class="billing-page">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input
          v-model="search.userId"
          placeholder="用户 ID"
          clearable
          style="width: 160px"
          @keyup.enter="fetchRecords"
        />
        <el-select
          v-model="search.type"
          placeholder="交易类型"
          clearable
          style="width: 140px; margin-left: 12px"
        >
          <el-option label="充值" value="RECHARGE" />
          <el-option label="消费" value="CONSUME" />
          <el-option label="退款" value="REFUND" />
        </el-select>
        <el-select
          v-model="search.status"
          placeholder="状态"
          clearable
          style="width: 120px; margin-left: 12px"
        >
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="待处理" value="PENDING" />
        </el-select>
        <el-date-picker
          v-model="search.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="margin-left: 12px"
        />
        <el-button type="primary" @click="fetchRecords" style="margin-left: 12px">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <!-- Recharge form -->
    <el-card shadow="never" style="margin-top: 16px">
      <div class="recharge-bar">
        <span class="recharge-label">快速充值：</span>
        <el-input-number
          v-model="quickRecharge.userId"
          :min="1"
          placeholder="用户 ID"
          style="width: 160px"
        />
        <el-input-number
          v-model="quickRecharge.amount"
          :min="1"
          :max="999999"
          placeholder="数量"
          style="width: 160px; margin-left: 12px"
        />
        <el-input
          v-model="quickRecharge.remark"
          placeholder="备注"
          clearable
          style="width: 200px; margin-left: 12px"
        />
        <el-button
          type="success"
          :loading="quickRechargeLoading"
          @click="handleQuickRecharge"
          style="margin-left: 12px"
        >
          <el-icon><Wallet /></el-icon> 充值
        </el-button>
      </div>
    </el-card>

    <!-- Transaction table -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.type === 'RECHARGE' ? 'success' : row.type === 'REFUND' ? 'warning' : ''"
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
            <el-button type="primary" size="small" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="fetchRecords"
        @current-change="fetchRecords"
      />
    </el-card>

    <!-- Detail Dialog -->
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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)

const search = reactive({
  userId: '',
  type: '' as string,
  status: '' as string,
  dateRange: null as [Date, Date] | null
})

const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

async function fetchRecords() {
  loading.value = true
  try {
    const params: any = { page: pagination.page, pageSize: pagination.pageSize }
    if (search.userId) params.userId = search.userId
    if (search.type) params.type = search.type
    if (search.status) params.status = search.status
    if (search.dateRange) {
      params.startDate = search.dateRange[0]?.toISOString()
      params.endDate = search.dateRange[1]?.toISOString()
    }

    const res: any = await client.get('/billing/transactions', { params })
    if (res) {
      tableData.value = res.list || res.records || []
      pagination.total = res.total ?? 0
    }
  } catch {
    // silently fail
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  search.userId = ''
  search.type = ''
  search.status = ''
  search.dateRange = null
  fetchRecords()
}

function typeLabel(type: string): string {
  const map: Record<string, string> = { RECHARGE: '充值', CONSUME: '消费', REFUND: '退款' }
  return map[type] || type
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { SUCCESS: '成功', FAILED: '失败', PENDING: '待处理' }
  return map[status] || status
}

// Quick Recharge
const quickRechargeLoading = ref(false)
const quickRecharge = reactive({ userId: null as number | null, amount: 100, remark: '' })

async function handleQuickRecharge() {
  if (!quickRecharge.userId || quickRecharge.userId < 1) {
    ElMessage.warning('请输入有效的用户 ID')
    return
  }
  if (!quickRecharge.amount || quickRecharge.amount < 1) {
    ElMessage.warning('请输入有效的充值数量')
    return
  }
  quickRechargeLoading.value = true
  try {
    await client.post('/billing/recharge', {
      userId: quickRecharge.userId,
      amount: quickRecharge.amount,
      remark: quickRecharge.remark
    })
    ElMessage.success('充值成功')
    quickRecharge.remark = ''
    fetchRecords()
  } catch (err: any) {
    const msg = err.response?.data?.message || '充值失败'
    ElMessage.error(msg)
  } finally {
    quickRechargeLoading.value = false
  }
}

// Detail
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

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.recharge-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.recharge-label {
  font-weight: bold;
  margin-right: 8px;
  color: #303133;
}
</style>
