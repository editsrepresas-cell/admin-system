import type { LoginVO } from '../api/auth'

export const getLoginUser = (): Partial<LoginVO> => {
  const raw = localStorage.getItem('admin_user')

  if (!raw) {
    return {}
  }

  try {
    return JSON.parse(raw) as Partial<LoginVO>
  } catch {
    return {}
  }
}

export const hasLoginUser = () => {
  return Boolean(localStorage.getItem('admin_user'))
}

export const hasPermission = (permissionCode: string) => {
  const user = getLoginUser()

  if (user.roleCode === 'SUPER_ADMIN') {
    return true
  }

  return Array.isArray(user.permissionCodes) && user.permissionCodes.includes(permissionCode)
}

export const hasAnyPermission = (permissionCodes: string[] = [], user = getLoginUser()) => {
  if (permissionCodes.length === 0) {
    return false
  }

  if (user.roleCode === 'SUPER_ADMIN') {
    return true
  }

  const ownedPermissionCodes = user.permissionCodes || []
  return permissionCodes.some((permissionCode) => ownedPermissionCodes.includes(permissionCode))
}

export const hasRole = (roleCodes: string[] = [], user = getLoginUser()) => {
  if (roleCodes.length === 0) {
    return false
  }

  if (user.roleCode === 'SUPER_ADMIN') {
    return true
  }

  return Boolean(user.roleCode && roleCodes.includes(user.roleCode))
}

export interface AccessRule {
  permissions?: string[]
  roles?: string[]
}

export const canAccess = (rule: AccessRule = {}, user = getLoginUser()) => {
  const permissions = rule.permissions || []
  const roles = rule.roles || []

  if (permissions.length === 0 && roles.length === 0) {
    return true
  }

  return hasRole(roles, user) || hasAnyPermission(permissions, user)
}
