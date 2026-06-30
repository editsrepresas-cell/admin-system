import request from '../utils/request'

export interface LoginDTO {
  username: string
  password: string
}

export interface LoginVO {
  token: string
  userId: number
  username: string
  nickname: string
  roleId?: number
  roleCode?: string
  roleName?: string
  permissionCodes?: string[]
}

export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
}

export const login = (data: LoginDTO) => {
  return request.post<LoginVO>('/auth/login', data)
}
export const getCurrentUser = () => {
  return request.get<LoginVO>('/auth/me')
}
export const changePassword = (data: ChangePasswordDTO) => {
  return request.put<number>('/auth/password', data)
}
