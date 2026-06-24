import { useTranslation } from 'react-i18next'

export default function LanguageSwitcher() {
  const { i18n } = useTranslation()
  const current = i18n.language

  return (
    <div className="flex items-center gap-1 bg-surface rounded-lg p-0.5">
      <button
        onClick={() => i18n.changeLanguage('sk')}
        className={`px-2 py-1 text-xs rounded-md transition ${current === 'sk' ? 'bg-accent-primary text-white' : 'text-text-secondary hover:text-text-primary'}`}
        title="Slovenčina"
      >
        SK
      </button>
      <button
        onClick={() => i18n.changeLanguage('en')}
        className={`px-2 py-1 text-xs rounded-md transition ${current === 'en' ? 'bg-accent-primary text-white' : 'text-text-secondary hover:text-text-primary'}`}
        title="English"
      >
        EN
      </button>
    </div>
  )
}
