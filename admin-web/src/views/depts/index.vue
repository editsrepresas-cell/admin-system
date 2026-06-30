<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createDept,
  deleteDept,
  getDeptTree,
  updateDept,
  type DeptItem,
} from '../../api/dept'
import { hasPermission } from '../../utils/permission'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const deptTree = ref<DeptItem[]>([])
const keyword = ref('')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canCreate = computed(() => hasPermission('dept:create') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canEdit = computed(() => hasPermission('dept:update') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canDelete = computed(() => hasPermission('dept:delete') || roleCode.value === 'SUPER_ADMIN')
const canShowActions = computed(() => canCreate.value || canEdit.value || canDelete.value)
const dialogTitle = computed(() => (editingId.value ? '编辑部门' : '新增部门'))

const form = reactive({
  parentId: 0,
  deptName: '',
  leader: '',
  phone: '',
  email: '',
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  phone: [
    {
      pattern: /^$|^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur',
    },
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
}

const deptOptions = computed(() => [
  {
    id: 0,
    deptName: '顶级部门',
    children: deptTree.value,
  },
])

const filteredDeptTree = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) {
    return deptTree.value
  }

  const filterTree = (items: DeptItem[]): DeptItem[] => {
    return items
      .map((item) => {
        const children = filterTree(item.children || [])
        const hit = [item.deptName, item.leader, item.phone, item.email]
          .filter(Boolean)
          .some((field) => String(field).toLowerCase().includes(value))

        if (hit || children.length) {
          return { ...item, children }
        }

        return null
      })
      .filter(Boolean) as DeptItem[]
  }

  return filterTree(deptTree.value)
})

const loadDeptTree = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    deptTree.value = res.data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.parentId = 0
  form.deptName = ''
  form.leader = ''
  form.phone = ''
  form.email = ''
  form.sort = 0
  form.status = 1
  formRef.value?.clearValidate()
}

const handleCreate = (parent?: DeptItem) => {
  resetForm()
  form.parentId = parent?.id || 0
  dialogVisible.value = true
}

const handleEdit = (row: DeptItem) => {
  editingId.value = row.id
  form.parentId = row.parentId
  form.deptName = row.deptName
  form.leader = row.leader || ''
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true

  try {
    if (editingId.value) {
      await updateDept(editingId.value, { ...form })
      ElMessage.success('部门更新成功')
    } else {
      await createDept({ ...form })
      ElMessage.success('部门新增成功')
    }

    dialogVisible.value = false
    await loadDeptTree()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: DeptItem) => {
  await ElMessageBox.confirm(`确认删除部门「${row.deptName}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })

  await deleteDept(row.id)
  ElMessage.success('部门删除成功')
  await loadDeptTree()
}

const handleReset = () => {
  keyword.value = ''
}

onMounted(() => {
  loadDeptTree()
})
</script>

<template>
  <div class="depts-page">
    <div class="page-header">
      <h1>部门管理</h1>
      <el-button v-if="canCreate" type="primary" @click="handleCreate()">
        + 新增部门
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            clearable
            placeholder="请输入部门、负责人、手机号或邮箱"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="filteredDeptTree"
        border
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="220" />
        <el-table-column prop="leader" label="负责人" min-width="140" />
        <el-table-column prop="phone" label="联系电话" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canShowActions" label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canCreate" link type="primary" @click="handleCreate(row)">
              新增下级
            </el-button>
            <el-button v-if="canEdit" link type="primary" @click="handleEdit(row)">
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
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptOptions"
            node-key="id"
            check-strictly
            default-expand-all
            :props="{ label: 'deptName', children: 'children' }"
            placeholder="请选择上级部门"
          />
        </el-form-item>

        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>

        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" maxlength="11" show-word-limit placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
  </div>
</template>

<style scoped>
.depts-page {
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
  color: #111827;
  font-size: 24px;
  font-weight: 700;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.table-card {
  width: 100%;
}
</style>
