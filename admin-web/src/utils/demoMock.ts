import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'

const demoHost = 'editsrepresas-cell.github.io'

export const isDemoMode = () => {
  return window.location.hostname === demoHost && window.location.pathname.startsWith('/admin-system')
}

const permissionCodes = [
  'user:list',
  'user:create',
  'user:update',
  'user:delete',
  'user:reset-password',
  'role:list',
  'role:create',
  'role:update',
  'role:delete',
  'role:permission',
  'permission:list',
  'permission:create',
  'permission:update',
  'permission:delete',
  'dept:list',
  'dept:create',
  'dept:update',
  'dept:delete',
  'post:list',
  'post:create',
  'post:update',
  'post:delete',
  'dict:list',
  'dict:create',
  'dict:update',
  'dict:delete',
  'notice:list',
  'notice:create',
  'notice:update',
  'notice:delete',
  'notice:publish',
  'operation-log:list',
]

const demoUser = {
  token: 'demo-token',
  userId: 1,
  username: 'superadmin',
  nickname: 'Demo Admin',
  roleId: 1,
  roleCode: 'SUPER_ADMIN',
  roleName: 'Super Administrator',
  permissionCodes,
}

const roles = [
  { id: 1, roleCode: 'SUPER_ADMIN', roleName: 'Super Administrator', sort: 1, status: 1 },
  { id: 2, roleCode: 'ADMIN', roleName: 'Administrator', sort: 2, status: 1 },
  { id: 3, roleCode: 'OPERATOR', roleName: 'Operator', sort: 3, status: 1 },
]

const depts = [
  {
    id: 1,
    parentId: 0,
    deptName: 'Head Office',
    leader: 'Demo Admin',
    phone: '13800000000',
    email: 'admin@example.com',
    sort: 1,
    status: 1,
    createTime: '2026-06-30 09:00:00',
    children: [
      {
        id: 2,
        parentId: 1,
        deptName: 'Technology Department',
        leader: 'Alice',
        phone: '13800000001',
        email: 'tech@example.com',
        sort: 1,
        status: 1,
        createTime: '2026-06-30 09:10:00',
      },
      {
        id: 3,
        parentId: 1,
        deptName: 'Operations Department',
        leader: 'Bob',
        phone: '13800000002',
        email: 'ops@example.com',
        sort: 2,
        status: 1,
        createTime: '2026-06-30 09:20:00',
      },
    ],
  },
]

const posts = [
  { id: 1, postCode: 'CEO', postName: 'General Manager', sort: 1, status: 1, createTime: '2026-06-30 09:00:00' },
  { id: 2, postCode: 'DEV', postName: 'Developer', sort: 2, status: 1, createTime: '2026-06-30 09:10:00' },
  { id: 3, postCode: 'OPS', postName: 'Operator', sort: 3, status: 1, createTime: '2026-06-30 09:20:00' },
]

const users = [
  {
    id: 1,
    username: 'superadmin',
    nickname: 'Demo Admin',
    deptId: 1,
    deptName: 'Head Office',
    postId: 1,
    postName: 'General Manager',
    roleId: 1,
    roleName: 'Super Administrator',
    phone: '13800000000',
    email: 'admin@example.com',
    status: 1,
    createTime: '2026-06-30 09:00:00',
  },
  {
    id: 2,
    username: 'admin',
    nickname: 'System Admin',
    deptId: 2,
    deptName: 'Technology Department',
    postId: 2,
    postName: 'Developer',
    roleId: 2,
    roleName: 'Administrator',
    phone: '13800000001',
    email: 'admin2@example.com',
    status: 1,
    createTime: '2026-06-30 09:30:00',
  },
  {
    id: 3,
    username: 'operator',
    nickname: 'Operations User',
    deptId: 3,
    deptName: 'Operations Department',
    postId: 3,
    postName: 'Operator',
    roleId: 3,
    roleName: 'Operator',
    phone: '13800000002',
    email: 'operator@example.com',
    status: 0,
    createTime: '2026-06-30 10:00:00',
  },
]

