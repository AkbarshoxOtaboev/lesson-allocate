<template>
  <AdminLayout>
    <PageBreadcrumb :page-title="meta.title" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 px-5 py-4 dark:border-gray-800"
      >
        <div>
          <h3 class="font-semibold text-gray-800 dark:text-white/90">{{ meta.title }}</h3>
          <p class="text-sm text-gray-500">{{ meta.endpoint }}</p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button
            v-if="supportsHemis"
            type="button"
            class="rounded-lg border border-brand-300 px-4 py-2.5 text-sm font-medium text-brand-600 hover:bg-brand-50 dark:border-brand-500/40 dark:hover:bg-brand-500/10"
            @click="hemisOpen = true"
          >
            HEMIS dan yuklash
          </button>
          <button
            type="button"
            class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
            @click="openCreate"
          >
            Qo‘shish
          </button>
        </div>
      </div>

      <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">ID</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Nomi</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Status</th>
              <th class="px-5 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="item in items" :key="item.id">
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ item.id }}</td>
              <td class="px-5 py-4 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                {{ item.name }}
              </td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ item.status || '—' }}</td>
              <td class="px-5 py-4">
                <div class="flex justify-end gap-3">
                  <button class="text-sm text-brand-500" @click="openEdit(item)">Tahrirlash</button>
                  <button class="text-sm text-error-600" @click="removeItem(item)">O‘chirish</button>
                </div>
              </td>
            </tr>
            <tr v-if="!items.length">
              <td colspan="4" class="px-5 py-8 text-center text-sm text-gray-500">Bo‘sh</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div class="relative w-full max-w-md rounded-2xl bg-white p-6 dark:bg-gray-900" @click.stop>
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingId ? 'Tahrirlash' : 'Yangi yozuv' }}
          </h3>
          <form class="space-y-4" @submit.prevent="save">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Nomi</label>
              <input
                v-model="name"
                required
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
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

    <HemisImportModal
      v-if="hemisOpen && supportsHemis"
      :target="props.kind === 'faculties' ? 'faculties' : 'departments'"
      @close="hemisOpen = false"
      @synced="onHemisSynced"
    />
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import HemisImportModal from '@/components/hemis/HemisImportModal.vue'
import {
  departmentApi,
  facultyApi,
  groupApi,
} from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import type { NamedEntity } from '@/types/api'

const props = defineProps<{
  kind: 'faculties' | 'departments' | 'groups'
}>()

const catalogMap = {
  faculties: { title: 'Fakultetlar', endpoint: '/api/faculties', api: facultyApi },
  departments: { title: 'Kafedralar', endpoint: '/api/departments', api: departmentApi },
  groups: { title: 'Guruhlar', endpoint: '/api/groups', api: groupApi },
} as const

const meta = computed(() => catalogMap[props.kind])
const supportsHemis = computed(() => props.kind === 'faculties' || props.kind === 'departments')

const items = ref<NamedEntity[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const formError = ref('')
const modalOpen = ref(false)
const hemisOpen = ref(false)
const editingId = ref<number | null>(null)
const name = ref('')

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await meta.value.api.list()
    items.value = unwrapList(data)
  } catch (e) {
    error.value = getErrorMessage(e)
    items.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  name.value = ''
  formError.value = ''
  modalOpen.value = true
}

function openEdit(item: NamedEntity) {
  editingId.value = item.id
  name.value = item.name
  formError.value = ''
  modalOpen.value = true
}

async function save() {
  saving.value = true
  formError.value = ''
  try {
    if (editingId.value) await meta.value.api.update(editingId.value, { name: name.value })
    else await meta.value.api.create({ name: name.value })
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function removeItem(item: NamedEntity) {
  if (!confirm(`"${item.name}" o‘chirilsinmi?`)) return
  try {
    await meta.value.api.remove(item.id)
    await load()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

async function onHemisSynced() {
  await load()
}

watch(
  () => props.kind,
  () => {
    hemisOpen.value = false
    load()
  },
)

onMounted(load)
</script>
