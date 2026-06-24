/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bg: '#0a0e1a',
        surface: '#111827',
        'surface-elevated': '#1a2235',
        border: '#1e2d45',
        accent: {
          primary: '#3b82f6',
          hover: '#2563eb',
          glow: 'rgba(59,130,246,0.15)',
        },
        'text-primary': '#f0f4ff',
        'text-secondary': '#6b7a99',
        status: {
          scheduled: '#3b82f6',
          locked: '#f59e0b',
          live: '#10b981',
          finished: '#4b5563',
          cancelled: '#ef4444',
        },
        rank: {
          rookie: '#4b5563',
          silver: '#94a3b8',
          gold: '#f59e0b',
          diamond: '#3b82f6',
          master: '#8b5cf6',
          challenger: '#f97316',
        },
      },
      fontFamily: {
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
}
