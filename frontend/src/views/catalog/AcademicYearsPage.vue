<template>
  <AdminLayout>
    <PageBreadcrumb page-title="O'quv yili" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 px-5 py-4 dark:border-gray-800"
      >
        <div>
          <h3 class="font-semibold text-gray-800 dark:text-white/90">O'quv yillari</h3>
          <p class="mt-1 text-sm text-gray-500">Masalan: 2025-2026, 2026-2027</p>
        </div>
        <button
          type="button"
          class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
          @click="openCreate"
        >
          Qo‘shish
        </button>
      </div>

      <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">O'quv yili</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Boshlanish</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Tugash</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Joriy</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Status</th>
              <th class="px-5 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(item, index) in items" :key="item.id">
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-5 py-4 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                {{ item.name }}
              </td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ item.startYear }}</td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ item.endYear }}</td>
              <td class="px-5 py-4 text-theme-sm">
                <span
                  v-if="item.currentYear"
                  class="rounded-full bg-success-50 px-2.5 py-0.5 text-xs font-medium text-success-700 dark:bg-success-500/15 dark:text-success-400"
                >
                  Joriy
                </span>
                <span v-else class="text-gray-400">—</span>
              </td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ item.status || '—' }}</td>
              <td class="px-5 py-4">
                <div class="flex justify-end gap-2">
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-warning-500 hover:bg-warning-50 dark:hover:bg-warning-500/10"
                    title="Tahrirlash"
                    @click="openEdit(item)"
                  >
                    <PencilAltIcon class="size-5" />
                  </button>
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-error-600 hover:bg-error-50 dark:hover:bg-error-500/10"
                    title="O‘chirish"
                    @click="removeItem(item)"
                  >
                    <TrashIcon class="size-5" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!items.length">
              <td colspan="7" class="px-5 py-8 text-center text-sm text-gray-500">Bo‘sh</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div
          class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingId ? "O'quv yilini tahrirlash" : "Yangi o'quv yili" }}
          </h3>
          <form class="space-y-4" @submit.prevent="save">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                  Boshlanish yili
                </label>
                <input
                  v-model.number="startYear"
                  type="number"
                  required
                  min="2000"
                  max="2100"
                  placeholder="2025"
                  class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  @input="onStartYearChange"
                />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                  Tugash yili
                </label>
                <input
                  v-model.number="endYear"
                  type="number"
                  required
                  min="2000"
                  max="2100"
                  placeholder="2026"
                  class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                />
              </div>
            </div>

            <div
              class="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-sm dark:border-gray-700 dark:bg-white/[0.03]"
            >
              <span class="text-gray-500">Nomi:</span>
              <span class="ml-2 font-medium text-gray-800 dark:text-white/90">
                {{ previewName || '—' }}
              </span>
            </div>

            <label class="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
              <input v-model="currentYear" type="checkbox" class="rounded border-gray-300" />
              Joriy o'quv yili sifatida belgilash
            </label>

            <div v-if="formError" class="text-sm text-error-600">{{ formError }}</div>
            <div class="flex justify-end gap-2">
              <button
                type="button"
                class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
                @click="modalOpen = false"
              >
                Bekor
              </button>
              <button
                type="submit"
                class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
                :disabled="saving"
              >
                Saqlash
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import { academicYearApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { PencilAltIcon, TrashIcon } from '@/icons'
import type { EntityStatus } from '@/types/api'

type AcademicYearItem = {
  id: number
  name: string
  startYear: number
  endYear: number
  currentYear?: boolean
  status?: EntityStatus
}

const items = ref<AcademicYearItem[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const formError = ref('')
const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const startYear = ref<number | null>(null)
const endYear = ref<number | null>(null)
const currentYear = ref(false)

const previewName = computed(() => {
  if (!startYear.value || !endYear.value) return ''
  return `${startYear.value}-${endYear.value}`
})

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function onStartYearChange() {
  if (startYear.value) {
    endYear.value = startYear.value + 1
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await academicYearApi.list()
    items.value = unwrapList(data) as AcademicYearItem[]
  } catch (e) {
    error.value = getErrorMessage(e)
    items.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  const year = new Date().getFullYear()
  startYear.value = year
  endYear.value = year + 1
  currentYear.value = !items.value.some((i) => i.currentYear)
  formError.value = ''
  modalOpen.value = true
}

function openEdit(item: AcademicYearItem) {
  editingId.value = item.id
  startYear.value = item.startYear
  endYear.value = item.endYear
  currentYear.value = Boolean(item.currentYear)
  formError.value = ''
  modalOpen.value = true
}

async function save() {
  if (!startYear.value || !endYear.value) {
    formError.value = 'Yillarni kiriting'
    return
  }
  if (endYear.value !== startYear.value + 1) {
    formError.value = "Tugash yili boshlanishdan 1 ga katta bo'lishi kerak"
    return
  }

  saving.value = true
  formError.value = ''
  try {
    const payload = {
      startYear: startYear.value,
      endYear: endYear.value,
      currentYear: currentYear.value,
    }
    if (editingId.value) await academicYearApi.update(editingId.value, payload)
    else await academicYearApi.create(payload)
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function removeItem(item: AcademicYearItem) {
  if (!confirm(`"${item.name}" o'quv yili o‘chirilsinmi?`)) return
  try {
    await academicYearApi.remove(item.id)
    await load()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

onMounted(load)
</script>
