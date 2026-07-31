<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Dars rejasi" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 px-5 py-4 dark:border-gray-800"
      >
        <div>
          <h3 class="font-semibold text-gray-800 dark:text-white/90">Dars rejasi</h3>
          <p class="mt-0.5 text-sm text-gray-500">
            Ta'lim yo'nalishi, shakli, kurs va semestr bo'yicha o'quv reja
          </p>
        </div>
        <button
          type="button"
          class="rounded-lg bg-teal-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-teal-700"
          @click="exportExcel"
        >
          Excelga yuklash
        </button>
      </div>

      <div
        class="grid grid-cols-1 gap-3 border-b border-gray-200 px-5 py-4 sm:grid-cols-2 lg:grid-cols-4 dark:border-gray-800"
      >
        <div>
          <label class="mb-1 block text-xs text-gray-500">Ta'lim yo'nalishi</label>
          <select v-model="selectedDirectionId" class="filter-field" @change="load">
            <option value="">Barchasi</option>
            <option v-for="d in directions" :key="d.id" :value="String(d.id)">
              {{ d.directionCode }} — {{ d.directionName || d.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Ta'lim shakli</label>
          <select v-model="selectedEducationType" class="filter-field" @change="load">
            <option value="">Barchasi</option>
            <option value="KUNDUZGI">Kunduzgi</option>
            <option value="KECHKI">Kechki</option>
            <option value="SIRTQI">Sirtqi</option>
            <option value="MASOFAVIY">Masofaviy</option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Kurs</label>
          <select v-model="selectedCourseYear" class="filter-field" @change="load">
            <option value="">Barchasi</option>
            <option v-for="y in [1, 2, 3, 4, 5]" :key="y" :value="String(y)">{{ y }}-kurs</option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Semestr</label>
          <select v-model="selectedSemester" class="filter-field" @change="load">
            <option value="">Barchasi</option>
            <option value="AUTUMN">Kuzgi</option>
            <option value="SPRING">Bahorgi</option>
          </select>
        </div>
      </div>

      <div
        v-if="planHeader"
        class="border-b border-gray-200 bg-slate-50/80 px-5 py-4 text-sm dark:border-gray-800 dark:bg-slate-900/40"
      >
        <div class="grid gap-1 sm:grid-cols-2 lg:grid-cols-4">
          <p>
            <span class="text-gray-500">Ta'lim yo'nalishi:</span>
            <span class="ml-1 font-semibold text-gray-800 dark:text-white/90">
              {{ planHeader.direction }}
            </span>
          </p>
          <p>
            <span class="text-gray-500">Ta'lim shakli:</span>
            <span class="ml-1 font-semibold text-gray-800 dark:text-white/90">
              {{ planHeader.educationType }}
            </span>
          </p>
          <p>
            <span class="text-gray-500">Kurs:</span>
            <span class="ml-1 font-semibold text-gray-800 dark:text-white/90">
              {{ planHeader.course }}
            </span>
          </p>
          <p>
            <span class="text-gray-500">Semestr:</span>
            <span class="ml-1 font-semibold text-gray-800 dark:text-white/90">
              {{ planHeader.semester }}
            </span>
          </p>
        </div>
      </div>

      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>
      <div v-else class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200 bg-slate-50/80 dark:border-gray-700 dark:bg-slate-900/40">
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">№</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Fan malakaviy kodi</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">
                O'qitiladigan fanlar
              </th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Jami soat</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Ma'ruza</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Amaliy</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Laboratoriya</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Seminar</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Haftalik soat</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Mustaqil ish</th>
              <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Kredit</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
            <tr v-for="(item, index) in items" :key="item.id">
              <td class="px-3 py-3 text-gray-500">{{ index + 1 }}</td>
              <td class="px-3 py-3 font-medium text-gray-800 dark:text-white/90">{{ item.code }}</td>
              <td class="px-3 py-3 text-gray-800 dark:text-white/90">{{ item.name }}</td>
              <td class="px-3 py-3 font-semibold">{{ item.totalSubjectHours ?? 0 }}</td>
              <td class="px-3 py-3">{{ item.lectureHours ?? 0 }}</td>
              <td class="px-3 py-3">{{ item.practicalHours ?? 0 }}</td>
              <td class="px-3 py-3">{{ item.labHours ?? 0 }}</td>
              <td class="px-3 py-3">{{ item.seminarHours ?? 0 }}</td>
              <td class="px-3 py-3">{{ weeklyHours(item) }}</td>
              <td class="px-3 py-3">{{ item.independentStudyHours ?? 0 }}</td>
              <td class="px-3 py-3 font-semibold">{{ formatCredit(item.credit) }}</td>
            </tr>
            <tr v-if="items.length" class="bg-slate-50/80 font-semibold dark:bg-slate-900/40">
              <td colspan="3" class="px-3 py-3">JAMI</td>
              <td class="px-3 py-3">{{ totals.totalSubjectHours }}</td>
              <td class="px-3 py-3">{{ totals.lectureHours }}</td>
              <td class="px-3 py-3">{{ totals.practicalHours }}</td>
              <td class="px-3 py-3">{{ totals.labHours }}</td>
              <td class="px-3 py-3">{{ totals.seminarHours }}</td>
              <td class="px-3 py-3">{{ Math.round(totals.weeklyHours * 10) / 10 }}</td>
              <td class="px-3 py-3">{{ totals.independentStudyHours }}</td>
              <td class="px-3 py-3">{{ formatCredit(totals.credit) }}</td>
            </tr>
            <tr v-if="!items.length">
              <td colspan="11" class="px-5 py-10 text-center text-gray-500">
                Tanlangan filtr bo'yicha fanlar topilmadi
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { directionApi, subjectApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { showError } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { Direction, Subject } from '@/types/api'

const WEEKS_PER_SEMESTER = 15

const auth = useAuthStore()
const loading = ref(false)
const items = ref<Subject[]>([])
const directions = ref<Direction[]>([])
const selectedDirectionId = ref('')
const selectedEducationType = ref('')
const selectedCourseYear = ref('')
const selectedSemester = ref('')

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function educationTypeLabel(v?: string) {
  switch (v) {
    case 'KUNDUZGI':
      return 'Kunduzgi'
    case 'KECHKI':
      return 'Kechki'
    case 'SIRTQI':
      return 'Sirtqi'
    case 'MASOFAVIY':
      return 'Masofaviy'
    default:
      return '—'
  }
}

function semesterLabel(v?: string) {
  if (v === 'SPRING') return 'Bahorgi'
  if (v === 'AUTUMN') return 'Kuzgi'
  return '—'
}

function formatCredit(v?: number | null) {
  if (v == null || Number.isNaN(Number(v))) return '0'
  const n = Number(v)
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

function contactHours(item: Subject) {
  return (
    (item.lectureHours ?? 0) +
    (item.practicalHours ?? 0) +
    (item.labHours ?? 0) +
    (item.seminarHours ?? 0)
  )
}

function weeklyHours(item: Subject) {
  return Math.round((contactHours(item) / WEEKS_PER_SEMESTER) * 10) / 10
}

const planHeader = computed(() => {
  const direction = directions.value.find((d) => String(d.id) === selectedDirectionId.value)
  return {
    direction: direction
      ? `${direction.directionCode} — ${direction.directionName || direction.name}`
      : selectedDirectionId.value
        ? '—'
        : 'Barchasi',
    educationType: selectedEducationType.value
      ? educationTypeLabel(selectedEducationType.value)
      : 'Barchasi',
    course: selectedCourseYear.value ? `${selectedCourseYear.value}-kurs` : 'Barchasi',
    semester: selectedSemester.value ? semesterLabel(selectedSemester.value) : 'Barchasi',
  }
})

const totals = computed(() => {
  return items.value.reduce(
    (acc, item) => {
      acc.totalSubjectHours += item.totalSubjectHours ?? 0
      acc.lectureHours += item.lectureHours ?? 0
      acc.practicalHours += item.practicalHours ?? 0
      acc.labHours += item.labHours ?? 0
      acc.seminarHours += item.seminarHours ?? 0
      acc.independentStudyHours += item.independentStudyHours ?? 0
      acc.credit += Number(item.credit ?? 0)
      acc.weeklyHours += weeklyHours(item)
      return acc
    },
    {
      totalSubjectHours: 0,
      lectureHours: 0,
      practicalHours: 0,
      labHours: 0,
      seminarHours: 0,
      independentStudyHours: 0,
      credit: 0,
      weeklyHours: 0,
    },
  )
})

async function loadDirections() {
  try {
    const { data } = await directionApi.list()
    directions.value = unwrapList(data)
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

async function load() {
  loading.value = true
  try {
    const scope = auth.catalogScopeParams()
    const params: Record<string, string | number | undefined> = {
      ...(scope.facultyId ? { facultyId: scope.facultyId } : {}),
      ...(scope.departmentId ? { departmentId: scope.departmentId } : {}),
      ...(selectedDirectionId.value ? { directionId: Number(selectedDirectionId.value) } : {}),
      ...(selectedEducationType.value ? { educationType: selectedEducationType.value } : {}),
      ...(selectedCourseYear.value ? { courseYear: Number(selectedCourseYear.value) } : {}),
      ...(selectedSemester.value ? { semester: selectedSemester.value } : {}),
    }
    const { data } = await subjectApi.list(params)
    items.value = unwrapList(data).sort((a, b) => a.name.localeCompare(b.name, 'uz'))
  } catch (e) {
    showError(getErrorMessage(e))
    items.value = []
  } finally {
    loading.value = false
  }
}

function exportExcel() {
  const header = [
    '№',
    'Fan malakaviy kodi',
    "O'qitiladigan fanlar",
    'Jami soat',
    "Ma'ruza",
    'Amaliy',
    'Laboratoriya',
    'Seminar',
    'Haftalik soat',
    'Mustaqil ish',
    'Kredit',
  ]
  const lines = items.value.map((item, index) =>
    [
      index + 1,
      item.code,
      item.name,
      item.totalSubjectHours ?? 0,
      item.lectureHours ?? 0,
      item.practicalHours ?? 0,
      item.labHours ?? 0,
      item.seminarHours ?? 0,
      weeklyHours(item),
      item.independentStudyHours ?? 0,
      formatCredit(item.credit),
    ].join(';'),
  )
  if (items.value.length) {
    lines.push(
      [
        '',
        '',
        'JAMI',
        totals.value.totalSubjectHours,
        totals.value.lectureHours,
        totals.value.practicalHours,
        totals.value.labHours,
        totals.value.seminarHours,
        Math.round(totals.value.weeklyHours * 10) / 10,
        totals.value.independentStudyHours,
        formatCredit(totals.value.credit),
      ].join(';'),
    )
  }
  const meta = [
    `Ta'lim yo'nalishi;${planHeader.value.direction}`,
    `Ta'lim shakli;${planHeader.value.educationType}`,
    `Kurs;${planHeader.value.course}`,
    `Semestr;${planHeader.value.semester}`,
    '',
  ]
  const blob = new Blob([[...meta, header.join(';'), ...lines].join('\n')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'dars-rejasi.csv'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadDirections()
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
