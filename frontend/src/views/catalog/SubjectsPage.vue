<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Fanlar" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 px-5 py-4 dark:border-gray-800"
      >
        <h3 class="font-semibold text-gray-800 dark:text-white/90">Fanlar</h3>
        <button
          type="button"
          class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
          @click="openCreate"
        >
          Qo‘shish
        </button>
      </div>

      <div
        class="grid grid-cols-1 gap-3 border-b border-gray-200 px-5 py-4 sm:grid-cols-2 lg:grid-cols-3 dark:border-gray-800"
      >
        <div>
          <label class="mb-1 block text-xs text-gray-500">Fakultet</label>
          <select v-model="selectedFacultyId" class="filter-field" @change="onFacultyFilterChange">
            <option value="">Barchasi</option>
            <option v-for="f in faculties" :key="f.id" :value="String(f.id)">{{ f.name }}</option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Kafedra</label>
          <select v-model="selectedDepartmentId" class="filter-field" @change="load">
            <option value="">Barchasi</option>
            <option v-for="d in filteredDepartments" :key="d.id" :value="String(d.id)">
              {{ d.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Fan nomi bo‘yicha qidirish</label>
          <input
            v-model="nameSearch"
            type="search"
            placeholder="Fan nomi yoki kodi..."
            class="filter-field"
          />
        </div>
      </div>

      <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Kod</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Nomi</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Kafedra</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Jami</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Umumiy</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Kredit</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Guruhlar</th>
              <th class="px-4 py-3 text-left text-theme-xs font-medium text-gray-500">Talabalar</th>
              <th class="px-4 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(item, index) in displayedItems" :key="item.id">
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ item.code }}</td>
              <td class="px-4 py-4 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                {{ item.name }}
              </td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ item.departmentName || '—' }}</td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ item.totalHours ?? 0 }}</td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ item.overallHours ?? 0 }}</td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ formatCredit(item.credit) }}</td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ item.groupCount ?? 0 }}</td>
              <td class="px-4 py-4 text-theme-sm text-gray-500">{{ item.studentCount ?? 0 }}</td>
              <td class="px-4 py-4">
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
            <tr v-if="!displayedItems.length">
              <td colspan="10" class="px-5 py-8 text-center text-sm text-gray-500">Bo‘sh</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div
          class="relative max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingId ? 'Fanni tahrirlash' : 'Yangi fan' }}
          </h3>
          <form class="space-y-4" @submit.prevent="save">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Kafedra</label>
              <select v-model="formDepartmentId" required class="filter-field">
                <option value="">Tanlang</option>
                <option v-for="d in formDepartments" :key="d.id" :value="String(d.id)">
                  {{ d.name }}
                </option>
              </select>
            </div>

            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fan kodi</label>
                <input v-model="form.code" required class="form-field" />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fan nomi</label>
                <input v-model="form.name" required class="form-field" />
              </div>
            </div>

            <div>
              <p class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">Yuklama soatlar</p>
              <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
                <div>
                  <label class="mb-1 block text-xs text-gray-500">Maruza (soat)</label>
                  <input v-model.number="form.lectureHours" type="number" min="0" class="form-field" />
                </div>
                <div>
                  <label class="mb-1 block text-xs text-gray-500">Amaliy (soat)</label>
                  <input v-model.number="form.practicalHours" type="number" min="0" class="form-field" />
                </div>
                <div>
                  <label class="mb-1 block text-xs text-gray-500">Laboratoriya</label>
                  <input v-model.number="form.labHours" type="number" min="0" class="form-field" />
                </div>
                <div>
                  <label class="mb-1 block text-xs text-gray-500">Seminar</label>
                  <input v-model.number="form.seminarHours" type="number" min="0" class="form-field" />
                </div>
                <div>
                  <label class="mb-1 block text-xs text-gray-500">Mustaqil ta'lim</label>
                  <input
                    v-model.number="form.independentStudyHours"
                    type="number"
                    min="0"
                    class="form-field"
                  />
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-3 rounded-xl bg-gray-50 p-4 sm:grid-cols-3 dark:bg-gray-800/50">
              <div>
                <p class="text-xs text-gray-500">Jami soat</p>
                <p class="text-lg font-semibold text-gray-800 dark:text-white/90">{{ computedTotalHours }}</p>
                <p class="text-xs text-gray-400">Maruza + amaliy + lab + seminar</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Umumiy fan soati</p>
                <p class="text-lg font-semibold text-gray-800 dark:text-white/90">{{ computedOverallHours }}</p>
                <p class="text-xs text-gray-400">Jami + mustaqil ta'lim</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Kredit</p>
                <p class="text-lg font-semibold text-brand-600 dark:text-brand-400">{{ computedCredit }}</p>
                <p class="text-xs text-gray-400">Umumiy / 3</p>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Guruhlar soni</label>
                <input v-model.number="form.groupCount" type="number" min="0" class="form-field" />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Talabalar soni</label>
                <input v-model.number="form.studentCount" type="number" min="0" class="form-field" />
              </div>
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
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import { departmentApi, facultyApi, subjectApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { PencilAltIcon, TrashIcon } from '@/icons'
import type { NamedEntity, Subject } from '@/types/api'

type DepartmentItem = NamedEntity & { facultyId?: number }

const emptyForm = () => ({
  code: '',
  name: '',
  lectureHours: 0,
  practicalHours: 0,
  labHours: 0,
  seminarHours: 0,
  independentStudyHours: 0,
  groupCount: 0,
  studentCount: 0,
})

const items = ref<Subject[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<DepartmentItem[]>([])
const selectedFacultyId = ref('')
const selectedDepartmentId = ref('')
const nameSearch = ref('')
const formDepartmentId = ref('')
const form = ref(emptyForm())
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const formError = ref('')
const modalOpen = ref(false)
const editingId = ref<number | null>(null)

const filteredDepartments = computed(() => {
  if (!selectedFacultyId.value) return departments.value
  const facultyId = Number(selectedFacultyId.value)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const formDepartments = computed(() => departments.value)

const displayedItems = computed(() => {
  const q = nameSearch.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((item) => {
    const haystack = `${item.code} ${item.name}`.toLowerCase()
    return haystack.includes(q)
  })
})

const computedTotalHours = computed(() => {
  const f = form.value
  return orZero(f.lectureHours) + orZero(f.practicalHours) + orZero(f.labHours) + orZero(f.seminarHours)
})

const computedOverallHours = computed(() => computedTotalHours.value + orZero(form.value.independentStudyHours))

const computedCredit = computed(() => formatCredit(computedOverallHours.value / 3))

function orZero(value: number | undefined | null) {
  return Number.isFinite(Number(value)) ? Number(value) : 0
}

function formatCredit(value: number | undefined | null) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '0'
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function listParams() {
  const params: Record<string, number> = {}
  if (selectedDepartmentId.value) params.departmentId = Number(selectedDepartmentId.value)
  else if (selectedFacultyId.value) params.facultyId = Number(selectedFacultyId.value)
  return Object.keys(params).length ? params : undefined
}

async function loadFilterOptions() {
  try {
    const [facRes, depRes] = await Promise.all([facultyApi.list(), departmentApi.list()])
    faculties.value = unwrapList(facRes.data)
    departments.value = unwrapList(depRes.data)
  } catch {
    faculties.value = []
    departments.value = []
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await subjectApi.list(listParams())
    items.value = unwrapList(data)
  } catch (e) {
    error.value = getErrorMessage(e)
    items.value = []
  } finally {
    loading.value = false
  }
}

function onFacultyFilterChange() {
  selectedDepartmentId.value = ''
  load()
}

function resetForm() {
  form.value = emptyForm()
  formDepartmentId.value = selectedDepartmentId.value || ''
}

async function openCreate() {
  editingId.value = null
  formError.value = ''
  await loadFilterOptions()
  resetForm()
  modalOpen.value = true
}

async function openEdit(item: Subject) {
  editingId.value = item.id
  formError.value = ''
  await loadFilterOptions()
  formDepartmentId.value = item.departmentId ? String(item.departmentId) : ''
  form.value = {
    code: item.code,
    name: item.name,
    lectureHours: item.lectureHours ?? 0,
    practicalHours: item.practicalHours ?? 0,
    labHours: item.labHours ?? 0,
    seminarHours: item.seminarHours ?? 0,
    independentStudyHours: item.independentStudyHours ?? 0,
    groupCount: item.groupCount ?? 0,
    studentCount: item.studentCount ?? 0,
  }
  modalOpen.value = true
}

async function save() {
  saving.value = true
  formError.value = ''
  try {
    if (!formDepartmentId.value) {
      formError.value = 'Kafedrani tanlang'
      return
    }
    const payload = {
      departmentId: Number(formDepartmentId.value),
      code: form.value.code.trim(),
      name: form.value.name.trim(),
      lectureHours: orZero(form.value.lectureHours),
      practicalHours: orZero(form.value.practicalHours),
      labHours: orZero(form.value.labHours),
      seminarHours: orZero(form.value.seminarHours),
      independentStudyHours: orZero(form.value.independentStudyHours),
      groupCount: orZero(form.value.groupCount),
      studentCount: orZero(form.value.studentCount),
    }
    if (editingId.value) await subjectApi.update(editingId.value, payload)
    else await subjectApi.create(payload)
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function removeItem(item: Subject) {
  if (!confirm(`"${item.name}" o‘chirilsinmi?`)) return
  try {
    await subjectApi.remove(item.id)
    await load()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

onMounted(async () => {
  await loadFilterOptions()
  await load()
})
</script>

<style scoped>
.filter-field,
.form-field {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}
:global(.dark) .filter-field,
:global(.dark) .form-field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
