<template>
  <div class="relative" ref="dropdownRef">
    <button
      class="relative flex items-center justify-center text-gray-500 transition-colors bg-white border border-gray-200 rounded-full hover:text-dark-900 h-11 w-11 hover:bg-gray-100 hover:text-gray-700 dark:border-gray-800 dark:bg-gray-900 dark:text-gray-400 dark:hover:bg-gray-800 dark:hover:text-white"
      @click="toggleDropdown"
    >
      <span
        :class="{ hidden: !notifying, flex: notifying }"
        class="absolute right-0 top-0.5 z-1 h-2 w-2 rounded-full bg-orange-400"
      >
        <span class="absolute inline-flex w-full h-full bg-orange-400 rounded-full opacity-75 -z-1 animate-ping"></span>
      </span>
      <svg class="fill-current" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M10.75 2.29248C10.75 1.87827 10.4143 1.54248 10 1.54248C9.58583 1.54248 9.25004 1.87827 9.25004 2.29248V2.83613C6.08266 3.20733 3.62504 5.9004 3.62504 9.16748V14.4591H3.33337C2.91916 14.4591 2.58337 14.7949 2.58337 15.2091C2.58337 15.6234 2.91916 15.9591 3.33337 15.9591H4.37504H15.625H16.6667C17.0809 15.9591 17.4167 15.6234 17.4167 15.2091C17.4167 14.7949 17.0809 14.4591 16.6667 14.4591H16.375V9.16748C16.375 5.9004 13.9174 3.20733 10.75 2.83613V2.29248ZM14.875 14.4591V9.16748C14.875 6.47509 12.6924 4.29248 10 4.29248C7.30765 4.29248 5.12504 6.47509 5.12504 9.16748V14.4591H14.875ZM8.00004 17.7085C8.00004 18.1228 8.33583 18.4585 8.75004 18.4585H11.25C11.6643 18.4585 12 18.1228 12 17.7085C12 17.2943 11.6643 16.9585 11.25 16.9585H8.75004C8.33583 16.9585 8.00004 17.2943 8.00004 17.7085Z"
        />
      </svg>
    </button>

    <div
      v-if="dropdownOpen"
      class="absolute -right-[240px] mt-[17px] flex h-[480px] w-[350px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-theme-lg dark:border-gray-800 dark:bg-gray-dark sm:w-[361px] lg:right-0"
    >
      <div class="flex items-center justify-between pb-3 mb-3 border-b border-gray-100 dark:border-gray-800">
        <h5 class="text-lg font-semibold text-gray-800 dark:text-white/90">{{ t('header.notifications') }}</h5>
        <button @click="closeDropdown" class="text-gray-500 dark:text-gray-400">×</button>
      </div>

      <div v-if="loading" class="py-10 text-center text-sm text-gray-500">Yuklanmoqda...</div>
      <ul v-else class="flex flex-col h-auto overflow-y-auto custom-scrollbar">
        <li v-for="notification in notifications" :key="notification.id">
          <router-link
            :to="notification.to || '/talabnomalar'"
            class="flex gap-3 rounded-lg border-b border-gray-100 p-3 px-4.5 py-3 hover:bg-gray-100 dark:border-gray-800 dark:hover:bg-white/5"
            @click="closeDropdown"
          >
            <span class="flex h-10 w-10 items-center justify-center rounded-full bg-brand-500 text-sm font-bold text-white">
              {{ notification.title.slice(0, 1).toUpperCase() }}
            </span>
            <span class="block">
              <span class="mb-1.5 block text-theme-sm text-gray-800 dark:text-white/90">
                {{ notification.title }}
              </span>
              <span class="block text-xs text-gray-500 dark:text-gray-400">
                {{ notification.description }}
              </span>
              <span class="mt-1 flex items-center gap-2 text-gray-500 text-theme-xs dark:text-gray-400">
                <span>{{ notification.meta }}</span>
                <span v-if="notification.time" class="w-1 h-1 bg-gray-400 rounded-full"></span>
                <span>{{ notification.time }}</span>
              </span>
            </span>
          </router-link>
        </li>
        <li v-if="!notifications.length" class="py-10 text-center text-sm text-gray-500">
          Bildirishnoma yo'q
        </li>
      </ul>

      <router-link
        to="/talabnomalar"
        class="mt-3 flex justify-center rounded-lg border border-gray-300 bg-white p-3 text-theme-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200"
        @click="closeDropdown"
      >
        {{ t('header.viewAllNotifications') }}
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { talabnomaApi } from '@/api/talabnoma'
import { useAuthStore } from '@/stores/auth'
import type { HeaderNotificationItem } from '@/types/api'
import type { Talabnoma } from '@/types/talabnoma'

const { t } = useI18n()
const auth = useAuthStore()
const dropdownOpen = ref(false)
const notifying = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const notifications = ref<HeaderNotificationItem[]>([])

const hasNotification = computed(() => notifications.value.length > 0)

function formatTime(value?: string) {
  if (!value) return ''
  return new Date(value).toLocaleString('uz-UZ', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function toNotification(item: Talabnoma): HeaderNotificationItem {
  if (auth.isKafedra) {
    return {
      id: item.id,
      title: item.fromFacultyName || 'Fakultetdan talabnoma',
      description: `${item.subjectName} fani ${item.toDepartmentName || 'kafedra'}ga yuborilgan`,
      meta: item.code,
      time: formatTime(item.createdAt),
      to: '/talabnomalar',
    }
  }
  return {
    id: item.id,
    title: item.subjectName,
    description: `${item.toDepartmentName || 'Kafedra'} tomonidan ${item.requestStatus === 'ALLOCATED' ? "to'liq" : item.requestStatus === 'PARTIAL' ? 'qisman' : 'qabul qilib'} taqsimlangan`,
    meta: item.code,
    time: formatTime(item.createdAt),
    to: '/talabnomalar',
  }
}

async function loadNotifications() {
  if (!auth.isAuthenticated) return
  loading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (auth.isKafedra) {
      params.status = 'NEW'
    } else if (auth.isDekan || auth.hasFullAccess) {
      // accepted/partial/allocated list is filtered on client
    }
    const { data } = await talabnomaApi.list(params as any)
    const rows = Array.isArray(data) ? data : ((data as any)?.content || (data as any)?.data || [])
    const filtered = (rows as Talabnoma[]).filter((item) => {
      if (auth.isKafedra) return item.requestStatus === 'NEW'
      return item.requestStatus === 'ACCEPTED' || item.requestStatus === 'PARTIAL' || item.requestStatus === 'ALLOCATED'
    })
    notifications.value = filtered.slice(0, 8).map(toNotification)
    notifying.value = notifications.value.length > 0
  } catch {
    notifications.value = []
    notifying.value = false
  } finally {
    loading.value = false
  }
}

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
  if (dropdownOpen.value) notifying.value = false
}

function closeDropdown() {
  dropdownOpen.value = false
}

function handleClickOutside(event: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    closeDropdown()
  }
}

onMounted(() => {
  void loadNotifications()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
