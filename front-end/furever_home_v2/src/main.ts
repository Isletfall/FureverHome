import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles/main.css'
import { createPinia } from 'pinia'  // ✅ 引入 Pinia

const app = createApp(App)

// 使用 Pinia
app.use(createPinia())

// 使用路由
app.use(router)

// 挂载应用
app.mount('#app')
