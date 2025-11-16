/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: { 
        primary: '#FB923C', 
        'background-light': '#F8FAFC', 
        'background-dark': '#18181B' 
      },
      fontFamily: { 
        display: ["'Noto Sans SC'", 'sans-serif'] 
      },
      borderRadius: { 
        DEFAULT: '0.5rem' 
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
  ],
} satisfies import('tailwindcss').Config;