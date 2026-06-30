import request from '../utils/request'

export interface DashboardStats {
  totalUsers: number
  enabledUsers: number
  disabledUsers: number
  totalRoles: number
  totalDepts: number
  totalPosts: number
  totalNotices: number
  todayOperationLogs: number
  lastOperationTime: string | null
}

export const getHealth = () => {
  return request.get('/health')
}

export const getDashboardStats = () => {
  return request.get<DashboardStats>('/dashboard/stats')
}
