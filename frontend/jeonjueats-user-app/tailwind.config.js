/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        secondary: {
          50: '#f9f9f9',
          100: '#f3f3f3',
          200: '#e8e8e8',
          300: '#d6d6d6',
          400: '#b8b8b8',
          500: '#9a9a9a',
          600: '#7c7c7c',
          700: '#646464',
          800: '#525252',
          900: '#464646',
        }
      },
      fontFamily: {
        sans: ['Pretendard', 'system-ui', 'sans-serif'],
      },
      spacing: {
        safe: 'env(safe-area-inset-bottom)',
      }
    },
  },
  plugins: [],
} 