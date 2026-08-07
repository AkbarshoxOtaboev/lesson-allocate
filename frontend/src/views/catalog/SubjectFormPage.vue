<template>
  <AdminLayout>
    <PageBreadcrumb :page-title="pageTitle" />

    <div
      class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6"
    >
      <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">{{ pageTitle }}</h3>
          <p class="mt-1 text-sm text-gray-500">
            Fan ma'lumotlari va yuklama soatlarini to‘ldiring
          </p>
        </div>
        <button
          type="button"
          class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
          @click="goBack"
        >
          ← Orqaga
        </button>
      </div>

      <div v-if="pageLoading" class="py-10 text-sm text-gray-500">Yuklanmoqda...</div>

      <form v-else class="space-y-4" @submit.prevent="save">
        <div>
          <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Kafedra</label>
          <select v-model="formDepartmentId" required class="filter-field" @change="onDepartmentChange">
            <option value="">Tanlang</option>
            <option v-for="d in departments" :key="d.id" :value="String(d.id)">
              {{ d.name }}
            </option>
          </select>
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <div>
            <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">O'quv yili</label>
            <select v-model="form.academicYearId" required class="filter-field">
              <option value="">Tanlang</option>
              <option v-for="ay in academicYears" :key="ay.id" :value="ay.id">
                {{ ay.name }}
              </option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Semestr</label>
            <select v-model="form.semester" required class="filter-field">
              <option value="AUTUMN">Kuzgi semestr</option>
              <option value="SPRING">Bahorgi semestr</option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Ta'lim turi</label>
            <select v-model="form.educationType" required class="filter-field">
              <option value="KUNDUZGI">Kunduzgi</option>
              <option value="KECHKI">Kechki</option>
              <option value="MASOFAVIY">Masofaviy</option>
              <option value="SIRTQI">Sirtqi</option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Ta'lim tili</label>
            <select v-model="form.educationLanguage" required class="filter-field">
              <option value="UZB">Uzb</option>
              <option value="RUS">Rus</option>
            </select>
          </div>
        </div>

        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div class="relative">
            <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Yo'nalish</label>
            <input
              v-model="directionSearch"
              type="text"
              required
              placeholder="Kod yoki nom bo'yicha qidiring"
              class="form-field"
              @focus="directionDropdownOpen = true"
              @input="onDirectionInput"
              @blur="closeDirectionDropdown"
            />
            <div
              v-if="directionDropdownOpen && filteredDirections.length"
              class="absolute z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-xl border border-gray-200 bg-white py-1 shadow-lg dark:border-gray-700 dark:bg-gray-900"
            >
              <button
                v-for="direction in filteredDirections"
                :key="direction.id"
                type="button"
                class="flex w-full items-start justify-between gap-3 px-3 py-2 text-left hover:bg-gray-50 dark:hover:bg-gray-800"
                @mousedown.prevent="selectDirection(direction)"
              >
                <span class="font-medium text-gray-800 dark:text-white/90">
                  {{ direction.directionName }}
                </span>
                <span class="text-xs text-gray-500">{{ direction.directionCode }}</span>
              </button>
            </div>
          </div>
          <div>
            <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Kurs</label>
            <select v-model.number="form.courseYear" required class="filter-field">
              <option :value="1">1-kurs</option>
              <option :value="2">2-kurs</option>
              <option :value="3">3-kurs</option>
              <option :value="4">4-kurs</option>
              <option :value="5">5-kurs</option>
            </select>
          </div>
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
          <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
            Umumiy fan soati
          </label>
          <input
            v-model.number="form.totalSubjectHours"
            type="number"
            min="1"
            required
            placeholder="masalan: 1200"
            class="form-field"
          />
          <p class="mt-1 text-xs text-gray-500">Reyting soati qolgan umumiy soatga ta'sir qilmaydi.</p>
        </div>

        <div>
          <div class="mb-2 flex flex-wrap items-center justify-between gap-2">
            <p class="text-sm font-medium text-gray-700 dark:text-gray-300">Yuklama soatlari</p>
            <p
              class="text-sm font-medium"
              :class="remainingNonRatingHours === 0 ? 'text-success-600' : 'text-warning-600'"
            >
              Qolgan: {{ remainingNonRatingHours }} soat
            </p>
          </div>
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div class="relative">
              <p
                v-if="activeHourField === 'lectureHours'"
                class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
              >
                Mavjud: {{ availableHoursFor('lectureHours') }}
              </p>
              <label class="mb-1 block text-xs text-gray-500">Maruza (soat)</label>
              <input
                :value="form.lectureHours"
                type="number"
                min="0"
                class="form-field"
                :title="hourInputTitle('lectureHours')"
                @focus="activeHourField = 'lectureHours'"
                @mouseenter="activeHourField = 'lectureHours'"
                @mouseleave="activeHourField = null"
                @blur="activeHourField = null"
                @input="onHourInput('lectureHours', $event)"
              />
            </div>
            <div class="relative">
              <p
                v-if="activeHourField === 'practicalHours'"
                class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
              >
                Mavjud: {{ availableHoursFor('practicalHours') }}
              </p>
              <label class="mb-1 block text-xs text-gray-500">Amaliy (soat)</label>
              <input
                :value="form.practicalHours"
                type="number"
                min="0"
                class="form-field"
                :title="hourInputTitle('practicalHours')"
                @focus="activeHourField = 'practicalHours'"
                @mouseenter="activeHourField = 'practicalHours'"
                @mouseleave="activeHourField = null"
                @blur="activeHourField = null"
                @input="onHourInput('practicalHours', $event)"
              />
            </div>
            <div class="relative">
              <p
                v-if="activeHourField === 'labHours'"
                class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
              >
                Mavjud: {{ availableHoursFor('labHours') }}
              </p>
              <label class="mb-1 block text-xs text-gray-500">Laboratoriya</label>
              <input
                :value="form.labHours"
                type="number"
                min="0"
                class="form-field"
                :title="hourInputTitle('labHours')"
                @focus="activeHourField = 'labHours'"
                @mouseenter="activeHourField = 'labHours'"
                @mouseleave="activeHourField = null"
                @blur="activeHourField = null"
                @input="onHourInput('labHours', $event)"
              />
            </div>
            <div class="relative">
              <p
                v-if="activeHourField === 'seminarHours'"
                class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
              >
                Mavjud: {{ availableHoursFor('seminarHours') }}
              </p>
              <label class="mb-1 block text-xs text-gray-500">Seminar</label>
              <input
                :value="form.seminarHours"
                type="number"
                min="0"
                class="form-field"
                :title="hourInputTitle('seminarHours')"
                @focus="activeHourField = 'seminarHours'"
                @mouseenter="activeHourField = 'seminarHours'"
                @mouseleave="activeHourField = null"
                @blur="activeHourField = null"
                @input="onHourInput('seminarHours', $event)"
              />
            </div>
            <div class="relative">
              <p
                v-if="activeHourField === 'ratingHours'"
                class="absolute -top-2 right-0 rounded-md bg-sky-50 px-2 py-1 text-[11px] font-semibold text-sky-600 shadow-sm ring-1 ring-sky-100"
              >
                Reyting erkin
              </p>
              <label class="mb-1 block text-xs text-gray-500">Reyting</label>
              <input
                :value="form.ratingHours"
                type="number"
                min="0"
                class="form-field"
                :title="hourInputTitle('ratingHours')"
                @focus="activeHourField = 'ratingHours'"
                @mouseenter="activeHourField = 'ratingHours'"
                @mouseleave="activeHourField = null"
                @blur="activeHourField = null"
                @input="onHourInput('ratingHours', $event)"
              />
            </div>
            <div class="relative">
              <p
                v-if="activeHourField === 'independentStudyHours'"
                class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
              >
                Mavjud: {{ availableHoursFor('independentStudyHours') }}
              </p>
              <label class="mb-1 block text-xs text-gray-500">Mustaqil ta'lim</label>
              <input
                :value="form.independentStudyHours"
                type="number"
                min="0"
                class="form-field"
                :title="hourInputTitle('independentStudyHours')"
                @focus="activeHourField = 'independentStudyHours'"
                @mouseenter="activeHourField = 'independentStudyHours'"
                @mouseleave="activeHourField = null"
                @blur="activeHourField = null"
                @input="onHourInput('independentStudyHours', $event)"
              />
            </div>
          </div>
          <p v-if="hoursWarning" class="mt-2 text-sm text-warning-600">{{ hoursWarning }}</p>
        </div>

        <div class="grid grid-cols-1 gap-3 rounded-xl bg-gray-50 p-4 sm:grid-cols-3 dark:bg-gray-800/50">
          <div>
            <p class="text-xs text-gray-500">Auditoriya soatlari</p>
            <p class="text-lg font-semibold text-gray-800 dark:text-white/90">
              {{ computedAuditoriyHours }}
            </p>
            <p class="text-xs text-gray-400">Maruza + amaliy + lab + seminar + reyting</p>
          </div>
          <div>
            <p class="text-xs text-gray-500">Umumiy soat</p>
            <p class="text-lg font-semibold text-gray-800 dark:text-white/90">
              {{ computedOverallHours }}
            </p>
            <p class="text-xs text-gray-400">Auditoriya + mustaqil ta'lim</p>
          </div>
          <div>
            <p class="text-xs text-gray-500">Kredit</p>
            <p class="text-lg font-semibold text-brand-600 dark:text-brand-400">
              {{ computedCredit }}
            </p>
            <p class="text-xs text-gray-400">Umumiy fan soati / 30</p>
          </div>
        </div>

        <div class="space-y-3">
          <div
            v-for="(row, index) in groupRows"
            :key="row.key"
            class="grid grid-cols-1 gap-4 sm:grid-cols-[1fr_10rem_auto]"
          >
            <div class="relative">
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Guruhlar</label>
              <input
                v-model="row.groupSearch"
                type="text"
                placeholder="Guruh nomi bo'yicha qidiring"
                class="form-field"
                @focus="openGroupDropdown(index)"
                @input="onGroupSearchInput(index)"
                @blur="closeGroupDropdown(index)"
              />
              <div
                v-if="row.dropdownOpen && filteredGroupsForRow(index).length"
                class="absolute z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-xl border border-gray-200 bg-white py-1 shadow-lg dark:border-gray-700 dark:bg-gray-900"
              >
                <button
                  v-for="group in filteredGroupsForRow(index)"
                  :key="group.id"
                  type="button"
                  class="flex w-full items-start justify-between gap-3 px-3 py-2 text-left hover:bg-gray-50 dark:hover:bg-gray-800"
                  @mousedown.prevent="selectGroup(index, group)"
                >
                  <span class="font-medium text-gray-800 dark:text-white/90">{{ group.name }}</span>
                  <span class="text-xs text-gray-500">{{ group.studentCount ?? 0 }} talaba</span>
                </button>
              </div>
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Talabalar soni</label>
              <input
                v-model.number="row.studentCount"
                type="number"
                min="0"
                class="form-field"
              />
            </div>
            <div class="flex items-end">
              <button
                v-if="groupRows.length > 1"
                type="button"
                class="mb-0.5 rounded-lg border border-rose-200 px-3 py-2 text-sm text-rose-600 hover:bg-rose-50 dark:border-rose-900 dark:hover:bg-rose-950/40"
                @click="removeGroupRow(index)"
              >
                O‘chirish
              </button>
            </div>
          </div>
          <p class="text-xs text-gray-500">
            Jami: {{ selectedGroupAssignments.length }} guruh,
            {{ totalAssignedStudents }} talaba
          </p>
          <div class="flex justify-center">
            <button
              type="button"
              class="rounded-lg border border-brand-300 bg-brand-50 px-4 py-2 text-sm font-medium text-brand-700 hover:bg-brand-100 dark:border-brand-800 dark:bg-brand-500/10 dark:text-brand-300"
              @click="addGroupRow"
            >
              Guruh qo‘shish
            </button>
          </div>
        </div>

        <div v-if="formError" class="text-sm text-error-600">{{ formError }}</div>
        <div v-if="!isFullyAllocated && form.totalSubjectHours > 0" class="text-sm text-warning-600">
          Umumiy fan soati to‘liq taqsimlanmagan (qolgan: {{ remainingNonRatingHours }}). Saqlash uchun
          barcha soatlarni taqsimlang.
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button
            type="button"
            class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
            @click="goBack"
          >
            Bekor
          </button>
          <button
            type="submit"
            class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="saving || !canSave"
          >
            Saqlash
          </button>
        </div>
      </form>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { academicYearApi, departmentApi, directionApi, groupApi, subjectApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { showError } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { Direction, NamedEntity, Subject } from '@/types/api'

type DepartmentItem = NamedEntity & { facultyId?: number }
type HourField =
  | 'lectureHours'
  | 'practicalHours'
  | 'labHours'
  | 'seminarHours'
  | 'independentStudyHours'
  | 'ratingHours'

interface AcademicYearItem {
  id: number
  name: string
}
interface DirectionItem extends Direction {}
interface GroupItem {
  id: number
  name: string
  studentCount?: number
  facultyId?: number
  departmentId?: number
}
interface GroupRow {
  key: number
  groupId: number | ''
  groupSearch: string
  studentCount: number
  dropdownOpen: boolean
}

let groupRowKey = 1
function emptyGroupRow(): GroupRow {
  return {
    key: groupRowKey++,
    groupId: '',
    groupSearch: '',
    studentCount: 0,
    dropdownOpen: false,
  }
}

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const emptyForm = () => ({
  code: '',
  name: '',
  semester: 'AUTUMN' as 'AUTUMN' | 'SPRING',
  academicYearId: '' as string | number,
  directionId: '' as string | number,
  courseYear: 1,
  educationType: 'KUNDUZGI' as 'KUNDUZGI' | 'KECHKI' | 'MASOFAVIY' | 'SIRTQI',
  educationLanguage: 'UZB' as 'UZB' | 'RUS',
  totalSubjectHours: 0,
  lectureHours: 0,
  practicalHours: 0,
  labHours: 0,
  seminarHours: 0,
  independentStudyHours: 0,
  ratingHours: 0,
  groupCount: 0,
  studentCount: 0,
})

const departments = ref<DepartmentItem[]>([])
const academicYears = ref<AcademicYearItem[]>([])
const directions = ref<DirectionItem[]>([])
const groups = ref<GroupItem[]>([])
const groupRows = ref<GroupRow[]>([emptyGroupRow()])
const formDepartmentId = ref('')
const directionSearch = ref('')
const directionDropdownOpen = ref(false)
const form = ref(emptyForm())
const pageLoading = ref(true)
const saving = ref(false)
const formError = ref('')
const hoursWarning = ref('')
const editingId = ref<number | null>(null)
const activeHourField = ref<HourField | null>(null)

const pageTitle = computed(() => (editingId.value ? 'Fanni tahrirlash' : 'Yangi fan'))

const filteredDirections = computed(() => {
  const q = directionSearch.value.trim().toLowerCase()
  if (!q) return directions.value.slice(0, 8)
  return directions.value
    .filter((item) => {
      const haystack = `${item.directionCode} ${item.directionName}`.toLowerCase()
      return haystack.includes(q)
    })
    .slice(0, 8)
})

const selectedGroupIds = computed(() =>
  groupRows.value.map((row) => row.groupId).filter((id): id is number => typeof id === 'number'),
)

const selectedGroupAssignments = computed(() =>
  groupRows.value
    .filter((row) => typeof row.groupId === 'number')
    .map((row) => ({
      groupId: row.groupId as number,
      studentCount: orZero(row.studentCount),
    })),
)

const totalAssignedStudents = computed(() =>
  selectedGroupAssignments.value.reduce((sum, row) => sum + row.studentCount, 0),
)

const allocatedHours = computed(() => {
  const f = form.value
  return (
    orZero(f.lectureHours) +
    orZero(f.practicalHours) +
    orZero(f.labHours) +
    orZero(f.seminarHours) +
    orZero(f.independentStudyHours)
  )
})

const remainingNonRatingHours = computed(() =>
  Math.max(0, orZero(form.value.totalSubjectHours) - allocatedHours.value),
)

const isFullyAllocated = computed(
  () =>
    orZero(form.value.totalSubjectHours) > 0 &&
    allocatedHours.value === orZero(form.value.totalSubjectHours),
)

const canSave = computed(
  () => isFullyAllocated.value && Boolean(formDepartmentId.value) && Boolean(form.value.directionId),
)

const computedAuditoriyHours = computed(() => {
  const f = form.value
  return (
    orZero(f.lectureHours) +
    orZero(f.practicalHours) +
    orZero(f.labHours) +
    orZero(f.seminarHours) +
    orZero(f.ratingHours)
  )
})

const computedOverallHours = computed(
  () => computedAuditoriyHours.value + orZero(form.value.independentStudyHours),
)

const computedCredit = computed(() => {
  const total = orZero(form.value.totalSubjectHours)
  return formatCredit(total > 0 ? total / 30 : 0)
})

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

function filteredGroupsForRow(index: number) {
  const row = groupRows.value[index]
  if (!row) return []
  const q = row.groupSearch.trim().toLowerCase()
  const taken = new Set(
    selectedGroupIds.value.filter((id) => id !== row.groupId),
  )
  return groups.value
    .filter((item) => !taken.has(item.id))
    .filter((item) => {
      if (!q) return true
      return item.name.toLowerCase().includes(q)
    })
    .slice(0, 8)
}

function sumExcept(field: HourField) {
  const f = form.value
  const tracked: Exclude<HourField, 'ratingHours'>[] = [
    'lectureHours',
    'practicalHours',
    'labHours',
    'seminarHours',
    'independentStudyHours',
  ]
  return tracked.reduce((sum, key) => (key === field ? sum : sum + orZero(f[key])), 0)
}

function availableHoursFor(field: HourField) {
  if (field === 'ratingHours') return 0
  const total = orZero(form.value.totalSubjectHours)
  return Math.max(0, total - sumExcept(field))
}

function hourInputTitle(field: HourField) {
  if (field === 'ratingHours') return "Reyting soati umumiy taqsimotga kirmaydi"
  return `Mavjud: ${availableHoursFor(field)} soat`
}

function onDirectionInput() {
  directionDropdownOpen.value = true
  const matched = directions.value.find((item) => {
    const haystack = `${item.directionCode} - ${item.directionName}`.toLowerCase()
    return haystack === directionSearch.value.trim().toLowerCase()
  })
  form.value.directionId = matched?.id ?? ''
}

function selectDirection(direction: DirectionItem) {
  form.value.directionId = direction.id
  directionSearch.value = `${direction.directionCode} - ${direction.directionName}`
  directionDropdownOpen.value = false
}

function closeDirectionDropdown() {
  window.setTimeout(() => {
    directionDropdownOpen.value = false
  }, 120)
}

function openGroupDropdown(index: number) {
  const row = groupRows.value[index]
  if (row) row.dropdownOpen = true
}

function onGroupSearchInput(index: number) {
  const row = groupRows.value[index]
  if (!row) return
  row.dropdownOpen = true
  const matched = groups.value.find(
    (item) => item.name.toLowerCase() === row.groupSearch.trim().toLowerCase(),
  )
  if (matched && !selectedGroupIds.value.includes(matched.id)) {
    row.groupId = matched.id
  } else if (!matched) {
    row.groupId = ''
  }
}

function selectGroup(index: number, group: GroupItem) {
  const row = groupRows.value[index]
  if (!row) return
  row.groupId = group.id
  row.groupSearch = group.name
  if (!row.studentCount) {
    row.studentCount = orZero(group.studentCount)
  }
  row.dropdownOpen = false
}

function closeGroupDropdown(index: number) {
  window.setTimeout(() => {
    const row = groupRows.value[index]
    if (row) row.dropdownOpen = false
  }, 120)
}

function addGroupRow() {
  groupRows.value.push(emptyGroupRow())
}

function removeGroupRow(index: number) {
  if (groupRows.value.length <= 1) return
  groupRows.value.splice(index, 1)
}

function onHourInput(field: HourField, event: Event) {
  const input = event.target as HTMLInputElement
  const raw = input.value
  const next = raw === '' ? 0 : Number(raw)
  const total = orZero(form.value.totalSubjectHours)

  if (!Number.isFinite(next) || next < 0) {
    input.value = String(form.value[field])
    return
  }

  if (total <= 0) {
    hoursWarning.value = 'Avval Umumiy fan soatini kiriting'
    form.value[field] = 0
    input.value = '0'
    return
  }

  if (field === 'ratingHours') {
    hoursWarning.value = ''
    form.value[field] = next
    return
  }

  const others = sumExcept(field)
  const maxAllowed = Math.max(0, total - others)

  if (next > maxAllowed) {
    hoursWarning.value = `Faqat ${maxAllowed} soat qoldi. ${next} soat kiritib bo'lmaydi.`
    form.value[field] = maxAllowed
    input.value = String(maxAllowed)
    return
  }

  hoursWarning.value = ''
  form.value[field] = next
}

function goBack() {
  router.push({ name: 'Subjects' })
}

async function loadGroups() {
  if (!formDepartmentId.value) {
    groups.value = []
    return
  }
  try {
    const dep = departments.value.find((d) => String(d.id) === formDepartmentId.value)
    const params = dep?.facultyId
      ? { facultyId: dep.facultyId }
      : { departmentId: Number(formDepartmentId.value) }
    const { data } = await groupApi.list(params)
    groups.value = unwrapList<GroupItem>(data)
  } catch {
    groups.value = []
  }
}

async function onDepartmentChange() {
  groupRows.value = [emptyGroupRow()]
  await loadGroups()
}

async function loadFilterOptions() {
  try {
    const [academicYearsRes, directionsRes] = await Promise.all([
      academicYearApi.list(),
      directionApi.list(),
    ])
    academicYears.value = unwrapList(academicYearsRes.data)
    directions.value = unwrapList(directionsRes.data)

    if (auth.hasFullAccess) {
      const depRes = await departmentApi.list()
      departments.value = unwrapList(depRes.data)
      return
    }

    if (auth.isDekan && auth.facultyId) {
      const depRes = await departmentApi.list({ facultyId: auth.facultyId })
      departments.value = unwrapList(depRes.data)
      return
    }

    if (auth.isKafedra) {
      departments.value = auth.user?.departmentId
        ? [
            {
              id: auth.user.departmentId,
              name: auth.user.departmentName || 'Kafedra',
              facultyId: auth.user.facultyId ?? undefined,
            },
          ]
        : []
      return
    }

    departments.value = []
  } catch {
    departments.value = []
    academicYears.value = []
    directions.value = []
  }
}

async function loadSubject(id: number) {
  const { data } = await subjectApi.getById(id)
  const item = data as Subject
  editingId.value = item.id
  formDepartmentId.value = item.departmentId ? String(item.departmentId) : ''
  form.value = {
    code: item.code,
    name: item.name,
    semester: item.semester === 'SPRING' ? 'SPRING' : 'AUTUMN',
    academicYearId: item.academicYearId ?? '',
    directionId: item.directionId ?? '',
    courseYear: item.courseYear && item.courseYear >= 1 && item.courseYear <= 5 ? item.courseYear : 1,
    educationType: item.educationType ?? 'KUNDUZGI',
    educationLanguage: item.educationLanguage ?? 'UZB',
    totalSubjectHours: item.totalSubjectHours ?? item.overallHours ?? 0,
    lectureHours: item.lectureHours ?? 0,
    practicalHours: item.practicalHours ?? 0,
    labHours: item.labHours ?? 0,
    seminarHours: item.seminarHours ?? 0,
    independentStudyHours: item.independentStudyHours ?? 0,
    ratingHours: item.ratingHours ?? 0,
    groupCount: item.groupCount ?? 0,
    studentCount: item.studentCount ?? 0,
  }
  directionSearch.value = item.directionId
    ? `${item.directionCode || ''} - ${item.directionName || ''}`.trim()
    : ''
  groupRows.value = [emptyGroupRow()]
  await loadGroups()
}

async function save() {
  if (!canSave.value) {
    formError.value = "Umumiy fan soati to'liq taqsimlanmagan yoki yo'nalish tanlanmagan"
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const assignments = selectedGroupAssignments.value
    const payload = {
      departmentId: Number(formDepartmentId.value),
      academicYearId: form.value.academicYearId ? Number(form.value.academicYearId) : null,
      directionId: form.value.directionId ? Number(form.value.directionId) : null,
      courseYear: orZero(form.value.courseYear) || 1,
      code: form.value.code.trim(),
      name: form.value.name.trim(),
      semester: form.value.semester,
      educationType: form.value.educationType,
      educationLanguage: form.value.educationLanguage,
      totalSubjectHours: orZero(form.value.totalSubjectHours),
      lectureHours: orZero(form.value.lectureHours),
      practicalHours: orZero(form.value.practicalHours),
      labHours: orZero(form.value.labHours),
      seminarHours: orZero(form.value.seminarHours),
      independentStudyHours: orZero(form.value.independentStudyHours),
      ratingHours: orZero(form.value.ratingHours),
      groupCount: assignments.length
        ? assignments.length
        : orZero(form.value.groupCount),
      studentCount: assignments.length
        ? assignments.reduce((sum, row) => sum + row.studentCount, 0)
        : orZero(form.value.studentCount),
      groups: assignments,
    }
    if (editingId.value) await subjectApi.update(editingId.value, payload)
    else await subjectApi.create(payload)
    goBack()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  pageLoading.value = true
  try {
    await loadFilterOptions()
    const idParam = route.params.id
    if (idParam) {
      const id = Number(idParam)
      if (!Number.isFinite(id) || id <= 0) {
        showError('Fan topilmadi')
        goBack()
        return
      }
      await loadSubject(id)
    } else {
      editingId.value = null
      form.value = emptyForm()
      formDepartmentId.value = auth.departmentId ? String(auth.departmentId) : ''
      directionSearch.value = ''
      groupRows.value = [emptyGroupRow()]
      await loadGroups()
    }
  } catch (e) {
    showError(getErrorMessage(e))
    goBack()
  } finally {
    pageLoading.value = false
  }
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
