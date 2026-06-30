<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  getDictDataList,
  getDictTypeList,
  updateDictData,
  updateDictType,
  type DictDataItem,
  type DictTypeItem,
} from '../../api/dict'
import { hasPermission } from '../../utils/permission'

const typeLoading = ref(false)
const dataLoading = ref(false)
const typeDialogVisible = ref(false)
const dataDialogVisible = ref(false)
const typeSubmitLoading = ref(false)
const dataSubmitLoading = ref(false)

const dictTypes = ref<DictTypeItem[]>([])
const dictData = ref<DictDataItem[]>([])
const selectedTypeId = ref<number | null>(null)
const editingTypeId = ref<number | null>(null)
const editingDataId = ref<number | null>(null)
const typeFormRef = ref<FormInstance>()
const dataFormRef = ref<FormInstance>()

const typeQuery = reactive({
  keyword: '',
})

const dataQuery = reactive({
  keyword: '',
})

const typeForm = reactive({
  dictCode: '',
  dictName: '',
  sort: 0,
  status: 1,
})

const dataForm = reactive({
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
  remark: '',
})

const loginUser = computed(() => {
  const raw = localStorage.getItem('admin_user')
  return raw ? JSON.parse(raw) : {}
})

const roleCode = computed(() => loginUser.value.roleCode || '')
const canCreate = computed(() => hasPermission('dict:create') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canEdit = computed(() => hasPermission('dict:update') || ['SUPER_ADMIN', 'ADMIN'].includes(roleCode.value))
const canDelete = computed(() => hasPermission('dict:delete') || roleCode.value === 'SUPER_ADMIN')
const canShowTypeActions = computed(() => canEdit.value || canDelete.value)
const canShowDataActions = computed(() => canEdit.value || canDelete.value)
const selectedType = computed(() => dictTypes.value.find((item) => item.id === selectedTypeId.value))
const typeDialogTitle = computed(() => (editingTypeId.value ? '编辑字典类型' : '新增字典类型'))
const dataDialogTitle = computed(() => (editingDataId.value ? '编辑字典数据' : '新增字典数据'))

const typeRules: FormRules = {
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9_]{1,49}$/,
      message: '字典编码只能使用小写字母、数字和下划线，且必须以字母开头',
      trigger: 'blur',
    },
  ],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dataRules: FormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [
    { required: true, message: '请输入字典值', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9_-]{1,50}$/,
      message: '字典值只能使用字母、数字、下划线和短横线',
      trigger: 'blur',
    },
  ],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const loadTypes = async () => {
  typeLoading.value = true
  try {
    const res = await getDictTypeList({ keyword: typeQuery.keyword || undefined })
    dictTypes.value = res.data

    if (!selectedTypeId.value && dictTypes.value.length > 0) {
      selectedTypeId.value = dictTypes.value[0].id
    }

    if (selectedTypeId.value && !dictTypes.value.some((item) => item.id === selectedTypeId.value)) {
      selectedTypeId.value = dictTypes.value[0]?.id || null
    }
  } finally {
    typeLoading.value = false
  }
}

const loadData = async () => {
  if (!selectedTypeId.value) {
    dictData.value = []
    return
  }

  dataLoading.value = true
  try {
    const res = await getDictDataList({
      dictTypeId: selectedTypeId.value,
      keyword: dataQuery.keyword || undefined,
    })
    dictData.value = res.data
  } finally {
    dataLoading.value = false
  }
}

const reloadPage = async () => {
  await loadTypes()
  await loadData()
}

const handleSelectType = async (row: DictTypeItem) => {
  selectedTypeId.value = row.id
  dataQuery.keyword = ''
  await loadData()
}

const resetTypeForm = () => {
  editingTypeId.value = null
  typeForm.dictCode = ''
  typeForm.dictName = ''
  typeForm.sort = dictTypes.value.length + 1
  typeForm.status = 1
  typeFormRef.value?.clearValidate()
}

const resetDataForm = () => {
  editingDataId.value = null
  dataForm.dictLabel = ''
  dataForm.dictValue = ''
  dataForm.sort = dictData.value.length + 1
  dataForm.status = 1
  dataForm.remark = ''
  dataFormRef.value?.clearValidate()
}

const handleCreateType = () => {
  resetTypeForm()
  typeDialogVisible.value = true
}

const handleEditType = (row: DictTypeItem) => {
  editingTypeId.value = row.id
  typeForm.dictCode = row.dictCode
  typeForm.dictName = row.dictName
  typeForm.sort = row.sort
  typeForm.status = row.status
  typeDialogVisible.value = true
}

const handleSubmitType = async () => {
  await typeFormRef.value?.validate()
  typeSubmitLoading.value = true

  try {
    if (editingTypeId.value) {
      await updateDictType(editingTypeId.value, {
        dictName: typeForm.dictName,
        sort: typeForm.sort,
        status: typeForm.status,
      })
      ElMessage.success('字典类型更新成功')
    } else {
      await createDictType({ ...typeForm })
      ElMessage.success('字典类型新增成功')
    }

    typeDialogVisible.value = false
    await reloadPage()
  } finally {
    typeSubmitLoading.value = false
  }
}