const notices = [
  {
    id: 1,
    title: 'System launch notice',
    noticeType: 'system',
    noticeTypeName: 'System Notice',
    content: 'The admin system demo is available for online preview.',
    status: 1,
    statusName: 'Published',
    createBy: 1,
    publishTime: '2026-06-30 10:30:00',
    createTime: '2026-06-30 10:20:00',
  },
  {
    id: 2,
    title: 'Permission module updated',
    noticeType: 'business',
    noticeTypeName: 'Business Notice',
    content: 'Menu, button and API permission controls are ready.',
    status: 1,
    statusName: 'Published',
    createBy: 1,
    publishTime: '2026-06-30 11:00:00',
    createTime: '2026-06-30 10:50:00',
  },
]

const dictTypes = [
  { id: 1, dictCode: 'sys_user_status', dictName: 'User Status', sort: 1, status: 1 },
  { id: 2, dictCode: 'sys_notice_type', dictName: 'Notice Type', sort: 2, status: 1 },
]

const dictData = [
  { id: 1, dictTypeId: 1, dictLabel: 'Enabled', dictValue: '1', sort: 1, status: 1, remark: 'Active account' },
  { id: 2, dictTypeId: 1, dictLabel: 'Disabled', dictValue: '0', sort: 2, status: 1, remark: 'Inactive account' },
  { id: 3, dictTypeId: 2, dictLabel: 'System Notice', dictValue: 'system', sort: 1, status: 1, remark: 'Platform notice' },
  { id: 4, dictTypeId: 2, dictLabel: 'Business Notice', dictValue: 'business', sort: 2, status: 1, remark: 'Business notice' },
]

const operationLogs = [
  {
    id: 1,
    operatorId: 1,
    operatorUsername: 'superadmin',
    module: 'Auth',
    action: 'Login',
    targetId: 1,
    detail: 'Demo user logged in',
    result: 1,
    createTime: '2026-06-30 10:00:00',
  },
  {
    id: 2,
    operatorId: 1,
    operatorUsername: 'superadmin',
    module: 'User',
    action: 'Update',
    targetId: 2,
    detail: 'Updated user profile',
    result: 1,
    createTime: '2026-06-30 10:12:00',
  },
  {
    id: 3,
    operatorId: 1,
    operatorUsername: 'superadmin',
    module: 'Role',
    action: 'Permission',
    targetId: 2,
    detail: 'Adjusted role permissions',
    result: 1,
    createTime: '2026-06-30 10:25:00',
  },
]

const permissions = [
  {
    id: 1,
    parentId: 0,
    permissionCode: 'system',
    permissionName: 'System Management',
    permissionType: 'MENU',
    sort: 1,
    status: 1,
    children: [
      { id: 11, parentId: 1, permissionCode: 'user:list', permissionName: 'User List', permissionType: 'MENU', sort: 1, status: 1 },
      { id: 12, parentId: 1, permissionCode: 'role:list', permissionName: 'Role List', permissionType: 'MENU', sort: 2, status: 1 },
      { id: 13, parentId: 1, permissionCode: 'permission:list', permissionName: 'Permission List', permissionType: 'MENU', sort: 3, status: 1 },
    ],
  },
  {
    id: 2,
    parentId: 0,
    permissionCode: 'operation-log:list',
    permissionName: 'Operation Logs',
    permissionType: 'MENU',
    sort: 2,
    status: 1,
  },
]

const ok = <T>(data: T) => ({ code: 200, message: 'success', data })

const paginate = <T>(records: T[], params: Record<string, unknown>) => {
  const pageNum = Number(params.pageNum || 1)
  const pageSize = Number(params.pageSize || 10)
  const start = (pageNum - 1) * pageSize

  return {
    total: records.length,
    pageNum,
    pageSize,
    records: records.slice(start, start + pageSize),
  }
}

const normalizeUrl = (rawUrl = '') => {
  return rawUrl.replace(/^https?:\/\/[^/]+/, '').replace(/^\/api/, '')
}

const getParams = (config: InternalAxiosRequestConfig) => {
  return (config.params || {}) as Record<string, unknown>
}

const filterByKeyword = <T extends Record<string, unknown>>(records: T[], keyword?: unknown) => {
  if (!keyword) {
    return records
  }

  const text = String(keyword).toLowerCase()
  return records.filter((record) => JSON.stringify(record).toLowerCase().includes(text))
}

const createBlob = (content: string) => {
  return new Blob([content], { type: 'text/csv;charset=utf-8' })
}

