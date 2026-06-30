<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, View } from '@element-plus/icons-vue'
import {
  createNotice,
  deleteNotice,
  getNoticeDetail,
  getNoticeList,
  offlineNotice,
  publishNotice,
  updateNotice,
  type NoticeItem,
} from '../../api/notice'
import { getDictDataOptions, type DictDataItem } from '../../api/dict'
import { hasPermission } from '../../utils/permission'

const route = useRoute()
const loading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const submitLoading = ref(false)
const detailLoading = ref(false)
const noticeList = ref<NoticeItem[]>([])
const noticeTypes = ref<DictDataItem[]>([])
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const currentNotice = ref<NoticeItem | null>(null)

const query = reactive({
  keyword: '',
  noticeType: '',
  status: undefined as number | undefined,
})

const form = reactive({
  title: '',
  noticeType: '',
  content: '',
  status: 0,
})

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canView = computed(() => hasPermission('notice:list') || ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'].includes(roleCode.value))
const canCreate = computed(() => hasPermission('notice:create') || ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'].includes(roleCode.value))
const canEdit = computed(() => hasPermission('notice:update') || ['SUPER_ADMIN', 'ADMIN', 'OPERATOR'].includes(roleCode.value))
const canDelete = computed(() => hasPermission('notice:delete') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canPublish = computed(() => hasPermission('notice:publish') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canShowActions = computed(() => canView.value || canEdit.value || canDelete.value || canPublish.value)
const dialogTitle = computed(() => (editingId.value ? '编辑公告' : '新增公告'))

const rules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 100, message: '公告标题不能超过 100 个字符', trigger: 'blur' },
  ],
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { max: 2000, message: '公告内容不能超过 2000 个字符', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择公告状态', trigger: 'change' }],
}

const loadNoticeTypes = async () => {
  const res = await getDictDataOptions('notice_type')
  noticeTypes.value = res.data
}

const loadNotices = async () => {
  loading.value = true
  try {
    const res = await getNoticeList({
      keyword: query.keyword || undefined,
      noticeType: query.noticeType || undefined,
      status: query.status,
    })
    noticeList.value = res.data
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.noticeType = ''
  query.status = undefined
  loadNotices()
}

const resetForm = () => {
  editingId.value = null
  form.title = ''
  form.noticeType = noticeTypes.value[0]?.dictValue || ''
  form.content = ''
  form.status = 0
  formRef.value?.clearValidate()
}

const handleCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: NoticeItem) => {
  editingId.value = row.id
  form.title = row.title
  form.noticeType = row.noticeType
  form.content = row.content
  form.status = row.status
  dialogVisible.value = true
}

const openDetail = async (id: number) => {
  detailDialogVisible.value = true
  detailLoading.value = true

  try {
    const res = await getNoticeDetail(id)
    currentNotice.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleView = (row: NoticeItem) => {
  openDetail(row.id)
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true

  try {
    if (editingId.value) {
      await updateNotice(editingId.value, { ...form })
      ElMessage.success('公告更新成功')
    } else {
      await createNotice({ ...form })
      ElMessage.success('公告新增成功')
    }

    dialogVisible.value = false
    await loadNotices()
  } finally {
    submitLoading.value = false
  }
}

const handlePublish = async (row: NoticeItem) => {
  await ElMessageBox.confirm(`确认发布公告「${row.title}」吗？`, '发布确认', {
    type: 'warning',
    confirmButtonText: '发布',
    cancelButtonText: '取消',
  })
  await publishNotice(row.id)
  ElMessage.success('公告发布成功')
  await loadNotices()
}

const handleOffline = async (row: NoticeItem) => {
  await ElMessageBox.confirm(`确认下线公告「${row.title}」吗？`, '下线确认', {
    type: 'warning',
    confirmButtonText: '下线',
    cancelButtonText: '取消',
  })
  await offlineNotice(row.id)
  ElMessage.success('公告下线成功')
  await loadNotices()
}

const handleDelete = async (row: NoticeItem) => {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteNotice(row.id)
  ElMessage.success('公告删除成功')
  noticeList.value = noticeList.value.filter((item) => item.id !== row.id)
}

onMounted(async () => {
  await loadNoticeTypes()
  await loadNotices()

  const noticeId = Number(route.query.noticeId)
  if (Number.isFinite(noticeId) && noticeId > 0) {
    openDetail(noticeId)
  }
})

watch(
  () => route.query.noticeId,
  (value) => {
    const noticeId = Number(value)
    if (Number.isFinite(noticeId) && noticeId > 0) {
      openDetail(noticeId)
    }
  },
)
</script>

<template>
  <div class="notice-page">
    <div class="page-header">
      <h1>通知公告</h1>
      <el-button v-if="canCreate" type="primary" :icon="Plus" @click="handleCreate">
        新增公告
      </el-button>
    </div>

    <div class="toolbar-panel">
      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="公告类型">
          <el-select v-model="query.noticeType" clearable placeholder="全部类型" class="type-select">
            <el-option
              v-for="item in noticeTypes"
              :key="item.id"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" class="status-select">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="标题或内容" class="keyword-input" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadNotices">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-panel">
      <el-table v-loading="loading" :data="noticeList" border>
        <el-table-column prop="title" label="公告标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="noticeTypeName" label="公告类型" width="140" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column v-if="canShowActions" label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canView" link type="primary" :icon="View" @click="handleView(row)">
              查看
            </el-button>
            <el-button v-if="canEdit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="canPublish && row.status === 0"
              link
              type="success"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="canPublish && row.status === 1"
              link
              type="warning"
              @click="handleOffline(row)"
            >
              下线
            </el-button>
            <el-button v-if="canDelete" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告类型" prop="noticeType">
          <el-select v-model="form.noticeType" placeholder="请选择公告类型">
            <el-option
              v-for="item in noticeTypes"
              :key="item.id"
              :label="item.dictLabel"
              :value="item.dictValue"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="公告状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="请输入公告内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="公告详情" width="720px">
      <div v-loading="detailLoading" class="notice-detail">
        <template v-if="currentNotice">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="公告标题" :span="2">
              {{ currentNotice.title }}
            </el-descriptions-item>
            <el-descriptions-item label="公告类型">
              {{ currentNotice.noticeTypeName }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentNotice.status === 1 ? 'success' : 'info'">
                {{ currentNotice.statusName }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ currentNotice.publishTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ currentNotice.createTime || '-' }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="detail-content">
            {{ currentNotice.content }}
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.notice-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 26px;
  line-height: 1.3;
}

.toolbar-panel,
.table-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.toolbar-panel {
  padding: 20px 24px 2px;
}

.table-panel {
  padding: 24px;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.type-select,
.keyword-input {
  width: 240px;
}

.status-select {
  width: 160px;
}

:deep(.el-dialog__body) {
  padding-bottom: 8px;
}

.notice-detail {
  min-height: 220px;
}

.detail-content {
  margin-top: 18px;
  min-height: 140px;
  padding: 16px;
  color: #1f2937;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
}
</style>
