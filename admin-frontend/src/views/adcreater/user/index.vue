<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :model="queryParams" :inline="true" label-width="68px" class="-mb-15px">
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名"
          clearable
          class="!w-180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入昵称"
          clearable
          class="!w-180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入手机号"
          clearable
          class="!w-180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          class="!w-120px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" />重置</el-button>
        <el-button type="primary" plain @click="openForm()">
          <Icon icon="ep:plus" />新增用户
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="tableData" stripe border>
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
          <el-button type="primary" size="small" link @click="openForm(row)">
            <Icon icon="ep:edit" />编辑
          </el-button>
          <el-button type="warning" size="small" link @click="openRecharge(row)">
            <Icon icon="ep:wallet" />充值
          </el-button>
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
    <Pagination
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.pageSize"
      @pagination="fetchUsers"
    />
  </ContentWrap>

  <UserForm ref="userFormRef" @success="fetchUsers" />
  <RechargeForm ref="rechargeFormRef" @success="fetchUsers" />
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { getUserPage, updateUserStatus } from '@/api/adcreater/user'
import UserForm from './UserForm.vue'
import RechargeForm from './RechargeForm.vue'

defineOptions({ name: 'AdCreaterUser' })

const message = useMessage()

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  page: 1,
  pageSize: 20,
  username: '',
  nickname: '',
  mobile: '',
  status: '' as number | ''
})

async function fetchUsers() {
  loading.value = true
  try {
    const params: any = { page: queryParams.page, pageSize: queryParams.pageSize }
    if (queryParams.username) params.username = queryParams.username
    if (queryParams.nickname) params.nickname = queryParams.nickname
    if (queryParams.mobile) params.mobile = queryParams.mobile
    if (queryParams.status !== '') params.status = queryParams.status

    const res = await getUserPage(params)
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
  fetchUsers()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const userFormRef = ref()
function openForm(row?: any) {
  userFormRef.value.open(row)
}

const rechargeFormRef = ref()
function openRecharge(row: any) {
  rechargeFormRef.value.open(row)
}

async function toggleStatus(row: any) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await message.confirm(`确定要${action}该用户吗？`)
    await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
    message.success(`${action}成功`)
    fetchUsers()
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  fetchUsers()
})
</script>