const getMockData = (config: InternalAxiosRequestConfig) => {
  const url = normalizeUrl(config.url)
  const method = (config.method || 'get').toLowerCase()
  const params = getParams(config)

  if (config.responseType === 'blob') {
    return createBlob('demo export\n')
  }

  if (method === 'post' && url === '/auth/login') {
    return ok(demoUser)
  }

  if (method === 'get' && url === '/auth/me') {
    return ok(demoUser)
  }

  if (method === 'put' && url === '/auth/password') {
    return ok(1)
  }

  if (method === 'get' && url === '/health') {
    return ok('demo front-end preview is running')
  }

  if (method === 'get' && url === '/dashboard/stats') {
    return ok({
      totalUsers: users.length,
      enabledUsers: users.filter((user) => user.status === 1).length,
      disabledUsers: users.filter((user) => user.status === 0).length,
      totalRoles: roles.length,
      totalDepts: 3,
      totalPosts: posts.length,
      totalNotices: notices.length,
      todayOperationLogs: operationLogs.length,
      lastOperationTime: '2026-06-30T10:25:00',
    })
  }

  if (method === 'get' && url === '/users') {
    let records = filterByKeyword(users, params.keyword)
    if (params.status !== undefined && params.status !== '') {
      records = records.filter((user) => user.status === Number(params.status))
    }
    return ok(paginate(records, params))
  }

  if (method === 'get' && url === '/roles/options') {
    return ok(roles.map(({ id, roleCode, roleName }) => ({ id, roleCode, roleName })))
  }

  if (method === 'get' && url === '/roles') {
    return ok(roles)
  }

  if (method === 'get' && url === '/permissions/tree') {
    return ok(permissions)
  }

  if (method === 'get' && /^\/roles\/\d+\/permissions$/.test(url || '')) {
    return ok([11, 12, 13])
  }

  if (method === 'get' && url === '/depts/tree') {
    return ok(depts)
  }

  if (method === 'get' && (url === '/posts' || url === '/posts/options')) {
    return ok(posts)
  }

  if (method === 'get' && (url === '/dict-types' || url === '/dict-types/options')) {
    return ok(filterByKeyword(dictTypes, params.keyword))
  }

  if (method === 'get' && url === '/dict-data') {
    let records = dictData
    if (params.dictTypeId) {
      records = records.filter((item) => item.dictTypeId === Number(params.dictTypeId))
    }
    return ok(filterByKeyword(records, params.keyword))
  }

  if (method === 'get' && url === '/dict-data/options') {
    const type = dictTypes.find((item) => item.dictCode === params.dictCode)
    return ok(type ? dictData.filter((item) => item.dictTypeId === type.id) : dictData)
  }

  if (method === 'get' && url === '/notices/unread') {
    return ok(notices.slice(0, 2))
  }

  if (method === 'get' && url === '/notices') {
    let records = filterByKeyword(notices, params.keyword)
    if (params.noticeType) {
      records = records.filter((notice) => notice.noticeType === params.noticeType)
    }
    if (params.status !== undefined && params.status !== '') {
      records = records.filter((notice) => notice.status === Number(params.status))
    }
    return ok(records)
  }

  const noticeDetailMatch = /^\/notices\/(\d+)$/.exec(url || '')
  if (method === 'get' && noticeDetailMatch) {
    return ok(notices.find((notice) => notice.id === Number(noticeDetailMatch[1])) || notices[0])
  }

  if (method === 'get' && url === '/operation-logs/modules') {
    return ok(['Auth', 'User', 'Role'])
  }

  if (method === 'get' && url === '/operation-logs') {
    let records = filterByKeyword(operationLogs, params.keyword)
    if (params.module) {
      records = records.filter((log) => log.module === params.module)
    }
    if (params.result !== undefined && params.result !== '') {
      records = records.filter((log) => log.result === Number(params.result))
    }
    return ok(paginate(records, params))
  }

  if (['post', 'put', 'delete'].includes(method)) {
    return ok(1)
  }

  return ok(null)
}

export const demoAdapter = async (
  config: InternalAxiosRequestConfig,
): Promise<AxiosResponse> => {
  return {
    data: getMockData(config),
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  }
}
