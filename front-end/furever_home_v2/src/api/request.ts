/**
 * HTTP request helper based on fetch.
 * Emits window events to control global loading indicator.
 */

// Base URL: dev uses /api (proxied by Vite); prod can override via VITE_API_BASE_URL
const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

// Request config
interface RequestConfig extends RequestInit {
  params?: Record<string, any>
  timeout?: number
}

// Standard API response
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

class HttpClient {
  private baseURL: string
  private timeout: number
  private static globalPendingCount = 0

  constructor(baseURL: string, timeout: number = 30000) {
    this.baseURL = baseURL
    this.timeout = timeout
  }

  private getAuthTokens(): {
    bearerToken: string | null
    saTokenName: string | null
    saTokenValue: string | null
  } {
    // 优先读取已经带 Bearer 前缀的 Authorization，避免前缀丢失
    const storedAuth = localStorage.getItem('Authorization') || sessionStorage.getItem('Authorization')
    const bearerToken =
      storedAuth ||
      localStorage.getItem('token') ||
      sessionStorage.getItem('token')

    const saTokenName = localStorage.getItem('saTokenName')
    const saTokenValue = localStorage.getItem('saTokenValue')
    return { bearerToken, saTokenName, saTokenValue }
  }
  private buildURL(url: string, params?: Record<string, any>): string {
    let fullURL: string
    if (url.startsWith('http')) {
      fullURL = url
    } else if (this.baseURL) {
      const base = this.baseURL.endsWith('/') ? this.baseURL.slice(0, -1) : this.baseURL
      const path = url.startsWith('/') ? url : `/${url}`
      fullURL = `${base}${path}`
    } else {
      fullURL = url.startsWith('/') ? url : `/${url}`
    }

    if (params) {
      const searchParams = new URLSearchParams()
      Object.keys(params).forEach(key => {
        if (params[key] !== undefined && params[key] !== null) {
          searchParams.append(key, String(params[key]))
        }
      })
      const queryString = searchParams.toString()
      return queryString ? `${fullURL}?${queryString}` : fullURL
    }

    return fullURL
  }

  private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
    const contentType = response.headers.get('content-type')
    const isJson = contentType?.includes('application/json')

    let data: any
    if (isJson) {
      try {
        data = await response.json()
      } catch (e) {
        const text = await response.text()
        console.error('JSON parse failed, raw response:', text)
        throw new Error(`响应解析失败: ${text}`)
      }
    } else {
      data = await response.text()
    }

    if (!response.ok) {
      console.error('HTTP error response:', {
        status: response.status,
        statusText: response.statusText,
        url: response.url,
        data
      })

      if (isJson && data && typeof data === 'object') {
        const errorMsg = data.message || data.msg || `HTTP error! status: ${response.status}`
        throw new Error(errorMsg)
      }
      throw new Error((data as any)?.message || data || `HTTP error! status: ${response.status}`)
    }

