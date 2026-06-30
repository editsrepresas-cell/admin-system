<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type TreeInstance } from 'element-plus'
import {
  createRole,
  deleteRole,
  getPermissionTree,
  getRoleList,
  getRolePermissions,
  updateRole,
  updateRolePermissions,
  type PermissionItem,
  type RoleItem,
} from '../../api/role'
import { hasPermission } from '../../utils/permission'

const loading = ref(false)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const submitLoading = ref(false)
const permissionSubmitLoading = ref(false)
const roleList = ref<RoleItem[]>([])
const permissionTree = ref<PermissionItem[]>([])
const checkedPermissionIds = ref<number[]>([])
const currentRole = ref<RoleItem | null>(null)
const formRef = ref<FormInstance>()
const treeRef = ref<TreeInstance>()
const editingId = ref<number | null>(null)

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canCreate = computed(() => hasPermission('role:create') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canEdit = computed(() => hasPermission('role:update') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canDelete = computed(() => hasPermission('role:delete') || roleCode.value === 'SUPER_ADMIN')
const canConfigPermission = computed(() => hasPermission('role:permission') || roleCode.value === 'SUPER_ADMIN')
const canShowActions = computed(() => canEdit.value || canConfigPermission.value || canDelete.value)

const dialogTitle = computed(() => (editingId.value ? '编辑角色' : '新增角色'))

const form = reactive({
  roleCode: '',
  roleName: '',
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    {
      pattern: /^[A-Z_]{2,30}$/,
      message: '角色编码只能使用大写字母和下划线',
      trigger: 'blur',
    },
  ],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const treeProps = {
  label: 'permissionName',
  children: 'children',
}

const loadRoleList = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.roleCode = ''
  form.roleName = ''
  form.sort = roleList.value.length + 1
  form.status = 1
  formRef.value?.clearValidate()
}

const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: RoleItem) => {
  editingId.value = row.id
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true

  try {
    if (editingId.value) {
      await updateRole(editingId.value, {
        roleName: form.roleName,
        sort: form.sort,
        status: form.status,
      })
      ElMessage.success('角色更新成功')
    } else {
      await createRole({ ...form })
      ElMessage.success('角色新增成功')
    }

    dialogVisible.value = false
    await loadRoleList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: RoleItem) => {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })

  await deleteRole(row.id)
  ElMessage.success('角色删除成功')
  await loadRoleList()
}

const handlePermission = async (row: RoleItem) => {
  currentRole.value = row
  permissionDialogVisible.value = true

  const [treeRes, checkedRes] = await Promise.all([
    getPermissionTree(),
    getRolePermissions(row.id),
  ])

  permissionTree.value = treeRes.data
  checkedPermissionIds.value = checkedRes.data

  setTimeout(() => {
    treeRef.value?.setCheckedKeys(checkedPermissionIds.value)
  })
}

const handleTreeCheck = () => {
  const checkedKeys = treeRef.value?.getCheckedKeys(false) || []
  checkedPermissionIds.value = checkedKeys.map((key) => Number(key))
}

const handlePermissionSubmit = async () => {
  if (!currentRole.value) {
    return
  }

  handleTreeCheck()
  permissionSubmitLoading.value = true
  try {
    await updateRolePermissions(currentRole.value.id, checkedPermissionIds.value)
    ElMessage.success('权限配置成功')
    permissionDialogVisible.value = false
  } finally {
    permissionSubmitLoading.value = false
  }
}

onMounted(() => {
  loadRoleList()
})
</script>

<template>
  <div class="roles-page">
    <div class="page-header">
      <h1>角色管理</h1>
      <el-button v-if="canCreate" type="primary" @click="handleCreate">
        新增角色
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="roleList" border stripe>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="roleCode" label="角色编码" min-width="180" />
        <el-table-column prop="roleName" label="角色名称" min-width="180" />
        <el-table-column prop="sort" label="排序" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canShowActions" label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canEdit" link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="canConfigPermission"
              link
              type="warning"
              @click="handlePermission(row)"
            >
              权限配置
            </el-button>
            <el-button
              v-if="canDelete"
              link
              type="danger"
              :disabled="row.id <= 4"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            :disabled="Boolean(editingId)"
            placeholder="例如 FINANCE_MANAGER"
          />
        </el-form-item>

        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
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
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="permissionDialogVisible"
      :title="`权限配置：${currentRole?.roleName || ''}`"
      width="640px"
    >
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="treeProps"
        node-key="id"
        show-checkbox
        default-expand-all
        @check="handleTreeCheck"
      />

      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="permissionSubmitLoading"
          @click="handlePermissionSubmit"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.roles-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.table-card {
  width: 100%;
}
</style>
