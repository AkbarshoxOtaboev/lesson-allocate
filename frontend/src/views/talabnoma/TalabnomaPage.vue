<template>
  <AdminLayout>
    <div class="space-y-5">
      <!-- Hero -->
      <section
        class="relative overflow-hidden rounded-2xl bg-gradient-to-r from-indigo-600 via-indigo-500 to-blue-500 p-6 text-white shadow-lg sm:p-8"
      >
        <div class="relative z-10 flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p
              class="inline-flex rounded-full bg-white/15 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.14em]"
            >
              Kafedra talabnomalari tizimi
            </p>
            <h1 class="mt-3 text-2xl font-bold sm:text-3xl">Dars yuklamasi bo'yicha Talabnomalar</h1>
            <p class="mt-2 max-w-2xl text-sm text-white/85">
              Fakultet dekani boshqa kafedraga soat talabnomasini yuboradi. Kafedra qabul qilgach,
              soatlar fan yuklamasiga qo'shiladi va taqsimlash mumkin bo'ladi.
            </p>
          </div>
          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded-xl border border-white/30 bg-white/10 px-4 py-2.5 text-sm font-semibold backdrop-blur hover:bg-white/20"
              @click="exportCsv"
            >
              Yuklab olish (CSV)
            </button>
            <button
              v-if="canCreate"
              type="button"
              class="rounded-xl bg-orange-500 px-4 py-2.5 text-sm font-bold text-white shadow-lg shadow-orange-500/30 hover:bg-orange-600"
              @click="openCreate"
            >
              + Yangi talabnoma
            </button>
          </div>
        </div>
      </section>

      <!-- Stats -->
      <div class="grid grid-cols-2 gap-3 xl:grid-cols-4">
        <div
          v-for="card in statCards"
          :key="card.key"
          class="rounded-2xl border p-4 shadow-sm"
          :class="card.wrap"
        >
          <p class="text-3xl font-bold" :class="card.valueClass">{{ card.value }}</p>
          <p class="mt-1 text-sm font-medium text-slate-600 dark:text-slate-300">{{ card.label }}</p>
          <button
            type="button"
            class="mt-3 text-xs font-semibold text-indigo-600 hover:underline"
            @click="statusFilter = card.status"
          >
            Batafsil →
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div
        class="flex flex-col gap-3 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800 lg:flex-row"
      >
        <input
          v-model="search"
          type="search"
          placeholder="ID, Fan nomi, Kafedra yoki talabgor bo'yicha qidirish..."
          class="h-11 flex-1 rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
        />
        <select
          v-model="statusFilter"
          class="h-11 rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
        >
          <option :value="null">Barcha holatlar</option>
          <option value="NEW">Kutilmoqda</option>
          <option value="ACCEPTED">Taqsimlanmagan</option>
          <option value="PARTIAL">Qisman taqsimlangan</option>
          <option value="ALLOCATED">To'liq taqsimlangan</option>
          <option value="REJECTED">Rad etilgan</option>
        </select>
        <select
          v-if="auth.hasFullAccess"
          v-model="filterFacultyId"
          class="h-11 rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
        >
          <option :value="null">Barcha fakultetlar</option>
          <option v-for="f in faculties" :key="f.id" :value="f.id">{{ f.name }}</option>
        </select>
        <select
          v-if="auth.hasFullAccess || auth.isDekan"
          v-model="filterDepartmentId"
          class="h-11 rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
        >
          <option :value="null">Barcha kafedralar</option>
          <option v-for="d in filteredDepartments" :key="d.id" :value="d.id">{{ d.name }}</option>
        </select>
      </div>

      <!-- Table -->
      <div
        class="overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800"
      >
        <div class="border-b border-slate-100 px-5 py-4 dark:border-slate-700">
          <h3 class="font-bold text-slate-900 dark:text-white">Talabnomalar ro'yxati</h3>
        </div>
        <div v-if="loading" class="px-5 py-10 text-sm text-slate-500">Yuklanmoqda...</div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="border-b border-slate-100 bg-slate-50/80 text-left text-xs uppercase tracking-wide text-slate-500 dark:border-slate-700">
                <th class="px-4 py-3">Talabnoma ID / Sana</th>
                <th class="px-4 py-3">{{ auth.isKafedra ? 'Kelgan fakultet' : 'Kafedra / Fakultet' }}</th>
                <th class="px-4 py-3">Fan / Semestr</th>
                <th class="px-4 py-3">Talab soat</th>
                <th class="px-4 py-3">Talabgor</th>
                <th class="px-4 py-3">Holati / Taqsimot</th>
                <th class="px-4 py-3 text-right">Amallar</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-700">
              <tr v-for="item in filteredItems" :key="item.id">
                <td class="px-4 py-3">
                  <p class="font-semibold text-slate-800 dark:text-white">{{ item.code }}</p>
                  <p class="text-xs text-slate-500">{{ formatDate(item.createdAt) }}</p>
                </td>
                <td class="px-4 py-3">
                  <template v-if="auth.isKafedra">
                    <p class="font-medium text-slate-800 dark:text-white">
                      {{ item.fromFacultyName || '—' }}
                    </p>
                    <p class="text-xs text-slate-500">Qabul: {{ item.toDepartmentName }}</p>
                  </template>
                  <template v-else>
                    <p class="font-medium text-slate-800 dark:text-white">{{ item.toDepartmentName }}</p>
                    <p class="text-xs text-slate-500">
                      {{ item.toFacultyName || '—' }}
                      <span v-if="item.fromFacultyName"> · Yuborgan: {{ item.fromFacultyName }}</span>
                    </p>
                  </template>
                </td>
                <td class="px-4 py-3">
                  <p class="font-medium text-slate-800 dark:text-white">{{ item.subjectName }}</p>
                  <span
                    class="mt-1 inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold"
                    :class="
                      item.semester === 'SPRING'
                        ? 'bg-sky-50 text-sky-700'
                        : 'bg-orange-50 text-orange-700'
                    "
                  >
                    {{ item.semester === 'SPRING' ? 'Bahorgi semestr' : 'Kuzgi semestr' }}
                  </span>
                </td>
                <td class="px-4 py-3 font-semibold text-slate-800 dark:text-white">
                  {{ item.totalSubjectHours || item.totalHours || 0 }} soat
                  <span
                    v-if="item.requestStatus === 'ACCEPTED' || item.requestStatus === 'PARTIAL' || item.requestStatus === 'ALLOCATED'"
                    class="block text-xs font-normal text-slate-500"
                  >
                    Taqsimlangan: {{ item.allocatedHours || 0 }} /
                    {{ item.totalHours || item.totalSubjectHours || 0 }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <div class="flex items-center gap-2">
                    <span
                      class="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-100 text-xs font-bold text-indigo-700"
                    >
                      {{ initials(item.createdByName) }}
                    </span>
                    <span class="text-slate-700 dark:text-slate-200">{{ item.createdByName || '—' }}</span>
                  </div>
                </td>
                <td class="px-4 py-3">
                  <span class="rounded-full px-2.5 py-1 text-xs font-semibold" :class="statusClass(item.requestStatus)">
                    {{ statusLabel(item.requestStatus) }}
                  </span>
                  <div
                    v-if="item.allocatedTeachers?.length"
                    class="mt-2 space-y-0.5 rounded-lg bg-slate-50 px-2 py-1.5 text-xs text-slate-600 dark:bg-slate-900/60 dark:text-slate-300"
                  >
                    <p class="font-semibold text-slate-700 dark:text-slate-200">O'qituvchilar:</p>
                    <p v-for="t in item.allocatedTeachers" :key="t.teacherId">
                      {{ t.teacherName }} — {{ t.hours }} soat
                    </p>
                  </div>
                  <p
                    v-else-if="item.requestStatus === 'ACCEPTED'"
                    class="mt-1 text-xs text-slate-500"
                  >
                    Hali o'qituvchiga taqsimlanmagan
                  </p>
                </td>
                <td class="px-4 py-3">
                  <div class="flex justify-end gap-1">
                    <button
                      v-if="canAccept(item)"
                      type="button"
                      class="rounded-lg bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-700 hover:bg-emerald-100"
                      @click="accept(item)"
                    >
                      Qabul
                    </button>
                    <button
                      v-if="canAccept(item)"
                      type="button"
                      class="rounded-lg bg-rose-50 px-2.5 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-100"
                      @click="reject(item)"
                    >
                      Rad
                    </button>
                    <button
                      v-if="item.linkedSubjectId"
                      type="button"
                      class="rounded-lg bg-indigo-50 px-2.5 py-1.5 text-xs font-semibold text-indigo-700 hover:bg-indigo-100"
                      @click="goAllocate(item)"
                    >
                      Taqsimlash
                    </button>
                    <button
                      v-if="canDelete(item)"
                      type="button"
                      class="rounded-lg px-2.5 py-1.5 text-xs font-semibold text-slate-500 hover:bg-slate-100"
                      @click="remove(item)"
                    >
                      O'chirish
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="!filteredItems.length">
                <td colspan="7" class="px-5 py-10 text-center text-slate-500">Talabnoma yo'q</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div
          class="relative max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">Yangi talabnoma</h3>
          <form class="space-y-4" @submit.prevent="save">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Qabul qiluvchi kafedra</label>
              <select v-model.number="form.toDepartmentId" required class="filter-field">
                <option disabled :value="0">Tanlang</option>
                <option v-for="d in createDepartments" :key="d.id" :value="d.id">
                  {{ d.name }}{{ d.facultyName ? ` (${d.facultyName})` : '' }}
                </option>
              </select>
            </div>

            <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">O'quv yili</label>
                <select v-model.number="form.academicYearId" required class="filter-field">
                  <option disabled :value="0">Tanlang</option>
                  <option v-for="y in years" :key="y.id" :value="y.id">{{ y.name }}</option>
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

            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fan kodi</label>
                <input v-model="form.subjectCode" class="form-field" placeholder="Ixtiyoriy" />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fan nomi</label>
                <input v-model="form.subjectName" required class="form-field" />
              </div>
            </div>

            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Umumiy fan soati</label>
              <input
                v-model.number="form.totalSubjectHours"
                type="number"
                min="1"
                required
                placeholder="masalan: 120"
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
                <div v-for="field in hourFields" :key="field.key" class="relative">
                  <p
                    v-if="activeHourField === field.key"
                    class="absolute -top-2 right-0 rounded-md px-2 py-1 text-[11px] font-semibold shadow-sm ring-1"
                    :class="
                      field.key === 'ratingHours'
                        ? 'bg-sky-50 text-sky-600 ring-sky-100'
                        : 'bg-emerald-50 text-emerald-600 ring-emerald-100'
                    "
                  >
                    {{
                      field.key === 'ratingHours'
                        ? 'Reyting erkin'
                        : `Mavjud: ${availableHoursFor(field.key)}`
                    }}
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">{{ field.label }}</label>
                  <input
                    :value="form[field.key]"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle(field.key)"
                    @focus="activeHourField = field.key"
                    @mouseenter="activeHourField = field.key"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput(field.key, $event)"
                  />
                </div>
              </div>
              <p v-if="hoursWarning" class="mt-2 text-sm text-warning-600">{{ hoursWarning }}</p>
            </div>

            <div class="grid grid-cols-1 gap-3 rounded-xl bg-gray-50 p-4 sm:grid-cols-3 dark:bg-gray-800/50">
              <div>
                <p class="text-xs text-gray-500">Auditorik soat</p>
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
                <p class="text-xs text-gray-400">Auditorik + mustaqil ta'lim</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Kredit</p>
                <p class="text-lg font-semibold text-brand-600 dark:text-brand-400">
                  {{ computedCredit }}
                </p>
                <p class="text-xs text-gray-400">Umumiy fan soati / 30</p>
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

            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Izoh</label>
              <textarea
                v-model="form.note"
                rows="2"
                class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm dark:border-gray-700 dark:bg-gray-900"
              />
            </div>

            <div v-if="formError" class="text-sm text-error-600">{{ formError }}</div>
            <div v-if="!isFullyAllocated && form.totalSubjectHours > 0" class="text-sm text-warning-600">
              Umumiy fan soati to‘liq taqsimlanmagan (qolgan: {{ remainingNonRatingHours }}). Yuborish uchun
              barcha soatlarni taqsimlang.
            </div>

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
                class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="saving || !canSave"
              >
                Yuborish
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Modal from '@/components/ui/Modal.vue'
import { talabnomaApi } from '@/api/talabnoma'
import { academicYearApi, departmentApi, directionApi, facultyApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { confirmAction, showError } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { Direction, NamedEntity } from '@/types/api'
import type { Talabnoma, TalabnomaStats, TalabnomaStatus } from '@/types/talabnoma'

type HourField =
  | 'lectureHours'
  | 'practicalHours'
  | 'labHours'
  | 'seminarHours'
  | 'independentStudyHours'
  | 'ratingHours'

const auth = useAuthStore()
const router = useRouter()

const items = ref<Talabnoma[]>([])
const stats = ref<TalabnomaStats>({ total: 0, pending: 0, accepted: 0, rejected: 0, allocated: 0 })
const faculties = ref<NamedEntity[]>([])
const departments = ref<(NamedEntity & { facultyId?: number; facultyName?: string })[]>([])
const createDepartments = ref<(NamedEntity & { facultyId?: number; facultyName?: string })[]>([])
const years = ref<NamedEntity[]>([])
const directions = ref<Direction[]>([])
const loading = ref(false)
const saving = ref(false)
const modalOpen = ref(false)
const search = ref('')
const statusFilter = ref<TalabnomaStatus | null>(null)
const filterFacultyId = ref<number | null>(null)
const filterDepartmentId = ref<number | null>(null)
const directionSearch = ref('')
const directionDropdownOpen = ref(false)
const formError = ref('')
const hoursWarning = ref('')
const activeHourField = ref<HourField | null>(null)

const form = reactive({
  toDepartmentId: 0,
  subjectName: '',
  subjectCode: '',
  semester: 'AUTUMN' as 'AUTUMN' | 'SPRING',
  academicYearId: 0,
  directionId: 0 as number | '',
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
  note: '',
})

const hourFields: { key: HourField; label: string }[] = [
  { key: 'lectureHours', label: 'Maruza (soat)' },
  { key: 'practicalHours', label: 'Amaliy (soat)' },
  { key: 'labHours', label: 'Laboratoriya' },
  { key: 'seminarHours', label: 'Seminar' },
  { key: 'ratingHours', label: 'Reyting' },
  { key: 'independentStudyHours', label: "Mustaqil ta'lim" },
]

const canCreate = computed(() => auth.isDekan || auth.hasFullAccess)

const filteredDepartments = computed(() => {
  if (!filterFacultyId.value) return departments.value
  return departments.value.filter((d) => d.facultyId === filterFacultyId.value)
})

const filteredDirections = computed(() => {
  const q = directionSearch.value.trim().toLowerCase()
  if (!q) return directions.value.slice(0, 8)
  return directions.value
    .filter((item) => `${item.directionCode} ${item.directionName}`.toLowerCase().includes(q))
    .slice(0, 8)
})

const filteredItems = computed(() => {
  const q = search.value.trim().toLowerCase()
  return items.value.filter((item) => {
    if (statusFilter.value && item.requestStatus !== statusFilter.value) return false
    if (!q) return true
    return `${item.code} ${item.subjectName} ${item.toDepartmentName} ${item.createdByName}`
      .toLowerCase()
      .includes(q)
  })
})

const allocatedHours = computed(
  () =>
    orZero(form.lectureHours) +
    orZero(form.practicalHours) +
    orZero(form.labHours) +
    orZero(form.seminarHours) +
    orZero(form.independentStudyHours),
)

const remainingNonRatingHours = computed(() =>
  Math.max(0, orZero(form.totalSubjectHours) - allocatedHours.value),
)

const isFullyAllocated = computed(
  () => orZero(form.totalSubjectHours) > 0 && allocatedHours.value === orZero(form.totalSubjectHours),
)

const canSave = computed(
  () =>
    isFullyAllocated.value &&
    form.toDepartmentId > 0 &&
    Boolean(form.directionId) &&
    Boolean(form.subjectName.trim()) &&
    form.academicYearId > 0,
)

const computedAuditoriyHours = computed(
  () =>
    orZero(form.lectureHours) +
    orZero(form.practicalHours) +
    orZero(form.labHours) +
    orZero(form.seminarHours) +
    orZero(form.ratingHours),
)

const computedOverallHours = computed(
  () => computedAuditoriyHours.value + orZero(form.independentStudyHours),
)

const computedCredit = computed(() => {
  const total = orZero(form.totalSubjectHours)
  if (total <= 0) return '0'
  const credit = total / 30
  return Number.isInteger(credit) ? String(credit) : credit.toFixed(2)
})

const statCards = computed(() => [
  {
    key: 'total',
    label: 'Jami Talabnomalar',
    value: stats.value.total,
    status: null as TalabnomaStatus | null,
    wrap: 'border-blue-100 bg-blue-50/60 dark:border-blue-500/20 dark:bg-blue-500/10',
    valueClass: 'text-blue-700 dark:text-blue-300',
  },
  {
    key: 'pending',
    label: 'Kutilmoqda (Yangi)',
    value: stats.value.pending,
    status: 'NEW' as TalabnomaStatus,
    wrap: 'border-amber-100 bg-amber-50/60 dark:border-amber-500/20 dark:bg-amber-500/10',
    valueClass: 'text-amber-700 dark:text-amber-300',
  },
  {
    key: 'accepted',
    label: 'Taqsimlanmagan',
    value: stats.value.accepted,
    status: 'ACCEPTED' as TalabnomaStatus,
    wrap: 'border-emerald-100 bg-emerald-50/60 dark:border-emerald-500/20 dark:bg-emerald-500/10',
    valueClass: 'text-emerald-700 dark:text-emerald-300',
  },
  {
    key: 'rejected',
    label: 'Rad etilgan',
    value: stats.value.rejected,
    status: 'REJECTED' as TalabnomaStatus,
    wrap: 'border-rose-100 bg-rose-50/60 dark:border-rose-500/20 dark:bg-rose-500/10',
    valueClass: 'text-rose-700 dark:text-rose-300',
  },
])

function orZero(value: number | undefined | null) {
  return Number.isFinite(Number(value)) ? Number(value) : 0
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function statusLabel(s: TalabnomaStatus) {
  const map: Record<TalabnomaStatus, string> = {
    NEW: 'Kutilmoqda',
    ACCEPTED: 'Taqsimlanmagan',
    REJECTED: 'Rad etilgan',
    PARTIAL: 'Qisman taqsimlangan',
    ALLOCATED: "To'liq taqsimlangan",
  }
  return map[s] || s
}

function statusClass(s: TalabnomaStatus) {
  const map: Record<TalabnomaStatus, string> = {
    NEW: 'bg-amber-50 text-amber-700',
    ACCEPTED: 'bg-slate-100 text-slate-700',
    REJECTED: 'bg-rose-50 text-rose-700',
    PARTIAL: 'bg-sky-50 text-sky-700',
    ALLOCATED: 'bg-emerald-50 text-emerald-700',
  }
  return map[s]
}

function formatDate(v?: string) {
  if (!v) return '—'
  return new Date(v).toLocaleDateString('uz-UZ')
}

function initials(name?: string) {
  if (!name) return '?'
  return name.trim().slice(0, 1).toUpperCase()
}

function canAccept(item: Talabnoma) {
  if (item.requestStatus !== 'NEW') return false
  if (auth.hasFullAccess) return true
  return auth.isKafedra && auth.departmentId === item.toDepartmentId
}

function canDelete(item: Talabnoma) {
  if (item.requestStatus !== 'NEW' && item.requestStatus !== 'REJECTED') return false
  return auth.hasFullAccess || auth.isDekan
}

function goAllocate(item: Talabnoma) {
  router.push({ path: '/workloads', query: { subjectId: String(item.linkedSubjectId) } })
}

function sumExcept(field: HourField) {
  const tracked: Exclude<HourField, 'ratingHours'>[] = [
    'lectureHours',
    'practicalHours',
    'labHours',
    'seminarHours',
    'independentStudyHours',
  ]
  return tracked.reduce((sum, key) => (key === field ? sum : sum + orZero(form[key])), 0)
}

function availableHoursFor(field: HourField) {
  if (field === 'ratingHours') return 0
  return Math.max(0, orZero(form.totalSubjectHours) - sumExcept(field))
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
  form.directionId = matched?.id ?? ''
}

function selectDirection(direction: Direction) {
  form.directionId = direction.id
  directionSearch.value = `${direction.directionCode} - ${direction.directionName}`
  directionDropdownOpen.value = false
}

function closeDirectionDropdown() {
  window.setTimeout(() => {
    directionDropdownOpen.value = false
  }, 120)
}

function onHourInput(field: HourField, event: Event) {
  const input = event.target as HTMLInputElement
  const raw = input.value
  const next = raw === '' ? 0 : Number(raw)
  const total = orZero(form.totalSubjectHours)

  if (!Number.isFinite(next) || next < 0) {
    input.value = String(form[field])
    return
  }

  if (total <= 0) {
    hoursWarning.value = 'Avval Umumiy fan soatini kiriting'
    form[field] = 0
    input.value = '0'
    return
  }

  if (field === 'ratingHours') {
    hoursWarning.value = ''
    form[field] = next
    return
  }

  const maxAllowed = Math.max(0, total - sumExcept(field))
  if (next > maxAllowed) {
    hoursWarning.value = `Faqat ${maxAllowed} soat qoldi. ${next} soat kiritib bo'lmaydi.`
    form[field] = maxAllowed
    input.value = String(maxAllowed)
    return
  }

  hoursWarning.value = ''
  form[field] = next
}

async function load() {
  loading.value = true
  try {
    const params: Record<string, number> = { ...auth.catalogScopeParams() }
    if (auth.hasFullAccess) {
      if (filterFacultyId.value) params.facultyId = filterFacultyId.value
      if (filterDepartmentId.value) params.departmentId = filterDepartmentId.value
    } else if (auth.isDekan && filterDepartmentId.value) {
      params.departmentId = filterDepartmentId.value
    }
    const [listRes, statsRes] = await Promise.all([
      talabnomaApi.list(params),
      talabnomaApi.stats(params),
    ])
    items.value = unwrapList(listRes.data)
    stats.value = statsRes.data
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

async function loadLookups() {
  // Kafedra FACULTY_VIEW yo'q — faqat o'ziga kerakli endpointlar chaqiriladi
  const tasks: Promise<void>[] = []

  if (auth.hasFullAccess) {
    tasks.push(
      facultyApi.list().then((f) => {
        faculties.value = unwrapList(f.data)
      }),
    )
  }

  if (auth.hasFullAccess || auth.isDekan) {
    tasks.push(
      departmentApi.list().then((d) => {
        departments.value = unwrapList(d.data) as typeof departments.value
      }),
    )
  }

  if (canCreate.value) {
    tasks.push(
      academicYearApi.list().then((y) => {
        years.value = unwrapList(y.data)
      }),
      directionApi.list().then((dir) => {
        directions.value = unwrapList(dir.data)
      }),
      departmentApi.listForTalabnoma().then((createDeps) => {
        createDepartments.value = unwrapList(createDeps.data) as typeof createDepartments.value
      }),
    )
  }

  await Promise.all(tasks)
}

function openCreate() {
  form.toDepartmentId = 0
  form.subjectName = ''
  form.subjectCode = ''
  form.semester = 'AUTUMN'
  form.academicYearId = years.value[0]?.id ?? 0
  form.directionId = ''
  form.educationType = 'KUNDUZGI'
  form.educationLanguage = 'UZB'
  form.totalSubjectHours = 0
  form.lectureHours = 0
  form.practicalHours = 0
  form.labHours = 0
  form.seminarHours = 0
  form.independentStudyHours = 0
  form.ratingHours = 0
  form.groupCount = 0
  form.studentCount = 0
  form.note = ''
  directionSearch.value = ''
  formError.value = ''
  hoursWarning.value = ''
  modalOpen.value = true
}

async function save() {
  if (!canSave.value) return
  saving.value = true
  formError.value = ''
  try {
    await talabnomaApi.create({
      toDepartmentId: form.toDepartmentId,
      subjectName: form.subjectName,
      subjectCode: form.subjectCode || undefined,
      semester: form.semester,
      academicYearId: form.academicYearId || undefined,
      directionId: Number(form.directionId) || undefined,
      educationType: form.educationType,
      educationLanguage: form.educationLanguage,
      totalSubjectHours: form.totalSubjectHours,
      lectureHours: form.lectureHours,
      practicalHours: form.practicalHours,
      labHours: form.labHours,
      seminarHours: form.seminarHours,
      independentStudyHours: form.independentStudyHours,
      ratingHours: form.ratingHours,
      groupCount: form.groupCount,
      studentCount: form.studentCount,
      note: form.note || undefined,
    })
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function accept(item: Talabnoma) {
  try {
    await talabnomaApi.accept(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

async function reject(item: Talabnoma) {
  try {
    await talabnomaApi.reject(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

async function remove(item: Talabnoma) {
  const ok = await confirmAction(`"${item.code}" o'chirilsinmi?`, "O'chirish")
  if (!ok) return
  try {
    await talabnomaApi.remove(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

function exportCsv() {
  const lines = filteredItems.value.map((i) =>
    [i.code, i.toDepartmentName, i.subjectName, i.totalSubjectHours || i.totalHours, i.requestStatus, i.createdByName].join(';'),
  )
  const blob = new Blob([['Kod;Kafedra;Fan;Soat;Holat;Talabgor', ...lines].join('\n')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'talabnomalar.csv'
  a.click()
  URL.revokeObjectURL(url)
}

watch([filterFacultyId, filterDepartmentId], () => {
  void load()
})

onMounted(async () => {
  try {
    await loadLookups()
  } catch (e) {
    // Lookup xatosi ro'yxatni bloklamasin
    console.warn(getErrorMessage(e))
  }
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
