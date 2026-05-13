<template>
  <div class="billing-page">
    <h2 class="page-title">点数充值</h2>

    <el-row :gutter="20">
      <!-- Balance Card -->
      <el-col :span="8">
        <el-card class="balance-card">
          <div class="balance-display">
            <p class="balance-label">当前余额</p>
            <p class="balance-amount">{{ balance }}</p>
            <p class="balance-unit">点数</p>
            <el-progress
              :percentage="balancePercentage"
              :color="balanceColor"
              :show-text="false"
              style="margin: 12px 0"
            />
            <el-button type="primary" size="large" class="recharge-btn" @click="showRechargeDialog">
              立即充值
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- Transaction History -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>交易记录</span>
          </template>

          <el-table
            :data="transactions"
            stripe
            v-loading="loadingTransactions"
            style="width: 100%"
          >
            <el-table-column prop="id" label="交易编号" width="180" />
            <el-table-column prop="amount" label="点数" width="100">
              <template #default="{ row }">
                <span :style="{ color: row.type === 'CHARGE' ? '#67c23a' : '#f56c6c' }">
                  {{ row.type === 'CHARGE' ? '+' : '-' }}{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'CHARGE' ? 'success' : 'warning'" size="small">
                  {{ row.type === 'CHARGE' ? '充值' : '消费' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column prop="createdAt" label="时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>

          <!-- Pagination -->
          <div class="pagination-wrap" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="fetchTransactions"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- How to Recharge -->
    <el-card class="guide-card">
      <template #header>
        <span>如何充值</span>
      </template>
      <el-steps direction="vertical" :space="40">
        <el-step>
          <template #title>选择充值套餐</template>
          <template #description>
            点击"立即充值"按钮，在弹出的对话框中选择适合的套餐
          </template>
        </el-step>
        <el-step>
          <template #title>完成支付</template>
          <template #description>
            支持支付宝、微信支付，扫码即可完成支付
          </template>
        </el-step>
        <el-step>
          <template #title>点数到账</template>
          <template #description>
            支付成功后点数将自动到账，可在工作台查看余额
          </template>
        </el-step>
      </el-steps>
    </el-card>

    <!-- Recharge Dialog -->
    <el-dialog v-model="rechargeVisible" title="选择充值套餐" width="500px">
      <div class="package-grid">
        <div
          v-for="pkg in packages"
          :key="pkg.id"
          class="package-item"
          :class="{ active: selectedPackage === pkg.id }"
          @click="selectedPackage = pkg.id"
        >
          <div class="package-points">{{ pkg.points }}</div>
          <div class="package-price">{{ pkg.price }}</div>
          <div class="package-bonus" v-if="pkg.bonus">赠送 {{ pkg.bonus }} 点</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="recharging"
          :disabled="!selectedPackage"
          @click="handleRecharge"
        >
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'

const balance = ref(0)
const balancePercentage = computed(() => Math.min((balance.value / 1000) * 100, 100))
const balanceColor = computed(() => {
  if (balance.value < 100) return '#f56c6c'
  if (balance.value < 500) return '#e6a23c'
  return '#67c23a'
})

const transactions = ref<any[]>([])
const loadingTransactions = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const rechargeVisible = ref(false)
const selectedPackage = ref('')
const recharging = ref(false)

const packages = [
  { id: 'p1', points: 100, price: 10, bonus: 0 },
  { id: 'p2', points: 500, price: 45, bonus: 50 },
  { id: 'p3', points: 1000, price: 85, bonus: 150 },
  { id: 'p4', points: 2000, price: 160, bonus: 400 },
  { id: 'p5', points: 5000, price: 380, bonus: 1200 },
  { id: 'p6', points: 10000, price: 720, bonus: 3000 }
]

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchBalance() {
  try {
    const res = await client.get('/billing/balance') as any
    balance.value = res?.balance ?? res?.data?.balance ?? 0
  } catch {
    // ignore
  }
}

async function fetchTransactions() {
  loadingTransactions.value = true
  try {
    const res = await client.get('/billing/transaction/page', {
      params: { page: currentPage.value, pageSize: pageSize.value }
    }) as any
    const data = res?.data ?? res
    transactions.value = data?.records ?? data?.list ?? []
    total.value = data?.total ?? 0
  } catch {
    transactions.value = []
  } finally {
    loadingTransactions.value = false
  }
}

function showRechargeDialog() {
  selectedPackage.value = ''
  rechargeVisible.value = true
}

async function handleRecharge() {
  if (!selectedPackage.value) return
  const pkg = packages.find((p) => p.id === selectedPackage.value)
  if (!pkg) return

  recharging.value = true
  try {
    const res = await client.post('/billing/recharge', {
      packageId: pkg.id,
      points: pkg.points,
      amount: pkg.price
    }) as any

    const orderUrl = res?.payUrl || res?.data?.payUrl
    if (orderUrl) {
      // Open payment URL in browser
      window.open(orderUrl, '_blank')
      ElMessage.info('请在新窗口中完成支付')
    } else {
      ElMessage.success('充值成功')
    }
    rechargeVisible.value = false
    await fetchBalance()
  } catch (err: any) {
    ElMessage.error('充值失败: ' + (err?.message || '未知错误'))
  } finally {
    recharging.value = false
  }
}

onMounted(() => {
  fetchBalance()
  fetchTransactions()
})
</script>

<style scoped>
.billing-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 20px 0;
  color: #303133;
}

.balance-card {
  height: 100%;
}

.balance-display {
  text-align: center;
  padding: 20px 0;
}

.balance-label {
  color: #909399;
  margin: 0 0 8px;
  font-size: 14px;
}

.balance-amount {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 4px;
}

.balance-unit {
  color: #909399;
  font-size: 14px;
  margin: 0 0 8px;
}

.recharge-btn {
  margin-top: 16px;
  width: 200px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.guide-card {
  margin-top: 20px;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.package-item {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.package-item:hover {
  border-color: #409EFF;
}

.package-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.package-points {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.package-price {
  font-size: 16px;
  color: #f56c6c;
  margin: 4px 0;
}

.package-bonus {
  font-size: 12px;
  color: #67c23a;
}
</style>
