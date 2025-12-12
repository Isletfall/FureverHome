/**
 * @description 动态注入外部资源，确保 Tailwind CSS、字体和图标在 SPA 中可用。
 */

// 定义要注入的资源
const resources = {
  fonts: 'https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&display=swap',
  icons: 'https://fonts.googleapis.com/icon?family=Material+Icons',
  tailwind: 'https://cdn.tailwindcss.com?plugins=forms,typography',
};

// 定义 Tailwind CSS 的配置
const tailwindConfig = {
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: '#FB923C',
        'background-light': '#F8FAFC',
        'background-dark': '#18181B',
      },
      fontFamily: {
        display: ["'Noto Sans SC'", 'sans-serif'],
      },
      borderRadius: {
        DEFAULT: '0.5rem',
      },
    },
  },
};

/**
 * 确保所有外部资源只被注入一次。
 * 这个函数会检查一个标记，如果资源已存在，则不会重复注入。
 */
export function ensureExternalAssets() {
  // 使用 window 上的一个唯一属性作为标记，防止重复执行
  if ((window as any).__assets_injected__) {
    return;
  }

  // 注入 Google Fonts
  const fontLink = document.createElement('link');
  fontLink.rel = 'stylesheet';
  fontLink.href = resources.fonts;
  document.head.appendChild(fontLink);

  // 注入 Material Icons
  const iconLink = document.createElement('link');
  iconLink.rel = 'stylesheet';
  iconLink.href = resources.icons;
  document.head.appendChild(iconLink);

  // 注入 Tailwind CSS 配置
  const configScript = document.createElement('script');
  configScript.innerHTML = `tailwind.config = ${JSON.stringify(tailwindConfig)}`;
  document.head.appendChild(configScript);

  // 注入 Tailwind CSS 脚本
  const tailwindScript = document.createElement('script');
  tailwindScript.src = resources.tailwind;
  document.head.appendChild(tailwindScript);

  // 设置标记，表示资源已注入
  (window as any).__assets_injected__ = true;
}
