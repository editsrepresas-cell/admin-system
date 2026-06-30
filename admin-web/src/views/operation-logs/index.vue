<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Search, View } from '@element-plus/icons-vue'
import {
  exportOperationLogList,
  getOperationLogList,
  getOperationLogModules,
  type OperationLogItem,
} from '../../api/operationLog'

const loading = ref(false)
const exportLoading = ref(false)
const detailVisible = ref(false)
const logList = ref<OperationLogItem[]>([])
const moduleOptions = ref<string[]>([])
const currentLog = ref<OperationLogItem | null>(null)
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  module: '',
  result: undefined as number | undefined,
  timeRange: [] as string[],
  keyword: '',
})

const buildFilterParams = () => ({
  module: query.module || undefined,
  result: query.result,
  startTime: query.timeRange[0],
  endTime: query.timeRange[1],
  keyword: query.keyword || undefined,
})

const loadModuleOptions = async () => {
  const res = await getOperationLogModules()
  moduleOptions.value = res.data
}

const loadLogList = async () => {
  loading.value = true
  try {
    const res = await getOperationLogList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      ...buildFilterParams(),
    })

    const records = res.data.records

    if (query.result !== undefined && records.some((item) => item.result !== query.result)) {
      logList.value = records.filter((item) => item.result === query.result)
      total.value = logList.value.length
      return
    }

    logList.value = records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  loadLogList()
}

const handleReset = () => {
  query.pageNum = 1
  query.module = ''
  query.result = undefined
  query.timeRange = []
  query.keyword = ''
  loadLogList()
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const blob = await exportOperationLogList(buildFilterParams())
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    const timestamp = new Date().toISOString().slice(0, 19).replace(/[-:T]/g, '')

    link.href = url
    link.download = `操作日志-${timestamp}.csv`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } finally {
    exportLoading.value = false
  }
}

const handleView = (row: OperationLogItem) => {
  currentLog.value = row
  detailVisible.value = true
}

const handlePageChange = (pageNum: number) => {
  query.pageNum = pageNum
  loadLogList()
}

const handleSizeChange = (pageSize: number) => {
  query.pageNum = 1
  query.pageSize = pageSize
  loadLogList()
}

onMounted(() => {
  loadModuleOptions()
  loadLogList()
})
</script>

<template>
  <div class="operation-logs-page">
    <div class="page-header">
      <h1>操作日志</h1>
    </div>

    <el-card class="filter-card">
      <el-form :model="query" label-position="left" class="filter-form">
        <div class="filter-grid">
          <el-form-item label="模块" class="filter-item">
            <el-select v-model="query.module" clearable placeholder="全部模块">
              <el-option
                v-for="item in moduleOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="结果" class="filter-item">
            <el-select v-model="query.result" clearable placeholder="全部结果">
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
          </el-form-item>

          <el-form-item label="时间" class="filter-item time-item">
            <el-date-picker
              v-model="query.timeRange"
              type="datetimerange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="关键词" class="filter-item keyword-item">
            <el-input
              v-model.trim="query.keyword"
              clearable
              placeholder="操作人、动作或详情"
              @keyup.enter="handleSearch"
            />
          </el-form-item>

          <div class="filter-actions">
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button :icon="Download" :loading="exportLoading" @click="handleExport">
              导出
            </el-button>
          </div>
        </div>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table v-loading="loading" :data="logList" border>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="operatorUsername" label="操作人" min-width="130" />
        <el-table-column prop="module" label="模块" min-width="120" />
        <el-table-column prop="action" label="操作" min-width="120" />
        <el-table-column prop="targetId" label="目标ID" width="110" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'">
              {{ row.result === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="操作时间" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="日志详情" width="720px">
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorUsername }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ currentLog.operatorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ currentLog.action }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ currentLog.targetId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="currentLog.result === 1 ? 'success' : 'danger'">
            {{ currentLog.result === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="详情" :span="2">
          <div class="detail-text">{{ currentLog.detail || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<style scoped>
.operation-logs-page {
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
}

.filter-card {
  margin-bottom: 20px;
}

.filter-card :deep(.el-card__body) {
  padding: 22px 24px;
}

.filter-grid {
  display: grid;
  grid-template-columns: minmax(230px, 1fr) minmax(190px, 0.8fr) minmax(460px, 1.7fr);
  gap: 16px 28px;
  align-items: end;
}

.filter-item {
  margin-bottom: 0;
}

.filter-item :deep(.el-form-item__label) {
  width: 64px;
  justify-content: flex-start;
  color: #4b5563;
  white-space: nowrap;
  word-break: keep-all;
}

.filter-item :deep(.el-form-item__content) {
  min-width: 0;
}

.filter-item :deep(.el-select),
.filter-item :deep(.el-input),
.time-item :deep(.el-date-editor) {
  width: 100%;
}

.keyword-item {
  grid-column: 1 / span 2;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 32px;
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}

@media (max-width: 1280px) {
  .filter-grid {
    grid-template-columns: minmax(230px, 1fr) minmax(190px, 1fr);
  }

  .time-item,
  .keyword-item {
    grid-column: auto;
  }
}

@media (max-width: 760px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    justify-content: flex-start;
  }
}
</style>
