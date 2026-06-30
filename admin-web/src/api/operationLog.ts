import request from '../utils/request'
import type { PageResult } from './user'

export interface OperationLogItem {
  id: number
  operatorId?: number
  operatorUsername: string
  module: string
  action: string
  targetId?: number
  detail?: string
  result: number
  createTime: string
}

export interface OperationLogQuery {
  pageNum: number
  pageSize: number
  module?: string
  result?: number
  startTime?: string
  endTime?: string
  keyword?: string
}

export const getOperationLogList = (params: OperationLogQuery) => {
  return request.get<PageResult<OperationLogItem>>('/operation-logs', { params })
}

export const getOperationLogModules = () => {
  return request.get<string[]>('/operation-logs/modules')
}

export const exportOperationLogList = (params: Omit<OperationLogQuery, 'pageNum' | 'pageSize'>) => {
  return request.get<Blob, Blob>('/operation-logs/export', {
    params,
    responseType: 'blob',
  })
}
