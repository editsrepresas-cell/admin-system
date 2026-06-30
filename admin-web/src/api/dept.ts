import request from '../utils/request'

export interface DeptItem {
  id: number
  parentId: number
  deptName: string
  leader?: string
  phone?: string
  email?: string
  sort: number
  status: number
  createTime?: string
  children?: DeptItem[]
}

export interface DeptCreateDTO {
  parentId: number
  deptName: string
  leader?: string
  phone?: string
  email?: string
  sort: number
  status: number
}

export type DeptUpdateDTO = DeptCreateDTO

export const getDeptTree = () => {
  return request.get<DeptItem[]>('/depts/tree')
}

export const createDept = (data: DeptCreateDTO) => {
  return request.post<number>('/depts', data)
}

export const updateDept = (id: number, data: DeptUpdateDTO) => {
  return request.put<number>(`/depts/${id}`, data)
}

export const deleteDept = (id: number) => {
  return request.delete<number>(`/depts/${id}`)
}
