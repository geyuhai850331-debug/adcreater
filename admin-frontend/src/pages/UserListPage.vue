<template>
  <div class="user-list-page">
    <el-card shadow="never">
      <div class="search-bar">
        <el-input
          v-model="search.username"
          placeholder="用户名"
          clearable
          style="width: 180px"
          @keyup.enter="fetchUsers"
        />
        <el-input
          v-model="search.nickname"
          placeholder="昵称"
          clearable
          style="width: 180px; margin-left: 12px"
          @keyup.enter="fetchUsers"
        />
        <el-input
          v-model="search.mobile"
          placeholder="手机号"
          clearable
          style="width: 180px; margin-left: 12px"
          @keyup.enter="fetchUsers"
        />
        <el-select
          v-model="search.status"
          placeholder="状态"
          clearable
          style="width: 120px; margin-left: 12px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchUsers" style="margin-left: 12px">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="openEdit()" style="margin-left: auto">
          <el-icon><Plus /></el-icon> 新增用户
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="mobile" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
        <el-table-column prop="points" label="点数" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" link @click="openRecharge(row)">充值</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              link
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
        @size-change="fetchUsers"
        @current-change="fetchUsers"
      />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editVisible"
      :title="isEditing ? '编辑用户' : '新增用户'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" :disabled="isEditing" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEditing ? '' : 'password'">
          <el-input
            v-model="editForm.password"
            type="password"
            :placeholder="isEditing ? '留空则不修改' : '请输入密码'"
            show-password
          />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="editForm.mobile" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="editForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Recharge Dialog -->
    <el-dialog
      v-model="rechargeVisible"
      title="用户充值"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="80px">
        <el-form-item label="用户 ID">
          <el-input :model-value="rechargeForm.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input :model-value="rechargeForm.username" disabled />
        </el-form-item>
        <el-form-item label="充值数量" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="999999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="rechargeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import client from '@/api/client'

const loading = ref(false)

const search = reactive({ username: '', nickname: '', mobile: '', status: '' as number | '' })
const tableData = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

async function fetchUsers() {
  loading.value = true
  try {
    const params: any = { page: pagination.page, pageSize: pagination.pageSize }
    if (search.username) params.username = search.username
    if (search.nickname) params.nickname = search.nickname
    if (search.mobile) params.mobile = search.mobile
    if (search.status !== '') params.status = search.status

    const res: any = await client.get('/system/users', { params })
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
  search.username = ''
  search.nickname = ''
  search.mobile = ''
  search.status = ''
  fetchUsers()
}

// --- Edit ---
const editVisible = ref(false)
const isEditing = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  password: '',
  mobile: '',
  email: '',
  status: 1
})

const editRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  mobile: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

function openEdit(row?: any) {
  if (row) {
    isEditing.value = true
    editForm.id = row.id
    editForm.username = row.username || ''
    editForm.nickname = row.nickname || ''
    editForm.password = ''
    editForm.mobile = row.mobile || ''
    editForm.email = row.email || ''
    editForm.status = row.status
  } else {
    isEditing.value = false
    editForm.id = null
    editForm.username = ''
    editForm.nickname = ''
    editForm.password = ''
    editForm.mobile = ''
    editForm.email = ''
    editForm.status = 1
  }
  editVisible.value = true
}

async function handleEditSubmit() {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async valid => {
    if (!valid) return
    editLoading.value = true
    try {
      const payload: any = {
        username: editForm.username,
        nickname: editForm.nickname,
        mobile: editForm.mobile,
        email: editForm.email,
        status: editForm.status
      }
      if (editForm.password) payload.password = editForm.password

      if (isEditing.value && editForm.id) {
        await client.put(`/system/users/${editForm.id}`, payload)
        ElMessage.success('用户更新成功')
      } else {
        await client.post('/system/users', payload)
        ElMessage.success('用户创建成功')
      }
      editVisible.value = false
      fetchUsers()
    } catch (err: any) {
      const msg = err.response?.data?.message || '操作失败'
      ElMessage.error(msg)
    } finally {
      editLoading.value = false
    }
  })
}

// --- Recharge ---
const rechargeVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeFormRef = ref<FormInstance>()
const rechargeForm = reactive({ userId: 0, username: '', amount: 100, remark: '' })
const rechargeRules: FormRules = {
  amount: [{ required: true, message: '请输入充值数量', trigger: 'blur' }]
}

function openRecharge(row: any) {
  rechargeForm.userId = row.id
  rechargeForm.username = row.username || ''
  rechargeForm.amount = 100
  rechargeForm.remark = ''
  rechargeVisible.value = true
}

async function handleRecharge() {
  if (!rechargeFormRef.value) return
  await rechargeFormRef.value.validate(async valid => {
    if (!valid) return
    rechargeLoading.value = true
    try {
      await client.post('/billing/recharge', {
        userId: rechargeForm.userId,
        amount: rechargeForm.amount,
        remark: rechargeForm.remark
      })
      ElMessage.success('充值成功')
      rechargeVisible.value = false
      fetchUsers()
    } catch (err: any) {
      const msg = err.response?.data?.message || '充值失败'
      ElMessage.error(msg)
    } finally {
      rechargeLoading.value = false
    }
  })
}

// --- Toggle Status ---
async function toggleStatus(row: any) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await client.put(`/system/users/${row.id}/status`, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
}
</style>
