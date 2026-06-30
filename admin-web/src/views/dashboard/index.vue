<script setup lang="ts">
import { computed, onMounted, ref, type Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  Bell,
  CircleCheck,
  Collection,
  Document,
  Lock,
  OfficeBuilding,
  Postcard,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import { getDashboardStats, getHealth, type DashboardStats } from '../../api/system'

const router = useRouter()
const loading = ref(false)
const serverStatus = ref('检测中...')
const stats = ref<DashboardStats>({
  totalUsers: 0,
  enabledUsers: 0,
  disabledUsers: 0,
  totalRoles: 0,
  totalDepts: 0,
  totalPosts: 0,
  totalNotices: 0,
  todayOperationLogs: 0,
  lastOperationTime: null,
})

interface StatCard {
  title: string
  value: number
  icon: Component
  tone: string
  path: string
  query?: Record<string, string>
}

const lastOperationText = computed(() => {
  if (!stats.value.lastOperationTime) {
    return '暂无记录'
  }
  return stats.value.lastOperationTime.replace('T', ' ')
})

const statCards = computed<StatCard[]>(() => [
  {
    title: '用户总数',
    value: stats.value.totalUsers,
    icon: User,
    tone: 'blue',
    path: '/users',
  },
  {
    title: '启用用户',
    value: stats.value.enabledUsers,
    icon: CircleCheck,
    tone: 'green',
    path: '/users',
    query: { status: '1' },
  },
  {
    title: '禁用用户',
    value: stats.value.disabledUsers,
    icon: Lock,
    tone: 'orange',
    path: '/users',
    query: { status: '0' },
  },
  {
    title: '角色数量',
    value: stats.value.totalRoles,
    icon: UserFilled,
    tone: 'purple',
    path: '/roles',
  },
  {
    title: '部门数量',
    value: stats.value.totalDepts,
    icon: OfficeBuilding,
    tone: 'cyan',
    path: '/depts',
  },
  {
    title: '岗位数量',
    value: stats.value.totalPosts,
    icon: Postcard,
    tone: 'indigo',
    path: '/posts',
  },
  {
    title: '通知公告',
    value: stats.value.totalNotices,
    icon: Bell,
    tone: 'red',
    path: '/notices',
  },
  {
    title: '今日操作',
    value: stats.value.todayOperationLogs,
    icon: Document,
    tone: 'teal',
    path: '/operation-logs',
  },
])

const handleCardClick = (card: StatCard) => {
  router.push({
    path: card.path,
    query: card.query,
  })
}

const loadDashboard = async () => {
  loading.value = true
  try {
    const [healthRes, statsRes] = await Promise.all([
      getHealth(),
      getDashboardStats(),
    ])

    serverStatus.value = healthRes.data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<template>
  <div class="dashboard-page" v-loading="loading">
    <div class="page-header">
      <div>
        <h1>仪表盘</h1>
        <p>系统运行状态、基础数据规模和操作活跃度概览</p>
      </div>
    </div>

    <el-alert
      class="server-alert"
      title="后端服务连接正常"
      :description="serverStatus"
      type="success"
      show-icon
      :closable="false"
    />

    <div class="stats-grid">
      <button
        v-for="card in statCards"
        :key="card.title"
        class="stat-card"
        type="button"
        @click="handleCardClick(card)"
      >
        <span :class="['stat-icon', card.tone]">
          <el-icon><component :is="card.icon" /></el-icon>
        </span>
        <span class="stat-text">
          <span class="card-title">{{ card.title }}</span>
          <span class="card-value">{{ card.value }}</span>
        </span>
        <span class="stat-arrow">进入</span>
      </button>
    </div>

    <el-card class="activity-card">
      <div class="activity-title">
        <el-icon><Collection /></el-icon>
        <span>最近操作</span>
      </div>
      <div class="activity-content">
        <span class="activity-label">最近操作时间</span>
        <span class="activity-value">{{ lastOperationText }}</span>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-page {
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

.page-header p {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.server-alert {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 20px;
}

.stat-card {
  position: relative;
  min-height: 128px;
  display: grid;
  grid-template-columns: 60px minmax(0, 1fr);
  align-items: center;
  gap: 20px;
  padding: 24px;
  border: 1px solid rgb(226 232 240 / 90%);
  border-radius: 8px;
  appearance: none;
  background:
    linear-gradient(135deg, rgb(255 255 255 / 96%) 0%, rgb(248 250 252 / 92%) 100%);
  box-shadow: 0 10px 28px rgb(15 23 42 / 7%);
  cursor: pointer;
  text-align: left;
  overflow: hidden;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.stat-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgb(59 130 246 / 10%), transparent 46%);
  opacity: 0;
  transition: opacity 0.18s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgb(147 197 253 / 88%);
  background: #ffffff;
  box-shadow: 0 18px 40px rgb(15 23 42 / 14%);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:active {
  transform: translateY(-1px);
}

.stat-card:focus-visible {
  outline: 3px solid rgb(59 130 246 / 28%);
  outline-offset: 2px;
}

.stat-icon {
  position: relative;
  z-index: 1;
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 60px;
  font-size: 28px;
  transition: transform 0.18s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.06);
}

.stat-icon.blue {
  color: #1677ff;
  background: #e8f3ff;
}

.stat-icon.green {
  color: #52c41a;
  background: #edf9e8;
}

.stat-icon.orange {
  color: #fa8c16;
  background: #fff3e6;
}

.stat-icon.purple {
  color: #722ed1;
  background: #f3e8ff;
}

.stat-icon.cyan {
  color: #08979c;
  background: #e6fffb;
}

.stat-icon.indigo {
  color: #2f54eb;
  background: #f0f5ff;
}

.stat-icon.red {
  color: #f5222d;
  background: #fff1f0;
}

.stat-icon.teal {
  color: #13a8a8;
  background: #e6fffb;
}

.stat-text {
  position: relative;
  z-index: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-title {
  color: #64748b;
  font-size: 15px;
}

.card-value {
  color: #020617;
  font-size: 34px;
  line-height: 1;
  font-weight: 800;
}

.stat-arrow {
  position: absolute;
  right: 18px;
  bottom: 16px;
  z-index: 1;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.stat-card:hover .stat-arrow {
  opacity: 1;
  transform: translateX(0);
}

.activity-card {
  margin-top: 20px;
  border-radius: 8px;
}

.activity-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.activity-content {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.activity-label {
  color: #6b7280;
}

.activity-value {
  color: #111827;
  font-weight: 600;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .activity-content {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
