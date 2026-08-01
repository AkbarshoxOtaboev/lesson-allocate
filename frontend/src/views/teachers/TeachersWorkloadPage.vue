<template>
  <AdminLayout>
    <div class="space-y-4">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-xl font-bold text-slate-900 dark:text-white">O'qituvchilar yuklamasi</h2>
          <p class="mt-1 text-sm text-slate-500">
            Rolga qarab: admin — barcha; dekan — fakultet; kafedra — o'z kafedrasi
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl bg-teal-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-teal-700"
            @click="exportCsv"
          >
            Excelga yuklash
          </button>
        </div>
      </div>

      <div
        class="flex flex-col gap-3 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800 lg:flex-row lg:items-end"
      >
        <div v-if="auth.hasFullAccess || auth.isDekan" class="min-w-[180px] flex-1">
          <label class="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Fakultet
          </label>
          <select
            v-model="filterFacultyId"
            class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
            :disabled="auth.isDekan"
          >
            <option :value="null">Barcha fakultetlar</option>
            <option v-for="f in faculties" :key="f.id" :value="f.id">{{ f.name }}</option>
          </select>
        </div>
        <div class="min-w-[180px] flex-1">
          <label class="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Kafedra
          </label>
          <select
            v-model="filterDepartmentId"
            class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
            :disabled="auth.isKafedra"
          >
            <option :value="null">Barcha kafedralar</option>
            <option v-for="d in filteredDepartments" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
        </div>
        <div class="min-w-[220px] flex-[1.4]">
          <label class="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Qidiruv
          </label>
          <input
            v-model="search"
            type="search"
            placeholder="O'qituvchini izlash"
            class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
          />
        </div>
      </div>

      <div
        class="overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800"
      >
        <div v-if="loading" class="px-5 py-10 text-sm text-slate-500">Yuklanmoqda...</div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="border-b border-slate-100 bg-slate-50/80 dark:border-slate-700 dark:bg-slate-900/40">
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">#</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">O'qituvchi</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Kafedra</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Ish stavkasi</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Fanlar</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Ma'ruza</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Amaliy</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Lab</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Seminar</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Reyting</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Auditorik soat</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Mustaqil</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Umumiy soat</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Guruhlar</th>
                <th class="px-3 py-3 text-right text-xs font-semibold text-slate-500">Amallar</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-700">
              <tr
                v-for="(row, index) in filteredRows"
                :key="row.id"
                class="hover:bg-slate-50/70 dark:hover:bg-white/5"
              >
                <td class="px-3 py-3 text-slate-500">{{ index + 1 }}</td>
                <td class="px-3 py-3 font-medium text-slate-800 dark:text-white">
                  {{ row.fullName || row.name }}
                </td>
                <td class="px-3 py-3 text-slate-600 dark:text-slate-300">
                  {{ row.departmentName || '—' }}
                </td>
                <td class="px-3 py-3 text-slate-600">{{ formatStavka(row.stavka) }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.subjectCount ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.lectureHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.practicalHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.labHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.seminarHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.ratingHours ?? 0 }}</td>
                <td class="px-3 py-3 font-semibold text-slate-800 dark:text-white">
                  {{ row.totalHours ?? 0 }}
                </td>
                <td class="px-3 py-3 text-slate-600">{{ row.independentHours ?? 0 }}</td>
                <td class="px-3 py-3 font-semibold text-slate-800 dark:text-white">
                  {{ (row.totalHours ?? 0) + (row.independentHours ?? 0) }}
                </td>
                <td class="px-3 py-3 text-slate-600">{{ row.groupCount ?? 0 }}</td>
                <td class="px-3 py-3">
                  <div class="relative flex justify-end">
                    <button
                      type="button"
                      class="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 shadow-sm transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800"
                      title="Amallar"
                      @click.stop="toggleActionsMenu(row.id)"
                    >
                      <SlidersHorizontal class="size-4" :stroke-width="2" />
                    </button>
                    <div
                      v-if="openActionsId === row.id"
                      class="absolute right-0 top-full z-50 mt-1.5 w-56 rounded-xl border border-slate-100 bg-white p-1.5 shadow-lg dark:border-slate-700 dark:bg-slate-900"
                      @click.stop
                    >
                      <button
                        type="button"
                        class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-teal-700 transition hover:bg-teal-50 disabled:opacity-50 dark:text-teal-300 dark:hover:bg-teal-500/10"
                        :disabled="exportingId === row.id"
                        @click="exportTeacherAllocation(row)"
                      >
                        <FileSpreadsheet class="size-4 shrink-0" :stroke-width="2" />
                        {{ exportingId === row.id ? 'Yuklanmoqda...' : 'Dars taqsimoti (Excel)' }}
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredRows.length" class="bg-slate-50/80 font-semibold dark:bg-slate-900/40">
                <td colspan="4" class="px-3 py-3 text-slate-700 dark:text-slate-200">Jami</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.subjectCount }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.lectureHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.practicalHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.labHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.seminarHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.ratingHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.totalHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.independentHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.overallHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.groupCount }}</td>
                <td class="px-3 py-3"></td>
              </tr>
              <tr v-if="!filteredRows.length">
                <td colspan="15" class="px-5 py-10 text-center text-slate-500">Ma'lumot topilmadi</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { FileSpreadsheet, SlidersHorizontal } from 'lucide-vue-next'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { departmentApi, facultyApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { showError, showWarning } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { NamedEntity } from '@/types/api'
