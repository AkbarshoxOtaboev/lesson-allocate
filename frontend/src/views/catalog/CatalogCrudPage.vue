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

      <div
        v-if="props.kind === 'teachers'"
        class="grid grid-cols-1 gap-3 border-b border-gray-200 px-5 py-4 sm:grid-cols-3 dark:border-gray-800"
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
          <select v-model="selectedDepartmentId" class="filter-field" @change="onDepartmentFilterChange">
            <option value="">Barchasi</option>
            <option v-for="d in filteredDepartments" :key="d.id" :value="String(d.id)">
              {{ d.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">F.I.O. bo‘yicha qidirish</label>
          <input
            v-model="fioSearch"
            type="search"
            placeholder="Familiya, ism..."
            class="filter-field"
          />
        </div>
      </div>

      <div
        v-else-if="filterLabel"
        class="flex flex-wrap items-center justify-between gap-2 border-b border-brand-100 bg-brand-50/60 px-5 py-3 dark:border-brand-500/20 dark:bg-brand-500/10"
      >
        <p class="text-sm text-brand-700 dark:text-brand-300">
          Filtr: <span class="font-medium">{{ filterLabel }}</span>
        </p>
        <button
          type="button"
          class="text-sm font-medium text-brand-600 hover:underline dark:text-brand-300"
          @click="clearFilter"
        >
          Filtrni tozalash
        </button>
      </div>

      <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Nomi</th>
              <th
                v-if="props.kind === 'faculties'"
                class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500"
              >
                Kafedralar
              </th>
              <th
                v-if="props.kind === 'faculties'"
                class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500"
              >
                O'qituvchilar
              </th>
              <th
                v-if="props.kind === 'departments' || props.kind === 'teachers'"
                class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500"
              >
                Fakultet
              </th>
              <th
                v-if="props.kind === 'departments'"
                class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500"
              >
                O'qituvchilar
              </th>
              <th
                v-if="props.kind === 'teachers'"
                class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500"
              >
                Kafedra
              </th>
              <th
                v-if="props.kind === 'teachers'"
                class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500"
              >
                Lavozim
              </th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Status</th>
              <th class="px-5 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(item, index) in displayedItems" :key="item.id">
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-5 py-4 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                <button
                  v-if="canDrillDown"
                  type="button"
                  class="text-left text-brand-600 hover:underline dark:text-brand-400"
                  @click="openChildren(item)"
                >
                  {{ item.name }}
                </button>
                <span v-else>{{ item.name }}</span>
              </td>
              <td
                v-if="props.kind === 'faculties'"
                class="px-5 py-4 text-theme-sm text-gray-500"
              >
                {{ item.departmentCount ?? 0 }}
              </td>
              <td
                v-if="props.kind === 'faculties'"
                class="px-5 py-4 text-theme-sm text-gray-500"
              >
                {{ item.teacherCount ?? 0 }}
              </td>
              <td
                v-if="props.kind === 'departments' || props.kind === 'teachers'"
                class="px-5 py-4 text-theme-sm text-gray-500"
              >
                {{ item.facultyName || '—' }}
              </td>
              <td
                v-if="props.kind === 'departments'"
                class="px-5 py-4 text-theme-sm text-gray-500"
              >
                {{ item.teacherCount ?? 0 }}
              </td>
              <td
                v-if="props.kind === 'teachers'"
                class="px-5 py-4 text-theme-sm text-gray-500"
              >
                {{ item.departmentName || '—' }}
              </td>
              <td
                v-if="props.kind === 'teachers'"
                class="px-5 py-4 text-theme-sm text-gray-500"
              >
                {{ item.staffPositionName || '—' }}
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
            <tr v-if="!displayedItems.length">
              <td :colspan="colSpan" class="px-5 py-8 text-center text-sm text-gray-500">
                Bo‘sh
              </td>
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
            {{
              editingId
                ? props.kind === 'teachers'
                  ? "O'qituvchini tahrirlash"
                  : 'Tahrirlash'
                : props.kind === 'teachers'
                  ? "Yangi o'qituvchi"
                  : 'Yangi yozuv'
            }}
          </h3>
          <form class="space-y-4" @submit.prevent="save">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                {{ props.kind === 'teachers' ? 'F.I.O.' : 'Nomi' }}
              </label>
              <input
                v-model="name"
                required
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>

            <template v-if="props.kind === 'teachers'">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fakultet</label>
                <select v-model="formFacultyId" required class="filter-field" @change="onFormFacultyChange">
                  <option value="">Tanlang</option>
                  <option v-for="f in faculties" :key="f.id" :value="String(f.id)">
                    {{ f.name }}
                  </option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Kafedra</label>
                <select v-model="formDepartmentId" required class="filter-field">
                  <option value="">Tanlang</option>
                  <option v-for="d in formDepartments" :key="d.id" :value="String(d.id)">
                    {{ d.name }}
                  </option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Lavozim</label>
                <select v-model="formStaffPosition" required class="filter-field">
                  <option value="">Tanlang</option>
                  <option v-for="pos in staffPositionOptions" :key="pos" :value="pos">
                    {{ pos }}
                  </option>
                </select>
              </div>
            </template>

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
      :target="hemisTarget"
      @close="hemisOpen = false"
      @synced="onHemisSynced"
    />
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import HemisImportModal from '@/components/hemis/HemisImportModal.vue'
import { departmentApi, facultyApi, teacherApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { PencilAltIcon, TrashIcon } from '@/icons'
import type { NamedEntity } from '@/types/api'

type CatalogItem = NamedEntity & {
  staffPositionName?: string
  staffPositionCode?: string
  departmentName?: string
  facultyName?: string
  facultyId?: number
  departmentId?: number
  departmentCount?: number
  teacherCount?: number
  fullName?: string
  firstName?: string
  secondName?: string
  thirdName?: string
}

const props = defineProps<{
  kind: 'faculties' | 'departments' | 'teachers'
}>()

const route = useRoute()
const router = useRouter()

const catalogMap = {
  faculties: { title: 'Fakultetlar', api: facultyApi },
  departments: { title: 'Kafedralar', api: departmentApi },
  teachers: { title: "O'qituvchilar", api: teacherApi },
} as const

const meta = computed(() => catalogMap[props.kind])
const supportsHemis = computed(
  () => props.kind === 'faculties' || props.kind === 'departments' || props.kind === 'teachers',
)
const hemisTarget = computed(() =>
  props.kind === 'faculties' ? 'faculties' : props.kind === 'departments' ? 'departments' : 'teachers',
)
const canDrillDown = computed(() => props.kind === 'faculties' || props.kind === 'departments')

const facultyIdFilter = computed(() => {
  const raw = route.query.facultyId
  const value = Array.isArray(raw) ? raw[0] : raw
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : null
})

const departmentIdFilter = computed(() => {
  const raw = route.query.departmentId
  const value = Array.isArray(raw) ? raw[0] : raw
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : null
})

const filterLabel = computed(() => {
  if (props.kind === 'departments' && facultyIdFilter.value) {
    const name = String(route.query.facultyName || '').trim()
    return name ? `Fakultet — ${name}` : `Fakultet #${facultyIdFilter.value}`
  }
  return ''
})

const colSpan = computed(() => {
  if (props.kind === 'teachers') return 7
  if (props.kind === 'departments') return 6
  if (props.kind === 'faculties') return 6
  return 4
})

const items = ref<CatalogItem[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<CatalogItem[]>([])
const selectedFacultyId = ref('')
const selectedDepartmentId = ref('')
const fioSearch = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const formError = ref('')
const modalOpen = ref(false)
const hemisOpen = ref(false)
const editingId = ref<number | null>(null)
const name = ref('')
const formFacultyId = ref('')
const formDepartmentId = ref('')
const formStaffPosition = ref('')

const DEFAULT_POSITIONS = [
  "O'qituvchi",
  'Assistent',
  "Katta o'qituvchi",
  'Dotsent',
  'Professor',
  'Kafedra mudiri',
]

const filteredDepartments = computed(() => {
  if (!selectedFacultyId.value) return departments.value
  const facultyId = Number(selectedFacultyId.value)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const formDepartments = computed(() => {
  if (!formFacultyId.value) return departments.value
  const facultyId = Number(formFacultyId.value)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const staffPositionOptions = computed(() => {
  const fromData = items.value
    .map((i) => i.staffPositionName?.trim())
    .filter((v): v is string => Boolean(v))
  return [...new Set([...DEFAULT_POSITIONS, ...fromData])].sort((a, b) =>
    a.localeCompare(b, 'uz'),
  )
})

const displayedItems = computed(() => {
  if (props.kind !== 'teachers') return items.value
  const q = fioSearch.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((item) => {
    const parts = [
      item.name,
      item.fullName,
      item.firstName,
      item.secondName,
      item.thirdName,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return parts.includes(q)
  })
})

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function listParams() {
  if (props.kind === 'departments' && facultyIdFilter.value) {
    return { facultyId: facultyIdFilter.value }
  }
  if (props.kind === 'teachers') {
    const params: Record<string, number> = {}
    if (selectedDepartmentId.value) params.departmentId = Number(selectedDepartmentId.value)
    else if (selectedFacultyId.value) params.facultyId = Number(selectedFacultyId.value)
    return Object.keys(params).length ? params : undefined
  }
  return undefined
}

function syncTeacherFiltersFromRoute() {
  selectedFacultyId.value = facultyIdFilter.value ? String(facultyIdFilter.value) : ''
  selectedDepartmentId.value = departmentIdFilter.value ? String(departmentIdFilter.value) : ''
}

async function loadFilterOptions() {
  if (props.kind !== 'teachers') return
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
    const { data } = await meta.value.api.list(listParams())
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
  const query: Record<string, string> = {}
  if (selectedFacultyId.value) {
    query.facultyId = selectedFacultyId.value
    const faculty = faculties.value.find((f) => String(f.id) === selectedFacultyId.value)
    if (faculty?.name) query.facultyName = faculty.name
  }
  router.replace({ name: 'Teachers', query })
}

function onDepartmentFilterChange() {
  const query: Record<string, string> = {}
  if (selectedDepartmentId.value) {
    query.departmentId = selectedDepartmentId.value
    const department = departments.value.find((d) => String(d.id) === selectedDepartmentId.value)
    if (department?.name) query.departmentName = department.name
    const facultyId = department?.facultyId
    if (facultyId) {
      query.facultyId = String(facultyId)
      selectedFacultyId.value = String(facultyId)
      const faculty = faculties.value.find((f) => f.id === facultyId)
      if (faculty?.name) query.facultyName = faculty.name
    } else if (selectedFacultyId.value) {
      query.facultyId = selectedFacultyId.value
      const faculty = faculties.value.find((f) => String(f.id) === selectedFacultyId.value)
      if (faculty?.name) query.facultyName = faculty.name
    }
  } else if (selectedFacultyId.value) {
    query.facultyId = selectedFacultyId.value
    const faculty = faculties.value.find((f) => String(f.id) === selectedFacultyId.value)
    if (faculty?.name) query.facultyName = faculty.name
  }
  router.replace({ name: 'Teachers', query })
}

function openChildren(item: CatalogItem) {
  if (props.kind === 'faculties') {
    router.push({
      name: 'Departments',
      query: { facultyId: String(item.id), facultyName: item.name },
    })
    return
  }
  if (props.kind === 'departments') {
    const query: Record<string, string> = {
      departmentId: String(item.id),
      departmentName: item.name,
    }
    if (item.facultyId) {
      query.facultyId = String(item.facultyId)
      if (item.facultyName) query.facultyName = item.facultyName
    }
    router.push({ name: 'Teachers', query })
  }
}

function clearFilter() {
  if (props.kind === 'departments') {
    router.replace({ name: 'Departments' })
  }
}

function resetTeacherForm() {
  formFacultyId.value = selectedFacultyId.value || ''
  formDepartmentId.value = selectedDepartmentId.value || ''
  formStaffPosition.value = ''
}

function onFormFacultyChange() {
  formDepartmentId.value = ''
}

async function openCreate() {
  editingId.value = null
  name.value = ''
  formError.value = ''
  if (props.kind === 'teachers') {
    await loadFilterOptions()
    resetTeacherForm()
  }
  modalOpen.value = true
}

async function openEdit(item: CatalogItem) {
  editingId.value = item.id
  name.value = item.name
  formError.value = ''
  if (props.kind === 'teachers') {
    await loadFilterOptions()
    formFacultyId.value = item.facultyId ? String(item.facultyId) : ''
    formDepartmentId.value = item.departmentId ? String(item.departmentId) : ''
    formStaffPosition.value = item.staffPositionName || ''
  }
  modalOpen.value = true
}

async function save() {
  saving.value = true
  formError.value = ''
  try {
    if (props.kind === 'teachers') {
      if (!formFacultyId.value || !formDepartmentId.value || !formStaffPosition.value) {
        formError.value = 'Fakultet, kafedra va lavozimni tanlang'
        return
      }
      const payload = {
        name: name.value,
        departmentId: Number(formDepartmentId.value),
        staffPositionName: formStaffPosition.value,
      }
      if (editingId.value) await meta.value.api.update(editingId.value, payload)
      else await meta.value.api.create(payload)
    } else if (editingId.value) {
      await meta.value.api.update(editingId.value, { name: name.value })
    } else {
      await meta.value.api.create({ name: name.value })
    }
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function removeItem(item: CatalogItem) {
  if (!confirm(`"${item.name}" o‘chirilsinmi?`)) return
  try {
    await meta.value.api.remove(item.id)
    await load()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

async function onHemisSynced() {
  await loadFilterOptions()
  await load()
}

watch(
  () => [props.kind, route.query.facultyId, route.query.departmentId],
  async () => {
    hemisOpen.value = false
    if (props.kind === 'teachers') {
      syncTeacherFiltersFromRoute()
      await loadFilterOptions()
    }
    await load()
  },
)

onMounted(async () => {
  if (props.kind === 'teachers') {
    syncTeacherFiltersFromRoute()
    await loadFilterOptions()
  }
  await load()
})
</script>

<style scoped>
.filter-field {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}
:global(.dark) .filter-field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
