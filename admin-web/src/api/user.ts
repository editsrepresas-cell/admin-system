import request from '../utils/request'

export interface UserItem {
  id: number
  username: string
  nickname: string
  deptId?: number
  deptName?: string
  postId?: number
  postName?: string
  roleId?: number
  roleName?: string
  phone: string
  email: string
  status: number
  createTime: string
}

export interface RoleItem {
  id: number
  roleCode: string
  roleName: string
}

export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}

export interface UserQuery {
  pageNum: number
  pageSize: number
  keyword?: string
}

export interface UserCreateDTO {
  username: string
  password: string
  nickname: string
  deptId: number | null
  postId: number | null
  roleId: number | null
  phone: string
  email: string
  status: number
}

export interface UserUpdateDTO {
  nickname: string
  deptId: number | null
  postId: number | null
  roleId: number | null
  phone: string
  email: string
  status: number
}

export const getUserList = (params: UserQuery) => {
  return request.get<PageResult<UserItem>>('/users', { params })
}

export const exportUserList = (params: Pick<UserQuery, 'keyword'>) => {
  return request.get<Blob>('/users/export', {
    params,
    responseType: 'blob',
  })
}

export const getRoleList = () => {
  return request.get<RoleItem[]>('/roles/options')
}

export const createUser = (data: UserCreateDTO) => {
  return request.post<number>('/users', data)
}

export const updateUser = (id: number, data: UserUpdateDTO) => {
  return request.put<number>(`/users/${id}`, data)
}

export const deleteUser = (id: number) => {
  return request.delete<number>(`/users/${id}`)
}

export const resetUserPassword = (id: number) => {
  return request.put<number>(`/users/${id}/reset-password`)
}