const handleDeleteType = async (row: DictTypeItem) => {
  await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })

  await deleteDictType(row.id)
  ElMessage.success('字典类型删除成功')
  dictTypes.value = dictTypes.value.filter((item) => item.id !== row.id)
  selectedTypeId.value = dictTypes.value[0]?.id || null
  dictData.value = []
  await reloadPage()
}

const handleCreateData = () => {
  if (!selectedTypeId.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }

  resetDataForm()
  dataDialogVisible.value = true
}

const handleEditData = (row: DictDataItem) => {
  editingDataId.value = row.id
  dataForm.dictLabel = row.dictLabel
  dataForm.dictValue = row.dictValue
  dataForm.sort = row.sort
  dataForm.status = row.status
  dataForm.remark = row.remark || ''
  dataDialogVisible.value = true
}

const handleSubmitData = async () => {
  await dataFormRef.value?.validate()
  if (!selectedTypeId.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }

  dataSubmitLoading.value = true

  try {
    if (editingDataId.value) {
      await updateDictData(editingDataId.value, {
        dictLabel: dataForm.dictLabel,
        sort: dataForm.sort,
        status: dataForm.status,
        remark: dataForm.remark,
      })
      ElMessage.success('字典数据更新成功')
    } else {
      await createDictData({
        dictTypeId: selectedTypeId.value,
        ...dataForm,
      })
      ElMessage.success('字典数据新增成功')
    }

    dataDialogVisible.value = false
    await loadData()
  } finally {
    dataSubmitLoading.value = false
  }
}

const handleDeleteData = async (row: DictDataItem) => {
  await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })

  await deleteDictData(row.id)
  ElMessage.success('字典数据删除成功')
  dictData.value = dictData.value.filter((item) => item.id !== row.id)
  await loadData()
}

const handleResetTypeQuery = async () => {
  typeQuery.keyword = ''
  await reloadPage()
}

const handleResetDataQuery = async () => {
  dataQuery.keyword = ''
  await loadData()
}

onMounted(async () => {
  await reloadPage()
})
</script>

<template>
  <div class="dicts-page">
    <div class="page-header">
      <h1>字典管理</h1>
    </div>

    <div class="dict-layout">
      <section class="panel type-panel">
        <div class="panel-header">
          <div>
            <h2>字典类型</h2>
            <p>维护系统内可复用的业务枚举</p>
          </div>
          <el-button v-if="canCreate" type="primary" :icon="Plus" @click="handleCreateType">
            新增类型
          </el-button>
        </div>

        <div class="toolbar">
          <el-input
            v-model="typeQuery.keyword"
            clearable
            placeholder="编码或名称"
            @keyup.enter="reloadPage"
          />
          <el-button type="primary" :icon="Search" @click="reloadPage">搜索</el-button>
          <el-button @click="handleResetTypeQuery">重置</el-button>
        </div>

        <el-table
          v-loading="typeLoading"
          :data="dictTypes"
          border
          highlight-current-row
          class="dict-table"
          @row-click="handleSelectType"
        >
          <el-table-column prop="dictCode" label="编码" min-width="160" />
          <el-table-column prop="dictName" label="名称" min-width="140" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="canShowTypeActions" label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEdit" link type="primary" @click.stop="handleEditType(row)">
                编辑
              </el-button>
              <el-button v-if="canDelete" link type="danger" @click.stop="handleDeleteType(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel data-panel">
        <div class="panel-header">
          <div>
            <h2>字典数据</h2>
            <p>{{ selectedType ? `${selectedType.dictName}（${selectedType.dictCode}）` : '请选择左侧字典类型' }}</p>
          </div>
          <el-button v-if="canCreate" type="primary" :icon="Plus" @click="handleCreateData">
            新增数据
          </el-button>
        </div>

        <div class="toolbar">
          <el-input
            v-model="dataQuery.keyword"
            clearable
            placeholder="标签或字典值"
            @keyup.enter="loadData"
          />
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
          <el-button @click="handleResetDataQuery">重置</el-button>
        </div>

        <el-table v-loading="dataLoading" :data="dictData" border stripe class="dict-table">
          <el-table-column prop="dictLabel" label="标签" min-width="140" />
          <el-table-column prop="dictValue" label="字典值" min-width="140" />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column v-if="canShowDataActions" label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canEdit" link type="primary" @click="handleEditData(row)">
                编辑
              </el-button>
              <el-button v-if="canDelete" link type="danger" @click="handleDeleteData(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="520px" @closed="resetTypeForm">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input
            v-model="typeForm.dictCode"
            :disabled="Boolean(editingTypeId)"
            placeholder="例如 sys_status"
          />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="typeForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="handleSubmitType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="560px" @closed="resetDataForm">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input
            v-model="dataForm.dictValue"
            :disabled="Boolean(editingDataId)"
            placeholder="例如 active"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dataForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitLoading" @click="handleSubmitData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dicts-page {
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

.dict-layout {
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(560px, 1.4fr);
  gap: 20px;
  align-items: start;
}

.panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  box-shadow: 0 8px 22px rgba(17, 24, 39, 0.06);
  padding: 20px 24px 24px;
  min-width: 0;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.panel-header h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.panel-header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.toolbar .el-input {
  max-width: 260px;
}

.dict-table {
  width: 100%;
}

@media (max-width: 1200px) {
  .dict-layout {
    grid-template-columns: 1fr;
  }
}
</style>