import type { TeacherWorkloadAllocation, TeacherWorkloadRow } from '@/types/talabnoma'
import api from '@/api/http'

const auth = useAuthStore()
const rows = ref<TeacherWorkloadRow[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<(NamedEntity & { facultyId?: number })[]>([])
const loading = ref(false)
const search = ref('')
const filterFacultyId = ref<number | null>(auth.facultyId)
const filterDepartmentId = ref<number | null>(auth.departmentId)
const openActionsId = ref<number | null>(null)
const exportingId = ref<number | null>(null)

const filteredDepartments = computed(() => {
  if (!filterFacultyId.value) return departments.value
  return departments.value.filter((d) => d.facultyId === filterFacultyId.value)
})

const filteredRows = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return rows.value
  return rows.value.filter((r) =>
    `${r.fullName || ''} ${r.name || ''} ${r.departmentName || ''}`.toLowerCase().includes(q),
  )
})

const totals = computed(() =>
  filteredRows.value.reduce(
    (acc, row) => {
      acc.subjectCount += row.subjectCount ?? 0
      acc.lectureHours += row.lectureHours ?? 0
      acc.practicalHours += row.practicalHours ?? 0
      acc.labHours += row.labHours ?? 0
      acc.seminarHours += row.seminarHours ?? 0
      acc.ratingHours += row.ratingHours ?? 0
      acc.totalHours += row.totalHours ?? 0
      acc.independentHours += row.independentHours ?? 0
      acc.overallHours += (row.totalHours ?? 0) + (row.independentHours ?? 0)
      acc.groupCount += row.groupCount ?? 0
      return acc
    },
    {
      subjectCount: 0,
      lectureHours: 0,
      practicalHours: 0,
      labHours: 0,
      seminarHours: 0,
      ratingHours: 0,
      totalHours: 0,
      independentHours: 0,
      overallHours: 0,
      groupCount: 0,
    },
  ),
)

function formatStavka(v?: number | null) {
  if (v == null) return '—'
  return Number(v).toFixed(2)
}

