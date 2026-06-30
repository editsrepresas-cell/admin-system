import request from '../utils/request'

export interface NoticeItem {
  id: number
  title: string
  noticeType: string
  noticeTypeName: string
  content: string
  status: number
  statusName: string
  createBy?: number
  publishTime?: string
  createTime?: string
}

export interface NoticeCreateDTO {
  title: string
  noticeType: string
  content: string
  status: number
}

export type NoticeUpdateDTO = NoticeCreateDTO

export const getNoticeList = (params?: { keyword?: string; noticeType?: string; status?: number }) => {
  return request.get<NoticeItem[]>('/notices', { params })
}

export const getNoticeDetail = (id: number) => {
  return request.get<NoticeItem>(`/notices/${id}`)
}

export const getUnreadNotices = () => {
  return request.get<NoticeItem[]>('/notices/unread')
}

export const markNoticeRead = (id: number) => {
  return request.put<number>(`/notices/${id}/read`)
}

export const markAllNoticesRead = () => {
  return request.put<number>('/notices/read-all')
}

export const createNotice = (data: NoticeCreateDTO) => {
  return request.post<number>('/notices', data)
}

export const updateNotice = (id: number, data: NoticeUpdateDTO) => {
  return request.put<number>(`/notices/${id}`, data)
}

export const publishNotice = (id: number) => {
  return request.put<number>(`/notices/${id}/publish`)
}

export const offlineNotice = (id: number) => {
  return request.put<number>(`/notices/${id}/offline`)
}

export const deleteNotice = (id: number) => {
  return request.delete<number>(`/notices/${id}`)
}
