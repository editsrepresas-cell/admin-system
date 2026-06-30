<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createPermission,
  deletePermission,
  getPermissionTree,
  updatePermission,
  type PermissionItem,
  type PermissionSaveDTO,
} from '../../api/role'
import { hasPermission } from '../../utils/permission'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const permissionTree = ref<PermissionItem[]>([])
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = reactive<PermissionSaveDTO>({
  parentId: 0,
  permissionCode: '',
  permissionName: '',
  permissionType: 'MENU',
  sort: 1,
  status: 1,
})

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canCreate = computed(() => hasPermission('permission:create') || roleCode.value === 'SUPER_ADMIN')
const canEdit = computed(() => hasPermission('permission:update') || roleCode.value === 'SUPER_ADMIN')
const canDelete = computed(() => hasPermission('permission:delete') || roleCode.value === 'SUPER_ADMIN')
const canShowActions = computed(() => canCreate.value || canEdit.value || canDelete.value)
const dialogTitle = computed(() => (editingId.value ? '编辑权限' : '新增权限'))

const total = computed(() => {
  const count = (items: PermissionItem[]): number => {
    return items.reduce((sum, item) => sum + 1 + count(item.children || []), 0)
  }

  return count(permissionTree.value)
})

const parentOptions = computed(() => [
  {
    id: 0,
    permissionName: '根权限',
    children: permissionTree.value,
  },
])

const rules: FormRules = {
  parentId: [{ required: true, message: '请选择上级权限', trigger: 'change' }],
  permissionCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { max: 100, message: '权限编码不能超过100个字符', trigger: 'blur' },
  ],
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { max: 50, message: '权限名称不能超过50个字符', trigger: 'blur' },
  ],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const loadPermissionTree = async () => {
  loading.value = true
  try {
    const res = await getPermissionTree()
    permissionTree.value = res.data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.parentId = 0
  form.permissionCode = ''
  form.permissionName = ''
  form.permissionType = 'MENU'
  form.sort = 1
  form.status = 1
  formRef.value?.clearValidate()
}

const openCreateDialog = (parent?: PermissionItem) => {
  resetForm()
  form.parentId = parent?.id || 0
  form.permissionType = parent ? 'BUTTON' : 'MENU'
  dialogVisible.value = true
}

const openEditDialog = (row: PermissionItem) => {
  editingId.value = row.id
  form.parentId = row.parentId || 0
  form.permissionCode = row.permissionCode
  form.permissionName = row.permissionName
  form.permissionType = row.permissionType
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitLoading.value = true
  try {
    if (editingId.value) {
      await updatePermission(editingId.value, { ...form })
      ElMessage.success('权限更新成功')
    } else {
      await createPermission({ ...form })
      ElMessage.success('权限新增成功')
    }

    dialogVisible.value = false
    await loadPermissionTree()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: PermissionItem) => {
  await ElMessageBox.confirm(`确认删除权限「${row.permissionName}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })

  await deletePermission(row.id)
  ElMessage.success('权限删除成功')
  await loadPermissionTree()
}

onMounted(() => {
  loadPermissionTree()
})
</script>

<template>
  <div class="permissions-page">
    <div class="page-header">
      <div>
        <h1>权限管理</h1>
        <p>维护系统菜单权限和按钮权限，角色授权会使用这些权限点。</p>
      </div>
      <div class="header-actions">
        <el-tag size="large" type="info">共 {{ total }} 个权限点</el-tag>
        <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreateDialog()">
          新增根权限
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="permissionTree"
        row-key="id"
        border
        stripe
        default-expand-all
      >
        <el-table-column prop="permissionName" label="权限名称" min-width="220" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="220" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.permissionType === 'MENU' ? 'primary' : 'warning'">
              {{ row.permissionType === 'MENU' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canShowActions" label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canCreate" link type="success" @click="openCreateDialog(row)">
              新增下级
            </el-button>
            <el-button v-if="canEdit" link type="primary" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button v-if="canDelete" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级权限" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'permissionName', value: 'id', children: 'children' }"
            check-strictly
            default-expand-all
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-radio-group v-model="form.permissionType">
            <el-radio value="MENU">菜单</el-radio>
            <el-radio value="BUTTON">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.permissions-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.page-header p {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-card {
  width: 100%;
}
</style>
