import { createI18n } from 'vue-i18n'
import uz from './locales/uz'
import ru from './locales/ru'
import en from './locales/en'

export type AppLocale = 'uz' | 'ru' | 'en'

const LOCALE_KEY = 'urspi_locale'

function resolveLocale(): AppLocale {
  const saved = localStorage.getItem(LOCALE_KEY) as AppLocale | null
  if (saved === 'uz' || saved === 'ru' || saved === 'en') return saved
  return 'uz'
}

export const i18n = createI18n({
  legacy: false,
  locale: resolveLocale(),
  fallbackLocale: 'uz',
  messages: { uz, ru, en },
})

export function setLocale(locale: AppLocale) {
  i18n.global.locale.value = locale
  localStorage.setItem(LOCALE_KEY, locale)
  document.documentElement.lang = locale
}

export function getLocale(): AppLocale {
  return i18n.global.locale.value as AppLocale
}
