<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Download, Plus, Search } from '@element-plus/icons-vue'
import { getDeptTree, type DeptItem } from '../../api/dept'
import { getPostOptions, type PostItem } from '../../api/post'
import {
  createUser,
  deleteUser,
  exportUserList,
  getRoleList,
  getUserList,
  resetUserPassword,
  updateUser,
  type RoleItem,
  type UserItem,
} from '../../api/user'
import { hasPermission } from '../../utils/permission'

const keyword = ref('')
const loading = ref(false)
const exportLoading = ref(false)
const userList = ref<UserItem[]>([])
const roleList = ref<RoleItem[]>([])
const deptTree = ref<DeptItem[]>([])
const postList = ref<PostItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const dialogMode = ref<'create' | 'edit'>('create')
const currentUserId = ref<number | null>(null)

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '新增用户' : '编辑用户'))

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canCreate = computed(() => hasPermission('user:create') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canEdit = computed(() => hasPermission('user:update') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canResetPassword = computed(() => hasPermission('user:reset-password') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canDelete = computed(() => hasPermission('user:delete') || roleCode.value === 'SUPER_ADMIN')
const canShowActions = computed(() => canEdit.value || canResetPassword.value || canDelete.value)

const form = ref({
  username: '',
  password: '',
  nickname: '',
  deptId: null as number | null,
  postId: null as number | null,
  roleId: null as number | null,
  phone: '',
  email: '',
  status: 1,
})

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (dialogMode.value === 'edit') {
    callback()
    return
  }

  if (!value) {
    callback(new Error('请输入密码'))
    return
  }

  if (value.length < 6 || value.length > 30) {
    callback(new Error('密码长度必须为 6 到 30 个字符'))
    return
  }

  callback()
}

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须为 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  postId: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

const loadUserList = async () => {
  loading.value = true

  try {
    const res = await getUserList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
    })

    userList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadRoleList = async () => {
  const res = await getRoleList()
  roleList.value = res.data
}

const loadDeptTree = async () => {
  const res = await getDeptTree()
  deptTree.value = res.data
}

const loadPostList = async () => {
  const res = await getPostOptions()
  postList.value = res.data
}

const handleCurrentChange = (value: number) => {
  pageNum.value = value
  loadUserList()
}

const handleSizeChange = (value: number) => {
  pageSize.value = value
  pageNum.value = 1
  loadUserList()
}

const handleSearch = () => {
  pageNum.value = 1
  loadUserList()
}

const handleReset = () => {
  keyword.value = ''
  pageNum.value = 1
  loadUserList()
}

const handleExport = async () => {
  exportLoading.value = true

  try {
    const blob = await exportUserList({ keyword: keyword.value }) as unknown as Blob
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    const date = new Date().toISOString().slice(0, 10)

    link.href = url
    link.download = `用户列表-${date}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } finally {
    exportLoading.value = false
  }
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  currentUserId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row: UserItem) => {
  dialogMode.value = 'edit'
  currentUserId.value = row.id
  form.value = {
    username: row.username,
    password: '',
    nickname: row.nickname,
    deptId: row.deptId ?? null,
    postId: row.postId ?? null,
    roleId: row.roleId ?? null,
    phone: row.phone,
    email: row.email,
    status: row.status,
  }
  dialogVisible.value = true
}

const resetForm = () => {
  form.value = {
    username: '',
    password: '',
    nickname: '',
    deptId: null,
    postId: null,
    roleId: null,
    phone: '',
    email: '',
    status: 1,
  }
  currentUserId.value = null
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitLoading.value = true

  try {
    if (dialogMode.value === 'create') {
      await createUser(form.value)
      ElMessage.success('新增用户成功')
      pageNum.value = 1
    } else if (currentUserId.value) {
      await updateUser(currentUserId.value, {
        nickname: form.value.nickname,
        deptId: form.value.deptId,
        postId: form.value.postId,
        roleId: form.value.roleId,
        phone: form.value.phone,
        email: form.value.email,
        status: form.value.status,
      })
      ElMessage.success('编辑用户成功')
    }

    dialogVisible.value = false
    resetForm()
    loadUserList()
  } finally {
    submitLoading.value = false
  }
}

const handleResetPassword = async (row: UserItem) => {
  await ElMessageBox.confirm(
    `确认将用户「${row.username}」的密码重置为 123456 吗？`,
    '重置密码确认',
    {
      type: 'warning',
      confirmButtonText: '重置',
      cancelButtonText: '取消',
    },
  )

  await resetUserPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

const handleDelete = async (row: UserItem) => {
  await ElMessageBox.confirm(
    `确认删除用户「${row.username}」吗？`,
    '删除确认',
    {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    },
  )

  await deleteUser(row.id)
  ElMessage.success('删除用户成功')

  if (userList.value.length === 1 && pageNum.value > 1) {
    pageNum.value -= 1
  }

  loadUserList()
}

onMounted(() => {
  loadUserList()

  if (canCreate.value || canEdit.value) {
    loadRoleList()
    loadDeptTree()
    loadPostList()
  }
})
</script>

<template>
  <div class="users-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreateDialog">
        新增用户
      </el-button>
    </div>

    <el-card class="search-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            placeholder="请输入用户名、姓名或手机号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button
            :icon="Download"
            :loading="exportLoading"
            @click="handleExport"
          >
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" fixed="left" />
        <el-table-column prop="username" label="用户名" min-width="150" fixed="left" />
        <el-table-column prop="nickname" label="姓名" min-width="130" />
        <el-table-column prop="deptName" label="部门" min-width="150">
          <template #default="{ row }">
            {{ row.deptName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="postName" label="岗位" min-width="130">
          <template #default="{ row }">
            {{ row.postName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="roleName" label="角色" min-width="130">
          <template #default="{ row }">
            <el-tag>{{ row.roleName || '未分配' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canShowActions" label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEdit" type="primary" link @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button v-if="canResetPassword" type="warning" link @click="handleResetPassword(row)">
              重置密码
            </el-button>
            <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>

        <el-form-item
          v-if="dialogMode === 'create'"
          label="密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="姓名" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            default-expand-all
            placeholder="请选择部门"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="岗位" prop="postId">
          <el-select
            v-model="form.postId"
            placeholder="请选择岗位"
            style="width: 100%"
          >
            <el-option
              v-for="post in postList"
              :key="post.id"
              :label="post.postName"
              :value="post.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="角色" prop="roleId">
          <el-select
            v-model="form.roleId"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.users-page h2 {
  margin: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  overflow: hidden;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
