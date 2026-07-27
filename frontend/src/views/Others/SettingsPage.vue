<template>
  <admin-layout>
    <PageBreadcrumb :pageTitle="t('settings.title')" />

    <div
      class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6"
    >
      <h3 class="mb-6 text-lg font-semibold text-gray-800 dark:text-white/90">
        {{ t('settings.title') }}
      </h3>

      <div class="mb-8 max-w-xl">
        <p class="mb-3 text-sm font-medium text-gray-700 dark:text-gray-300">
          {{ t('settings.language') }}
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="opt in languages"
            :key="opt.code"
            type="button"
            class="rounded-lg border px-4 py-2.5 text-sm font-medium transition"
            :class="
              locale === opt.code
                ? 'border-brand-500 bg-brand-500 text-white'
                : 'border-gray-300 text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]'
            "
            @click="changeLocale(opt.code)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div class="max-w-xl">
        <p class="mb-3 text-sm font-medium text-gray-700 dark:text-gray-300">
          {{ t('settings.theme') }}
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="rounded-lg border px-4 py-2.5 text-sm font-medium transition"
            :class="
              !isDarkMode
                ? 'border-brand-500 bg-brand-500 text-white'
                : 'border-gray-300 text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]'
            "
            @click="setTheme('light')"
          >
            {{ t('settings.light') }}
          </button>
          <button
            type="button"
            class="rounded-lg border px-4 py-2.5 text-sm font-medium transition"
            :class="
              isDarkMode
                ? 'border-brand-500 bg-brand-500 text-white'
                : 'border-gray-300 text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]'
            "
            @click="setTheme('dark')"
          >
            {{ t('settings.dark') }}
          </button>
        </div>
      </div>
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { useTheme } from '@/components/layout/ThemeProvider.vue'
import { setLocale, type AppLocale } from '@/i18n'

const { t, locale } = useI18n()
const { isDarkMode, setTheme } = useTheme() as {
  isDarkMode: { value: boolean }
  setTheme: (theme: 'light' | 'dark') => void
}

const languages = computed(() => [
  { code: 'uz' as AppLocale, label: t('settings.langUz') },
  { code: 'ru' as AppLocale, label: t('settings.langRu') },
  { code: 'en' as AppLocale, label: t('settings.langEn') },
])

function changeLocale(code: AppLocale) {
  setLocale(code)
}
</script>
