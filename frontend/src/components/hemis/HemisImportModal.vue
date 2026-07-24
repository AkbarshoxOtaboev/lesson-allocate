<template>
  <Modal full-screen-backdrop @close="emit('close')">
    <template #body>
      <div
        class="relative flex max-h-[90vh] w-full max-w-4xl flex-col rounded-2xl bg-white dark:bg-gray-900"
        @click.stop
      >
        <div class="border-b border-gray-200 px-6 py-4 dark:border-gray-800">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
            HEMIS dan yuklash — {{ title }}
          </h3>
          <p class="mt-1 text-sm text-gray-500">
            GET /v1/data/department-list parametrlari bo‘yicha
          </p>
        </div>

        <div class="space-y-4 overflow-y-auto px-6 py-4">
          <div class="grid grid-cols-2 gap-3 md:grid-cols-5">
            <div>
              <label class="mb-1 block text-xs text-gray-500">page</label>
              <input v-model.number="filters.page" type="number" min="1" class="field" />
            </div>
            <div>
              <label class="mb-1 block text-xs text-gray-500">limit (1–200)</label>
              <input v-model.number="filters.limit" type="number" min="1" max="200" class="field" />
            </div>
            <div>
              <label class="mb-1 block text-xs text-gray-500">active</label>
              <select v-model="filters.active" class="field">
                <option value="1">1 (faol)</option>
                <option value="0">0 (nofaol)</option>
                <option value="all">all</option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-xs text-gray-500">_structure_type</label>
              <input
                v-model="filters.structureType"
                type="text"
                placeholder="11, 12, 13..."
                class="field"
              />
            </div>
            <div>
              <label class="mb-1 block text-xs text-gray-500">parent</label>
              <input
                v-model.number="filters.parent"
                type="number"
                min="1"
                placeholder="parent id"
                class="field"
              />
            </div>
          </div>

          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
              :disabled="loading"
              @click="load"
            >
              {{ loading ? 'Yuklanmoqda...' : 'Ro‘yxatni yuklash' }}
            </button>
            <button
              type="button"
              class="rounded-lg border border-brand-300 px-4 py-2.5 text-sm text-brand-600 disabled:opacity-60 dark:border-brand-500/40"
              :disabled="syncing || !items.length"
              @click="sync"
            >
              {{ syncing ? 'Saqlanmoqda...' : 'Lokal bazaga sync' }}
            </button>
            <button
              type="button"
              class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm dark:border-gray-700"
              @click="emit('close')"
            >
              Yopish
            </button>
          </div>

          <div v-if="error" class="text-sm text-error-600">{{ error }}</div>
          <div v-if="syncResult" class="text-sm text-success-600">
            Sync: fetched={{ syncResult.fetched }}, created={{ syncResult.created }},
            updated={{ syncResult.updated }}, skipped={{ syncResult.skipped }}
          </div>
          <p v-if="meta" class="text-xs text-gray-500">
            Sahifa {{ meta.page || filters.page }} / {{ meta.pageCount || '—' }} · jami
            {{ meta.totalCount ?? items.length }}
          </p>

          <div class="max-h-[420px] overflow-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table class="min-w-full">
              <thead class="sticky top-0 bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">ID</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Nomi</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Kod</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Parent</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Turi</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
                <tr v-for="item in items" :key="item.id">
                  <td class="px-3 py-2 text-sm text-gray-500">{{ item.id }}</td>
                  <td class="px-3 py-2 text-sm font-medium text-gray-800 dark:text-white/90">
                    {{ item.name }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">{{ item.code || '—' }}</td>
                  <td class="px-3 py-2 text-sm text-gray-500">{{ item.parent ?? '—' }}</td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.structureType?.name || item.structureType?.code || '—' }}
                  </td>
                </tr>
                <tr v-if="!items.length && !loading">
                  <td colspan="5" class="px-3 py-8 text-center text-sm text-gray-500">
                    Parametrlarni to‘ldiring va “Ro‘yxatni yuklash” ni bosing
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
  </Modal>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import Modal from '@/components/ui/Modal.vue'
import { getErrorMessage } from '@/api/http'
import {
  hemisApi,
  type HemisDepartment,
  type HemisDepartmentListResponse,
  type HemisSyncResult,
} from '@/api/hemis'

const props = defineProps<{
  target: 'faculties' | 'departments'
}>()

const emit = defineEmits<{
  close: []
  synced: []
}>()

const title = props.target === 'faculties' ? 'Fakultetlar' : 'Kafedralar'

const filters = reactive({
  page: 1,
  limit: 50,
  active: '1',
  structureType: '',
  parent: undefined as number | undefined,
})

const items = ref<HemisDepartment[]>([])
const meta = ref<Pick<
  HemisDepartmentListResponse,
  'page' | 'pageCount' | 'totalCount' | 'pageSize'
> | null>(null)
const loading = ref(false)
const syncing = ref(false)
const error = ref('')
const syncResult = ref<HemisSyncResult | null>(null)

function queryPayload() {
  return {
    page: filters.page || 1,
    limit: filters.limit || 50,
    active: filters.active || '1',
    structureType: filters.structureType || undefined,
    parent: filters.parent || undefined,
  }
}

async function load() {
  loading.value = true
  error.value = ''
  syncResult.value = null
  try {
    const { data } = await hemisApi.fetchDepartments(queryPayload())
    items.value = data.items || []
    meta.value = {
      page: data.page,
      pageCount: data.pageCount,
      totalCount: data.totalCount,
      pageSize: data.pageSize,
    }
  } catch (e) {
    error.value = getErrorMessage(e)
    items.value = []
    meta.value = null
  } finally {
    loading.value = false
  }
}

async function sync() {
  syncing.value = true
  error.value = ''
  try {
    const { data } =
      props.target === 'faculties'
        ? await hemisApi.syncFaculties(queryPayload())
        : await hemisApi.syncDepartments(queryPayload())
    syncResult.value = data
    emit('synced')
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    syncing.value = false
  }
}
</script>

<style scoped>
.field {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}
:global(.dark) .field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
