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
        <button
          type="button"
          class="h-11 rounded-xl bg-teal-600 px-5 text-sm font-semibold text-white hover:bg-teal-700"
          @click="load"
        >
          Qidirish
        </button>
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
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Talabalar</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-700">
              <tr v-for="(row, index) in filteredRows" :key="row.id" class="hover:bg-slate-50/70 dark:hover:bg-white/5">
                <td class="px-3 py-3 text-slate-500">{{ index + 1 }}</td>
                <td class="px-3 py-3 font-medium text-slate-800 dark:text-white">
                  {{ row.fullName || row.name }}
                </td>
                <td class="px-3 py-3 text-slate-600 dark:text-slate-300">{{ row.departmentName || '—' }}</td>
                <td class="px-3 py-3 text-slate-600">{{ formatStavka(row.stavka) }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.subjectCount ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.lectureHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.practicalHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.labHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.seminarHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.ratingHours ?? 0 }}</td>
                <td class="px-3 py-3 font-semibold text-slate-800 dark:text-white">{{ row.totalHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.independentHours ?? 0 }}</td>
                <td class="px-3 py-3 font-semibold text-slate-800 dark:text-white">
                  {{ (row.totalHours ?? 0) + (row.independentHours ?? 0) }}
                </td>
                <td class="px-3 py-3 text-slate-600">{{ row.groupCount ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.studentCount ?? 0 }}</td>
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
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { departmentApi, facultyApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { showError } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { NamedEntity } from '@/types/api'
import type { TeacherWorkloadRow } from '@/types/talabnoma'
import api from '@/api/http'

const auth = useAuthStore()
const rows = ref<TeacherWorkloadRow[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<(NamedEntity & { facultyId?: number })[]>([])
const loading = ref(false)
const search = ref('')
const filterFacultyId = ref<number | null>(auth.facultyId)
const filterDepartmentId = ref<number | null>(auth.departmentId)

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

function formatStavka(v?: number | null) {
  if (v == null) return '—'
  return Number(v).toFixed(2)
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
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
    ].join(';'),
  )
  const blob = new Blob([[header.join(';'), ...lines].join('\n')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'oqituvchilar-yuklamasi.csv'
  a.click()
  URL.revokeObjectURL(url)
}

watch(filterFacultyId, () => {
  if (!auth.isKafedra) filterDepartmentId.value = null
})

onMounted(async () => {
  await loadCatalog()
  await load()
})
</script>
