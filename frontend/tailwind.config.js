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
          50: '#fce4ec',
          100: '#f8bbd9', 
          200: '#f48fb1',
          300: '#f06292',
          400: '#ec407a',
          500: '#e91e63', // 요기요 스타일 빨간색/핑크
          600: '#d81b60',
          700: '#c2185b',
          800: '#ad1457',
          900: '#880e4f',
        }
      }
    },
  },
  plugins: [],
}