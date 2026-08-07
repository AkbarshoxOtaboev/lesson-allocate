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
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="rounded-lg bg-teal-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-teal-700"
            @click="exportCsv"
          >
            Excelga yuklash
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

      <div class="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <div class="flex flex-wrap gap-4">
          <div
            class="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-1 dark:border-gray-700 dark:bg-gray-800/60"
            role="tablist"
            aria-label="Semestr filtri"
          >
            <button
              v-for="opt in semesterFilterOptions"
              :key="opt.value || 'all'"
              type="button"
              role="tab"
              class="rounded-md px-3 py-1.5 text-sm font-medium transition"
              :class="
                selectedSemester === opt.value
                  ? 'bg-white text-brand-600 shadow-sm dark:bg-gray-900 dark:text-brand-400'
                  : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'
              "
              @click="onSemesterFilterChange(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
          <div
            class="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-1 dark:border-gray-700 dark:bg-gray-800/60"
            role="tablist"
            aria-label="Kurs filtri"
          >
            <button
              v-for="opt in courseFilterOptions"
              :key="opt.value || 'all'"
              type="button"
              role="tab"
              class="rounded-md px-3 py-1.5 text-sm font-medium transition"
              :class="
                selectedCourseYear === opt.value
                  ? 'bg-white text-brand-600 shadow-sm dark:bg-gray-900 dark:text-brand-400'
                  : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'
              "
              @click="onCourseFilterChange(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
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
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan kodi</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan nomi</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kafedra</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kelgan fakultet</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">O'quv yili</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Semestr</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kurs</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan soati</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kredit</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Auditoriya soatlari</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Maruza</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Amaliy</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Lab</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Seminar</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Mustaqil</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Reyting</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Umumiy soat</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Guruhlar</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Talabalar</th>
              <th class="px-3 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(item, index) in displayedItems" :key="item.id">
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.code }}</td>
              <td class="px-3 py-4 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                <button
                  type="button"
                  class="text-left text-brand-600 hover:underline dark:text-brand-400"
                  @click="openDetail(item)"
                >
                  {{ item.name }}
                </button>
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.departmentName || '—' }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">
                <span v-if="item.sourceFacultyName" class="font-medium text-indigo-700 dark:text-indigo-300">
                  {{ item.sourceFacultyName }}
                </span>
                <span v-else>—</span>
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.academicYearName || '—' }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ semesterLabel(item.semester) }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">
                {{ item.courseYear ? `${item.courseYear}-kurs` : '—' }}
              </td>
              <td class="px-3 py-4 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ item.totalSubjectHours ?? 0 }}
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ formatCredit(item.credit) }}</td>
              <td class="px-3 py-4 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ subjectAuditoriumHours(item) }}
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.lectureHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.practicalHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.labHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.seminarHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.independentStudyHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.ratingHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ subjectTotalHours(item) }}
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.groupCount ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.studentCount ?? 0 }}</td>
              <td class="px-3 py-4">
                <div class="relative flex justify-end">
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-gray-200 bg-white text-gray-600 shadow-sm transition hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-300 dark:hover:bg-gray-800"
                    title="Amallar"
                    @click.stop="toggleActionsMenu(item.id)"
                  >
                    <SlidersHorizontal class="size-4" :stroke-width="2" />
                  </button>
                  <div
                    v-if="openActionsId === item.id"
                    class="absolute right-0 top-full z-50 mt-1.5 w-44 rounded-xl border border-gray-100 bg-white p-1.5 shadow-lg dark:border-gray-700 dark:bg-gray-900"
                    @click.stop
                  >
                    <button
                      type="button"
                      class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-indigo-700 transition hover:bg-indigo-50 dark:text-indigo-300 dark:hover:bg-indigo-500/10"
                      @click="onViewAction(item)"
                    >
                      <Eye class="size-4 shrink-0" :stroke-width="2" />
                      Ko'rish
                    </button>
                    <button
                      type="button"
                      class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-teal-600 transition hover:bg-teal-50 dark:text-teal-400 dark:hover:bg-teal-500/10"
                      @click="onEditAction(item)"
                    >
                      <Pencil class="size-4 shrink-0" :stroke-width="2" />
                      Tahrirlash
                    </button>
                    <button
                      type="button"
                      class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-rose-600 transition hover:bg-rose-50 dark:text-rose-400 dark:hover:bg-rose-500/10"
                      @click="onDeleteAction(item)"
                    >
                      <Trash2 class="size-4 shrink-0" :stroke-width="2" />
                      O'chirish
                    </button>
                  </div>
                </div>
              </td>
            </tr>
            <tr v-if="displayedItems.length" class="bg-slate-50/80 font-semibold dark:bg-slate-900/40">
              <td colspan="8" class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">Jami</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.totalSubjectHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ formatCredit(subjectTotals.credit) }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.auditoriumHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.lectureHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.practicalHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.labHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.seminarHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.independentStudyHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.ratingHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.totalHours }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.groupCount }}</td>
              <td class="px-3 py-4 text-theme-sm text-slate-700 dark:text-slate-200">{{ subjectTotals.studentCount }}</td>
              <td class="px-3 py-4"></td>
            </tr>
            <tr v-if="!displayedItems.length">
              <td colspan="21" class="px-5 py-8 text-center text-sm text-gray-500">Bo‘sh</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal v-if="detailOpen && detailItem" full-screen-backdrop @close="detailOpen = false">
      <template #body>
        <div
          class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <div class="mb-4 flex items-start justify-between gap-3">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">Fan va Yuklama</h3>
            <button
              type="button"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800"
              @click="detailOpen = false"
            >
              ×
            </button>
          </div>

          <div class="rounded-xl bg-gray-50 p-4 dark:bg-gray-800/60">
            <p class="text-base font-semibold text-gray-800 dark:text-white/90">
              {{ detailItem.name }}
            </p>
            <div class="mt-3 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
              <div>
                <span class="text-gray-500">Kafedra:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ detailItem.departmentName || '—' }}
                </span>
              </div>
              <div v-if="detailItem.sourceFacultyName">
                <span class="text-gray-500">Kelgan fakultet:</span>
                <span class="ml-1 font-medium text-indigo-700 dark:text-indigo-300">
                  {{ detailItem.sourceFacultyName }}
                </span>
                <span v-if="detailItem.talabnomaCode" class="ml-1 text-xs text-gray-400">
                  ({{ detailItem.talabnomaCode }})
                </span>
              </div>
              <div>
                <span class="text-gray-500">Semestr:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ semesterLabel(detailItem.semester) }} semestr
                </span>
              </div>
              <div>
                <span class="text-gray-500">Yo'nalish:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ detailItem.directionName || '—' }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Kurs:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ detailItem.courseYear ? `${detailItem.courseYear}-kurs` : '—' }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Ta'lim:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ educationTypeLabel(detailItem.educationType) }} /
                  {{ educationLanguageLabel(detailItem.educationLanguage) }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Kredit:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ formatCredit(detailItem.credit) }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Umumiy soat:</span>
                <span class="ml-1 font-bold text-emerald-600">
                  {{ detailItem.totalSubjectHours ?? 0 }}
                </span>
              </div>
            </div>
          </div>

          <div class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Ma'ruza</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.lectureHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Amaliy</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.practicalHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Laboratoriya</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.labHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Reyting</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.ratingHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Seminar</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.seminarHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Mustaqil t.</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.independentStudyHours ?? 0 }}
              </p>
            </div>
          </div>

          <div class="mt-3 rounded-xl border border-gray-200 px-4 py-3 dark:border-gray-700">
            <p class="text-xs text-gray-500">Guruhlar/Talaba</p>
            <p class="mt-1 text-lg font-semibold text-gray-800 dark:text-white/90">
              {{ detailItem.groupCount ?? 0 }} / {{ detailItem.studentCount ?? 0 }}
            </p>
          </div>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import { departmentApi, facultyApi, subjectApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { confirmAction, showError } from '@/utils/swal'
import { Eye, Pencil, SlidersHorizontal, Trash2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import type { NamedEntity, Subject } from '@/types/api'

type DepartmentItem = NamedEntity & { facultyId?: number }

const semesterFilterOptions = [
  { value: '', label: 'Barchasi' },
  { value: 'AUTUMN', label: 'Kuzgi' },
  { value: 'SPRING', label: 'Bahorgi' },
] as const

const courseFilterOptions = [
  { value: '', label: 'Barchasi' },
  { value: '1', label: '1-kurs' },
  { value: '2', label: '2-kurs' },
  { value: '3', label: '3-kurs' },
  { value: '4', label: '4-kurs' },
  { value: '5', label: '5-kurs' },
] as const

const auth = useAuthStore()
const router = useRouter()

const items = ref<Subject[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<DepartmentItem[]>([])
const selectedFacultyId = ref('')
const selectedDepartmentId = ref('')
const selectedSemester = ref('')
const selectedCourseYear = ref('')
const nameSearch = ref('')
const loading = ref(false)
const error = ref('')
const detailOpen = ref(false)
const openActionsId = ref<number | null>(null)
const detailItem = ref<Subject | null>(null)

function toggleActionsMenu(id: number) {
  openActionsId.value = openActionsId.value === id ? null : id
}

function closeActionsMenu() {
  openActionsId.value = null
}

function onViewAction(item: Subject) {
  closeActionsMenu()
  openDetail(item)
}

function onEditAction(item: Subject) {
  closeActionsMenu()
  openEdit(item)
}

function onDeleteAction(item: Subject) {
  closeActionsMenu()
  void removeItem(item)
}

function onDocumentClick() {
  closeActionsMenu()
}

const filteredDepartments = computed(() => {
  if (!selectedFacultyId.value) return departments.value
  const facultyId = Number(selectedFacultyId.value)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const displayedItems = computed(() => {
  const q = nameSearch.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((item) => {
    const haystack = `${item.code} ${item.name}`.toLowerCase()
    return haystack.includes(q)
  })
})

function subjectAuditoriumHours(item: {
  auditoriumHours?: number | null
  lectureHours?: number | null
  practicalHours?: number | null
  labHours?: number | null
  seminarHours?: number | null
  ratingHours?: number | null
}) {
  if (item.auditoriumHours != null && item.auditoriumHours > 0) return item.auditoriumHours
  return (
    (item.lectureHours ?? 0) +
    (item.practicalHours ?? 0) +
    (item.labHours ?? 0) +
    (item.seminarHours ?? 0) +
    (item.ratingHours ?? 0)
  )
}

function subjectTotalHours(item: {
  totalHours?: number | null
  overallHours?: number | null
  independentStudyHours?: number | null
  auditoriumHours?: number | null
  lectureHours?: number | null
  practicalHours?: number | null
  labHours?: number | null
  seminarHours?: number | null
  ratingHours?: number | null
}) {
  if (item.totalHours != null && item.totalHours > 0) return item.totalHours
  if (item.overallHours != null && item.overallHours > 0) return item.overallHours
  return subjectAuditoriumHours(item) + (item.independentStudyHours ?? 0)
}

const subjectTotals = computed(() =>
  displayedItems.value.reduce(
    (acc, item) => {
      acc.totalSubjectHours += item.totalSubjectHours ?? 0
      acc.credit += item.credit ?? 0
      acc.auditoriumHours += subjectAuditoriumHours(item)
      acc.lectureHours += item.lectureHours ?? 0
      acc.practicalHours += item.practicalHours ?? 0
      acc.labHours += item.labHours ?? 0
      acc.seminarHours += item.seminarHours ?? 0
      acc.independentStudyHours += item.independentStudyHours ?? 0
      acc.ratingHours += item.ratingHours ?? 0
      acc.totalHours += subjectTotalHours(item)
      acc.groupCount += item.groupCount ?? 0
      acc.studentCount += item.studentCount ?? 0
      return acc
    },
    {
      totalSubjectHours: 0,
      credit: 0,
      auditoriumHours: 0,
      lectureHours: 0,
      practicalHours: 0,
      labHours: 0,
      seminarHours: 0,
      independentStudyHours: 0,
      ratingHours: 0,
      totalHours: 0,
      groupCount: 0,
      studentCount: 0,
    },
  ),
)

function formatCredit(value: number | undefined | null) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '0'
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

function semesterLabel(semester?: 'AUTUMN' | 'SPRING' | null) {
  if (semester === 'SPRING') return 'Bahorgi'
  if (semester === 'AUTUMN') return 'Kuzgi'
  return '—'
}

function educationTypeLabel(value?: Subject['educationType'] | null) {
  switch (value) {
    case 'KUNDUZGI':
      return 'Kunduzgi'
    case 'KECHKI':
      return 'Kechki'
    case 'MASOFAVIY':
      return 'Masofaviy'
    case 'SIRTQI':
      return 'Sirtqi'
    default:
      return '—'
  }
}

function educationLanguageLabel(value?: Subject['educationLanguage'] | null) {
  switch (value) {
    case 'UZB':
      return 'Uzb'
    case 'RUS':
      return 'Rus'
    default:
      return '—'
  }
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function listParams() {
  const params: Record<string, string | number> = {}
  if (selectedDepartmentId.value) params.departmentId = Number(selectedDepartmentId.value)
  else if (selectedFacultyId.value) params.facultyId = Number(selectedFacultyId.value)
  if (selectedSemester.value) params.semester = selectedSemester.value
  if (selectedCourseYear.value) params.courseYear = Number(selectedCourseYear.value)
  return Object.keys(params).length ? params : undefined
}

async function loadFilterOptions() {
  try {
    if (auth.hasFullAccess) {
      const [facRes, depRes] = await Promise.all([facultyApi.list(), departmentApi.list()])
      faculties.value = unwrapList(facRes.data)
      departments.value = unwrapList(depRes.data)
      return
    }

    if (auth.isDekan && auth.facultyId) {
      faculties.value = auth.user?.facultyId
        ? [{ id: auth.user.facultyId, name: auth.user.facultyName || 'Fakultet' }]
        : []
      const depRes = await departmentApi.list({ facultyId: auth.facultyId })
      departments.value = unwrapList(depRes.data)
      return
    }

    if (auth.isKafedra) {
      faculties.value = auth.user?.facultyId
        ? [{ id: auth.user.facultyId, name: auth.user.facultyName || 'Fakultet' }]
        : []
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

    faculties.value = []
    departments.value = []
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
    items.value = unwrapList<Subject>(data)
  } catch (e) {
    showError(getErrorMessage(e))
    items.value = []
  } finally {
    loading.value = false
  }
}

function onFacultyFilterChange() {
  selectedDepartmentId.value = ''
  load()
}

function onSemesterFilterChange(value: string) {
  selectedSemester.value = value
  load()
}

function onCourseFilterChange(value: string) {
  selectedCourseYear.value = value
  load()
}

function openDetail(item: Subject) {
  detailItem.value = item
  detailOpen.value = true
}

function openCreate() {
  router.push({ name: 'SubjectCreate' })
}

function openEdit(item: Subject) {
  router.push({ name: 'SubjectEdit', params: { id: String(item.id) } })
}

async function removeItem(item: Subject) {
  const ok = await confirmAction(`"${item.name}" o‘chirilsinmi?`, 'O‘chirish')
  if (!ok) return
  try {
    await subjectApi.remove(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

function exportCsv() {
  const header = [
    '#',
    'Fan kodi',
    'Fan nomi',
    'Kafedra',
    'Kelgan fakultet',
    "O'quv yili",
    'Semestr',
    'Kurs',
    'Fan soati',
    'Kredit',
    'Auditoriya soatlari',
    "Ma'ruza",
    'Amaliy',
    'Lab',
    'Seminar',
    'Mustaqil',
    'Reyting',
    'Umumiy soat',
    'Guruhlar',
    'Talabalar',
  ]
  const lines = displayedItems.value.map((item, index) =>
    [
      index + 1,
      item.code,
      item.name,
      item.departmentName || '',
      item.sourceFacultyName || '',
      item.academicYearName || '',
      semesterLabel(item.semester),
      item.courseYear ? `${item.courseYear}-kurs` : '',
      item.totalSubjectHours ?? 0,
      formatCredit(item.credit),
      subjectAuditoriumHours(item),
      item.lectureHours ?? 0,
      item.practicalHours ?? 0,
      item.labHours ?? 0,
      item.seminarHours ?? 0,
      item.independentStudyHours ?? 0,
      item.ratingHours ?? 0,
      subjectTotalHours(item),
      item.groupCount ?? 0,
      item.studentCount ?? 0,
    ].join(';'),
  )
  lines.push(
    [
      '',
      '',
      'Jami',
      '',
      '',
      '',
      '',
      '',
      subjectTotals.value.totalSubjectHours,
      formatCredit(subjectTotals.value.credit),
      subjectTotals.value.auditoriumHours,
      subjectTotals.value.lectureHours,
      subjectTotals.value.practicalHours,
      subjectTotals.value.labHours,
      subjectTotals.value.seminarHours,
      subjectTotals.value.independentStudyHours,
      subjectTotals.value.ratingHours,
      subjectTotals.value.totalHours,
      subjectTotals.value.groupCount,
      subjectTotals.value.studentCount,
    ].join(';'),
  )
  const blob = new Blob([[header.join(';'), ...lines].join('\n')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'fanlar.csv'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  document.addEventListener('click', onDocumentClick)
  if (auth.isKafedra) {
    selectedFacultyId.value = auth.facultyId ? String(auth.facultyId) : ''
    selectedDepartmentId.value = auth.departmentId ? String(auth.departmentId) : ''
  } else if (auth.isDekan) {
    selectedFacultyId.value = auth.facultyId ? String(auth.facultyId) : ''
  }
  await loadFilterOptions()
  await load()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocumentClick)
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
