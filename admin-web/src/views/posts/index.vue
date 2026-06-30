<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  createPost,
  deletePost,
  getPostList,
  updatePost,
  type PostItem,
} from '../../api/post'
import { hasPermission } from '../../utils/permission'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const postList = ref<PostItem[]>([])
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canCreate = computed(() => hasPermission('post:create') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canEdit = computed(() => hasPermission('post:update') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canDelete = computed(() => hasPermission('post:delete') || roleCode.value === 'SUPER_ADMIN')
const canShowActions = computed(() => canEdit.value || canDelete.value)
const dialogTitle = computed(() => (editingId.value ? '编辑岗位' : '新增岗位'))

const form = reactive({
  postCode: '',
  postName: '',
  sort: 0,
  status: 1,
})

const rules: FormRules = {
  postCode: [
    { required: true, message: '请输入岗位编码', trigger: 'blur' },
    {
      pattern: /^[A-Z_]{2,30}$/,
      message: '岗位编码只能使用大写字母和下划线',
      trigger: 'blur',
    },
  ],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const loadPostList = async () => {
  loading.value = true
  try {
    const res = await getPostList()
    postList.value = res.data
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.postCode = ''
  form.postName = ''
  form.sort = postList.value.length + 1
  form.status = 1
  formRef.value?.clearValidate()
}

const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: PostItem) => {
  editingId.value = row.id
  form.postCode = row.postCode
  form.postName = row.postName
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true

  try {
    if (editingId.value) {
      await updatePost(editingId.value, {
        postName: form.postName,
        sort: form.sort,
        status: form.status,
      })
      ElMessage.success('岗位更新成功')
    } else {
      await createPost({ ...form })
      ElMessage.success('岗位新增成功')
    }

    dialogVisible.value = false
    await loadPostList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row: PostItem) => {
  await ElMessageBox.confirm(`确认删除岗位「${row.postName}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })

  await deletePost(row.id)
  ElMessage.success('岗位删除成功')
  await loadPostList()
}

onMounted(() => {
  loadPostList()
})
</script>

<template>
  <div class="posts-page">
    <div class="page-header">
      <h1>岗位管理</h1>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="handleCreate">
        新增岗位
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="postList" border stripe>
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="postCode" label="岗位编码" min-width="180" />
        <el-table-column prop="postName" label="岗位名称" min-width="180" />
        <el-table-column prop="sort" label="排序" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canShowActions" label="操作" width="180" fixed="right">
          <template #default="{ row }">
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="岗位编码" prop="postCode">
          <el-input
            v-model="form.postCode"
            :disabled="Boolean(editingId)"
            placeholder="例如 PROJECT_MANAGER"
          />
        </el-form-item>

        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="form.postName" placeholder="请输入岗位名称" />
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
.posts-page {
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
