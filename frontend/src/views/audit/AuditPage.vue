<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Audit log" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div class="flex items-center justify-between border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <div>
          <h3 class="font-semibold text-gray-800 dark:text-white/90">Amallar tarixi</h3>
          <p class="text-sm text-gray-500">Faqat SUPER_ADMIN</p>
        </div>
        <button
          type="button"
          class="rounded-lg border border-gray-300 px-3 py-2 text-sm dark:border-gray-700"
          @click="load"
        >
          Yangilash
        </button>
      </div>

      <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Vaqt</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">User</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Amal</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Entity</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Method / URL</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">IP</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(log, index) in logs" :key="log.id">
              <td class="px-5 py-3 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-5 py-3 text-theme-sm text-gray-500">
                {{ formatDate(log.createdAt || log.timestamp) }}
              </td>
              <td class="px-5 py-3 text-theme-sm text-gray-800 dark:text-white/90">
                {{ log.username || '—' }}
              </td>
              <td class="px-5 py-3 text-theme-sm text-gray-500">{{ log.action || '—' }}</td>
              <td class="px-5 py-3 text-theme-sm text-gray-500">{{ log.entity || '—' }}</td>
              <td class="px-5 py-3 text-theme-sm text-gray-500">
                <span class="font-medium">{{ log.httpMethod }}</span>
                {{ log.url }}
              </td>
              <td class="px-5 py-3 text-theme-sm text-gray-500">{{ log.ipAddress || '—' }}</td>
            </tr>
            <tr v-if="!logs.length">
              <td colspan="7" class="px-5 py-8 text-center text-sm text-gray-500">
                Audit yozuvlari yo‘q
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { auditApi } from '@/api/audit'
import { getErrorMessage } from '@/api/http'
import type { AuditLog } from '@/types/api'

const logs = ref<AuditLog[]>([])
const loading = ref(false)
const error = ref('')

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function formatDate(value?: string) {
  if (!value) return '—'
  return new Date(value).toLocaleString()
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await auditApi.list()
    logs.value = unwrapList(data)
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
