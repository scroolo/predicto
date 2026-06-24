import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import sk from './sk.json'
import en from './en.json'

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: { sk: { translation: sk }, en: { translation: en } },
    fallbackLng: 'sk',
    detection: { order: ['localStorage', 'navigator'], caches: ['localStorage'], lookupLocalStorage: 'predicto_lang' },
    interpolation: { escapeValue: false },
  })

export default i18n
