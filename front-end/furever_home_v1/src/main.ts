import './assets/styles/main.css';
// 动态注入外部样式与字体（Tailwind CDN、Google Fonts、Material Icons）
import { ensureExternalAssets } from './utils/injectAssets';

import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from './App.vue';
import router from './router';

// 在应用创建前确保外部资源已注入
ensureExternalAssets();

// 创建Vue应用实例
const app = createApp(App);

// 安装插件
app.use(createPinia());
app.use(router);

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err);
  console.error('错误组件:', instance);
  console.error('错误信息:', info);
};

// 全局属性配置
app.config.globalProperties.$appName = 'FUREVER HOME';

// 挂载应用
app.mount('#app');