    return data
  }

  private async request<T>(
    url: string,
    config: RequestConfig = {}
  ): Promise<ApiResponse<T>> {
    const { params, timeout = this.timeout, ...fetchConfig } = config
    const fullURL = this.buildURL(url, params)

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(fetchConfig.headers as Record<string, string>),
    }

    const { bearerToken, saTokenName, saTokenValue } = this.getAuthTokens()

    // 统一处理 Token 逻辑：始终优先使用 Authorization: Bearer <token>
    // 只有当 saTokenName 明确存在且不为 Authorization/satoken 时，才额外添加自定义头
    
    let finalToken = ''

    if (saTokenValue) {
      finalToken = saTokenValue
    } else if (bearerToken) {
      finalToken = bearerToken
    }

    if (finalToken) {
      // 确保 Token 值不包含 Bearer 前缀（先清理）
      const cleanToken = finalToken.startsWith('Bearer ') 
        ? finalToken.substring(7) 
        : finalToken
      
      // 构造标准的 Bearer Token
      const tokenWithBearer = `Bearer ${cleanToken}`

      // 1. 始终设置 Authorization 头（最标准做法）
      headers['Authorization'] = tokenWithBearer

      // 2. 如果有自定义的 Token Name，且不是 Authorization，则也设置（兼容性）
      // 注意：不要设置 satoken 头，除非 saTokenName 明确是 satoken，否则容易导致后端重复读取报错
      if (saTokenName && saTokenName.toLowerCase() !== 'authorization') {
         headers[saTokenName] = tokenWithBearer
      }
    }

    // 调试日志：打印请求头，方便排查 500 错误
    if (url.includes('/admin')) {
       console.log(`[HttpClient] Request: ${fullURL}`, headers)
    }

    HttpClient.globalPendingCount++
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('http-request-start', {
        detail: { pending: HttpClient.globalPendingCount }
      }))
    }

    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), timeout)

    try {
      const response = await fetch(fullURL, {
        ...fetchConfig,
        headers,
        signal: controller.signal,
      })

      clearTimeout(timeoutId)
      return await this.handleResponse<T>(response)
    } catch (error: any) {
      clearTimeout(timeoutId)
      if (error.name === 'AbortError') {
        throw new Error('请求超时')
      }
      throw error
    } finally {
      HttpClient.globalPendingCount = Math.max(0, HttpClient.globalPendingCount - 1)
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('http-request-end', {
          detail: { pending: HttpClient.globalPendingCount }
        }))
      }
    }
  }

  get<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...config,
      method: 'GET',
    })
  }

  post<T = any>(
    url: string,
    data?: any,
    config?: RequestConfig
  ): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...config,
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  put<T = any>(
    url: string,
    data?: any,
    config?: RequestConfig
  ): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...config,
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  delete<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...config,
      method: 'DELETE',
    })
  }

  patch<T = any>(
    url: string,
    data?: any,
    config?: RequestConfig
  ): Promise<ApiResponse<T>> {
    return this.request<T>(url, {
      ...config,
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async upload<T = any>(
    url: string,
    formData: FormData,
    config?: RequestConfig
  ): Promise<ApiResponse<T>> {
    const fullURL = this.buildURL(url)
    const headers: Record<string, string> = {}

    const { bearerToken, saTokenName, saTokenValue } = this.getAuthTokens()
    if (bearerToken) {
      const cleanBearerToken = bearerToken.startsWith('Bearer ')
        ? bearerToken.substring(7)
        : bearerToken
      const tokenWithBearer = `Bearer ${cleanBearerToken}`
      headers['Authorization'] = tokenWithBearer
      headers['satoken'] = tokenWithBearer
    }
    if (saTokenName && saTokenValue) {
      const cleanTokenValue = saTokenValue.startsWith('Bearer ')
        ? saTokenValue.substring(7)
        : saTokenValue
      const tokenWithBearer = `Bearer ${cleanTokenValue}`
      headers[saTokenName] = tokenWithBearer
      headers['Authorization'] = tokenWithBearer
      headers['satoken'] = tokenWithBearer
    }

    const controller = new AbortController()
    const timeout = config?.timeout || this.timeout
    const timeoutId = setTimeout(() => controller.abort(), timeout)

    try {
      const response = await fetch(fullURL, {
        ...config,
        method: 'POST',
        headers,
        body: formData,
        signal: controller.signal,
      })

      clearTimeout(timeoutId)
      return await this.handleResponse<T>(response)
    } catch (error: any) {
      clearTimeout(timeoutId)
      if (error.name === 'AbortError') {
        throw new Error('上传超时')
      }
      throw error
    }
  }
}

const httpClient = new HttpClient(BASE_URL)

const ADMIN_BASE_URL = import.meta.env.VITE_ADMIN_BASE_URL || (import.meta.env.DEV ? '' : 'http://localhost:8080')
const adminHttpClient = new HttpClient(ADMIN_BASE_URL)

export default httpClient
export { HttpClient, adminHttpClient, type ApiResponse, type RequestConfig }

