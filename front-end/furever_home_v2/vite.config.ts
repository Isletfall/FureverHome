import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
    },
  server: {
    proxy: {
      // 代理前台的 /api 接口到后端测试环境
      '/api': {
        target: 'http://colltar.natapp1.cc',
        changeOrigin: true,
        secure: false,
        ws: true, // 开启 WebSocket 代理，支持 /api/ws/*
        timeout: 30000,
        // 不重写路径，保持原样
        rewrite: (path) => path,
        // 前台接口路径：/api/xxx，后端也是 /api/xxx，所以不需要 rewrite
        configure: (proxy, _options) => {
          proxy.on('error', (err, req, res) => {
            console.error('前台接口代理错误:', err.message)
            console.error('请求路径:', req.url)
            // 静默处理错误，避免崩溃
            if (res && typeof res.writeHead === 'function' && !res.headersSent) {
              try {
                res.writeHead(502, {
                  'Content-Type': 'application/json'
                })
                res.end(JSON.stringify({
                  code: 502,
                  message: '网关错误：无法连接到后端服务器',
                  error: err.message
                }))
              } catch (e) {
                // 忽略错误处理失败
              }
            }
          })
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('前台接口代理请求:', req.method, req.url, '->', proxyReq.path)
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            // 记录响应状态，帮助调试
            if (proxyRes.statusCode >= 400) {
              console.log('前台接口代理响应错误:', req.url, '状态码:', proxyRes.statusCode)
            }
          })
        }
      },
      // 代理后台的 /admin 接口到后端测试环境
      '/admin': {
        target: 'http://colltar.natapp1.cc',
        changeOrigin: true,
        secure: false,
        ws: true, // 开启 WebSocket 代理
        timeout: 30000,
        // 关键修复：如果是浏览器页面请求（accept: text/html），则不走代理，让前端路由处理
        bypass: (req, res, options) => {
          const accept = req.headers.accept
          if (accept && accept.includes('text/html')) {
            console.log('跳过代理，服务前端页面:', req.url)
            return req.url
          }
        },
        // 后台接口路径：/admin/xxx，后端也是 /admin/xxx，所以不需要 rewrite
        // 确保代理所有 /admin 开头的请求
        configure: (proxy, _options) => {
          proxy.on('error', (err, req, res) => {
            console.log('后台接口代理错误:', err.message)
            // Vite 5 中需要检查 res 是否有 writeHead 方法
            if (res && typeof res.writeHead === 'function' && !res.headersSent) {
              try {
                res.writeHead(502, {
                  'Content-Type': 'application/json'
                })
                res.end(JSON.stringify({
                  code: 502,
                  message: '网关错误：无法连接到后端服务器',
                  error: err.message
                }))
              } catch (e) {
                console.error('代理错误处理失败:', e)
              }
            }
          })
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            console.log('后台接口代理请求:', req.method, req.url, '->', proxyReq.path, '目标:', proxyReq.getHeader('host'))
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            // 记录响应状态，帮助调试
            if (proxyRes.statusCode >= 400) {
              console.log('后台接口代理响应错误:', req.url, '状态码:', proxyRes.statusCode)
            }
          })
        }
      }
    }
  }
})
