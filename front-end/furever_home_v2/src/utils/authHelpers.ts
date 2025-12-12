import type { Router } from 'vue-router'
import { getCurrentUser, type CurrentUserInfo } from '@/api/userApi'

interface LoginResponseLike {
  code?: number
  message?: string
  data?: any
  [key: string]: any
}

export interface LoginProcessResult {
  targetRouteName: string
  successUserName: string
  isAdminLogin: boolean
}

/**
 * 统一处理登录成功后的行为：保存 token、角色、当前用户信息，并返回跳转目标
 */
export async function processLoginSuccess(
  res: LoginResponseLike,
  account: string
): Promise<LoginProcessResult> {
  const data = res?.data || {}
  const tokenInfo = data.tokenInfo || data

  const tokenValue: string | undefined = tokenInfo.tokenValue
  const tokenName: string | undefined = tokenInfo.tokenName
  const isLogin: boolean = tokenInfo.isLogin ?? true

  if (!((res?.code === 0 || res?.code === 200) && tokenValue && isLogin)) {
    throw new Error(res?.message || '登录失败，请检查账号或验证码')
  }

  localStorage.setItem('token', tokenValue)
  if (tokenName && tokenValue) {
    localStorage.setItem('saTokenName', tokenName)
    localStorage.setItem('saTokenValue', tokenValue)
  }

  let targetRouteName: string = 'Home'
  let successUserName = account

  const rawRoles: any = data.roles || tokenInfo.roles || (res as any).roles
  let roles: string[] = []

  if (Array.isArray(rawRoles)) {
    roles = rawRoles.map(r => String(r).toUpperCase().trim())
  } else if (typeof rawRoles === 'string') {
    roles = rawRoles.split(',').map(r => r.toUpperCase().trim()).filter(Boolean)
  } else if (rawRoles !== null && rawRoles !== undefined) {
    const roleStr = String(rawRoles).toUpperCase().trim()
    if (roleStr) roles = [roleStr]
  }

  if (roles.length > 0) {
    localStorage.setItem('roles', JSON.stringify(roles))
  }

  const hasAdminRole = roles.some(r => {
    const roleStr = String(r).toUpperCase().trim()
    return roleStr === 'ADMIN' || roleStr.includes('ADMIN') || roleStr === 'ROLE_ADMIN' || roleStr.startsWith('ADMIN')
  })

  const hasUserRole = roles.some(r => {
    const roleStr = String(r).toUpperCase().trim()
    return roleStr === 'USER' || roleStr.includes('USER') || roleStr === 'ROLE_USER' || roleStr.startsWith('USER')
  })

  let isAdminLogin = false

  if (hasAdminRole) {
    isAdminLogin = true
    targetRouteName = 'AdminDashboard'
    successUserName = account
  } else {
    if (hasUserRole) {
      targetRouteName = 'Home'
    }
    try {
      const meRes = await getCurrentUser()
      if ((meRes.code === 0 || meRes.code === 200) && meRes.data) {
        const me: CurrentUserInfo = meRes.data
        localStorage.setItem('currentUser', JSON.stringify(me))
        if (me.userName) localStorage.setItem('userName', me.userName)
        if (me.avatarUrl) localStorage.setItem('avatarUrl', me.avatarUrl)
        successUserName = me.userName || account
        window.dispatchEvent(new CustomEvent('current-user-updated'))
      }
    } catch (error) {
      console.error('登录后获取当前用户信息失败', error)
    }
  }

  return {
    targetRouteName,
    successUserName,
    isAdminLogin
  }
}