function semesterLabel(semester?: string | null) {
  if (semester === 'SPRING') return 'Bahorgi'
  if (semester === 'AUTUMN') return 'Kuzgi'
  return ''
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function toggleActionsMenu(id: number) {
  openActionsId.value = openActionsId.value === id ? null : id
}

function closeActionsMenu() {
  openActionsId.value = null
}

function onDocumentClick() {
  closeActionsMenu()
}

function safeFileName(name: string) {
  return name
    .trim()
    .replace(/[\\/:*?"<>|]+/g, '')
    .replace(/\s+/g, '-')
    .slice(0, 80)
}

function downloadCsv(filename: string, lines: string[]) {
  const blob = new Blob([['\uFEFF', lines.join('\n')].join('')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

async function loadCatalog() {
  try {
    const [fRes, dRes] = await Promise.all([
      facultyApi.list(auth.catalogScopeParams()),
      departmentApi.list(auth.catalogScopeParams()),
    ])
    faculties.value = unwrapList(fRes.data)
    departments.value = unwrapList(dRes.data) as (NamedEntity & { facultyId?: number })[]
  } catch {
    faculties.value = []
    departments.value = []
  }
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
    const { data } = await api.get<TeacherWorkloadRow[]>('/teachers/workload-summary', { params })
    rows.value = unwrapList(data)
  } catch (e) {
    showError(getErrorMessage(e))
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function exportTeacherAllocation(row: TeacherWorkloadRow) {
  exportingId.value = row.id
  try {
    const { data } = await api.get<TeacherWorkloadAllocation[]>(
      `/teachers/${row.id}/workload-allocations`,
    )
    const items = unwrapList(data)
    if (!items.length) {
      showWarning("Bu o'qituvchiga hali fan taqsimlanmagan")
      return
    }

    const teacherName = row.fullName || row.name || `teacher-${row.id}`
    const header = [
      '#',
      'Fan kodi',
      'Fan nomi',
      'Kafedra',
      'Semestr',
      'Kurs',
      "Ma'ruza",
      'Amaliy',
      'Lab',
      'Seminar',
      'Reyting',
      'Auditorik soat',
      'Mustaqil',
      'Umumiy soat',
      'Guruhlar',
      'Talabalar',
    ]

    let sumLecture = 0
    let sumPractical = 0
    let sumLab = 0
    let sumSeminar = 0
    let sumRating = 0
    let sumAuditorium = 0
    let sumIndependent = 0
    let sumOverall = 0
    let sumGroups = 0
    let sumStudents = 0

    const lines = items.map((item, index) => {
      const lecture = item.lectureHours ?? 0
      const practical = item.practicalHours ?? 0
      const lab = item.labHours ?? 0
      const seminar = item.seminarHours ?? 0
      const rating = item.ratingHours ?? 0
      const auditorium = item.totalHours ?? lecture + practical + lab + seminar + rating
      const independent = item.independentHours ?? 0
      const overall = auditorium + independent
      const groups = item.groupCount ?? 0
      const students = item.studentCount ?? 0

      sumLecture += lecture
      sumPractical += practical
      sumLab += lab
      sumSeminar += seminar
      sumRating += rating
      sumAuditorium += auditorium
      sumIndependent += independent
      sumOverall += overall
      sumGroups += groups
      sumStudents += students

      return [
        index + 1,
        item.subjectCode || '',
        item.subjectName || '',
        item.departmentName || '',
        semesterLabel(item.semester),
        item.courseYear ? `${item.courseYear}-kurs` : '',
        lecture,
        practical,
        lab,
        seminar,
        rating,
        auditorium,
        independent,
        overall,
        groups,
        students,
      ].join(';')
    })

    lines.push(
      [
        '',
        '',
        'Jami',
        '',
        '',
        '',
        sumLecture,
        sumPractical,
        sumLab,
        sumSeminar,
        sumRating,
        sumAuditorium,
        sumIndependent,
        sumOverall,
        sumGroups,
        sumStudents,
      ].join(';'),
    )

    const meta = [
      `O'qituvchi;${teacherName}`,
      `Kafedra;${row.departmentName || ''}`,
      `Fanlar soni;${items.length}`,
      '',
      header.join(';'),
      ...lines,
    ]

    downloadCsv(`${safeFileName(teacherName)}-dars-taqsimoti.csv`, meta)
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    exportingId.value = null
    closeActionsMenu()
  }
}

function exportCsv() {
  const header = [
    '#',
    "O'qituvchi",
    'Kafedra',
    'Stavka',
    'Fanlar',
    "Ma'ruza",
    'Amaliy',
    'Lab',
    'Seminar',
    'Reyting',
    'Auditorik soat',
    'Mustaqil',
    'Umumiy soat',
    'Guruhlar',
  ]
  const lines = filteredRows.value.map((r, i) =>
    [
      i + 1,
      r.fullName || r.name,
      r.departmentName || '',
      r.stavka ?? '',
      r.subjectCount ?? 0,
      r.lectureHours ?? 0,
      r.practicalHours ?? 0,
      r.labHours ?? 0,
      r.seminarHours ?? 0,
      r.ratingHours ?? 0,
      r.totalHours ?? 0,
      r.independentHours ?? 0,
      (r.totalHours ?? 0) + (r.independentHours ?? 0),
      r.groupCount ?? 0,
    ].join(';'),
  )
  lines.push(
    [
      '',
      'Jami',
      '',
      '',
      totals.value.subjectCount,
      totals.value.lectureHours,
      totals.value.practicalHours,
      totals.value.labHours,
      totals.value.seminarHours,
      totals.value.ratingHours,
      totals.value.totalHours,
      totals.value.independentHours,
      totals.value.overallHours,
      totals.value.groupCount,
    ].join(';'),
  )
  downloadCsv('oqituvchilar-yuklamasi.csv', [header.join(';'), ...lines])
}

watch(filterFacultyId, () => {
  if (!auth.isKafedra) filterDepartmentId.value = null
})

watch([filterFacultyId, filterDepartmentId], () => {
  void load()
})

onMounted(async () => {
  document.addEventListener('click', onDocumentClick)
  await loadCatalog()
  await load()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>
