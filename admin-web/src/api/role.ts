import request from '../utils/request'

export interface RoleItem {
  id: number
  roleCode: string
  roleName: string
  sort: number
  status: number
}

export interface RoleCreateDTO {
  roleCode: string
  roleName: string
  sort: number
  status: number
}

export interface RoleUpdateDTO {
  roleName: string
  sort: number
  status: number
}

export interface PermissionItem {
  id: number
  parentId: number
  permissionCode: string
  permissionName: string
  permissionType: 'MENU' | 'BUTTON'
  sort: number
  status: number
  children?: PermissionItem[]
}

export interface PermissionSaveDTO {
  parentId: number
  permissionCode: string
  permissionName: string
  permissionType: 'MENU' | 'BUTTON'
  sort: number
  status: number
}

export const getRoleList = () => {
  return request.get<RoleItem[]>('/roles')
}

export const getRoleOptions = () => {
  return request.get<RoleItem[]>('/roles/options')
}

export const createRole = (data: RoleCreateDTO) => {
  return request.post<number>('/roles', data)
}

export const updateRole = (id: number, data: RoleUpdateDTO) => {
  return request.put<number>(`/roles/${id}`, data)
}

export const deleteRole = (id: number) => {
  return request.delete<number>(`/roles/${id}`)
}

export const getPermissionTree = () => {
  return request.get<PermissionItem[]>('/permissions/tree')
}

export const createPermission = (data: PermissionSaveDTO) => {
  return request.post<number>('/permissions', data)
}

export const updatePermission = (id: number, data: PermissionSaveDTO) => {
  return request.put<number>(`/permissions/${id}`, data)
}

export const deletePermission = (id: number) => {
  return request.delete<number>(`/permissions/${id}`)
}

export const getRolePermissions = (roleId: number) => {
  return request.get<number[]>(`/roles/${roleId}/permissions`)
}

export const updateRolePermissions = (roleId: number, permissionIds: number[]) => {
  return request.put<number>(`/roles/${roleId}/permissions`, { permissionIds })
}
