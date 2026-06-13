export type Theme = 'light' | 'dark'

export function getTheme(): Theme {
  return (localStorage.getItem('octane-theme') as Theme) ?? 'light'
}

export function setTheme(theme: Theme): void {
  localStorage.setItem('octane-theme', theme)
  document.documentElement.classList.toggle('dark', theme === 'dark')
}

export function initTheme(): void {
  setTheme(getTheme())
}

export function toggleTheme(): Theme {
  const next = getTheme() === 'light' ? 'dark' : 'light'
  setTheme(next)
  return next
}
