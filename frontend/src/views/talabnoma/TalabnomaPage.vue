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
          <option value="ACCEPTED">Qabul qilingan</option>
          <option value="PARTIAL">Qisman taqsimlangan</option>
          <option value="ALLOCATED">Taqsimlangan</option>
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
                <th class="px-4 py-3">Kafedra / Fakultet</th>
                <th class="px-4 py-3">Fan / Semestr</th>
                <th class="px-4 py-3">Talab soat</th>
                <th class="px-4 py-3">Talabgor</th>
                <th class="px-4 py-3">Holati</th>
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
                  <p class="font-medium text-slate-800 dark:text-white">{{ item.toDepartmentName }}</p>
                  <p class="text-xs text-slate-500">{{ item.toFacultyName || item.fromFacultyName }}</p>
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
                  {{ item.totalHours || 0 }} soat
                  <span v-if="item.allocatedHours" class="block text-xs font-normal text-slate-500">
                    Taqsimlangan: {{ item.allocatedHours }}
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
                    class="mt-2 space-y-0.5 text-xs text-slate-500"
                  >
                    <p v-for="t in item.allocatedTeachers" :key="t.teacherId">
                      {{ t.teacherName }} — {{ t.hours }} soat
                    </p>
                  </div>
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
        <div class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900" @click.stop>
          <h3 class="mb-4 text-lg font-semibold text-slate-900 dark:text-white">Yangi talabnoma</h3>
          <form class="space-y-3" @submit.prevent="save">
            <div>
              <label class="mb-1 block text-sm text-slate-600">Qabul qiluvchi kafedra</label>
              <select
                v-model.number="form.toDepartmentId"
                required
                class="h-11 w-full rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
              >
                <option disabled :value="0">Tanlang</option>
                <option v-for="d in createDepartments" :key="d.id" :value="d.id">
                  {{ d.name }}{{ d.facultyName ? ` (${d.facultyName})` : '' }}
                </option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm text-slate-600">Fan nomi</label>
              <input
                v-model="form.subjectName"
                required
                class="h-11 w-full rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
              />
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="mb-1 block text-sm text-slate-600">Semestr</label>
                <select
                  v-model="form.semester"
                  class="h-11 w-full rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
                >
                  <option value="AUTUMN">Kuzgi</option>
                  <option value="SPRING">Bahorgi</option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm text-slate-600">O'quv yili</label>
                <select
                  v-model.number="form.academicYearId"
                  class="h-11 w-full rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
                >
                  <option :value="0">Joriy</option>
                  <option v-for="y in years" :key="y.id" :value="y.id">{{ y.name }}</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
              <div v-for="field in hourFields" :key="field.key">
                <label class="mb-1 block text-xs text-slate-500">{{ field.label }}</label>
                <input
                  v-model.number="form[field.key]"
                  type="number"
                  min="0"
                  class="h-10 w-full rounded-xl border border-slate-200 px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
                />
              </div>
            </div>
            <div>
              <label class="mb-1 block text-sm text-slate-600">Izoh</label>
              <textarea
                v-model="form.note"
                rows="2"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm dark:border-slate-600 dark:bg-slate-900"
              />
            </div>
            <div class="flex justify-end gap-2 pt-2">
              <button
                type="button"
                class="rounded-xl border border-slate-200 px-4 py-2.5 text-sm"
                @click="modalOpen = false"
              >
                Bekor
              </button>
              <button
                type="submit"
                class="rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white"
                :disabled="saving"
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
import { academicYearApi, departmentApi, facultyApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { confirmAction, showError } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { NamedEntity } from '@/types/api'
import type { Talabnoma, TalabnomaStats, TalabnomaStatus } from '@/types/talabnoma'

const auth = useAuthStore()
const router = useRouter()

const items = ref<Talabnoma[]>([])
const stats = ref<TalabnomaStats>({ total: 0, pending: 0, accepted: 0, rejected: 0, allocated: 0 })
const faculties = ref<NamedEntity[]>([])
const departments = ref<(NamedEntity & { facultyId?: number; facultyName?: string })[]>([])
const years = ref<NamedEntity[]>([])
const loading = ref(false)
const saving = ref(false)
const modalOpen = ref(false)
const search = ref('')
const statusFilter = ref<TalabnomaStatus | null>(null)
const filterFacultyId = ref<number | null>(null)
const filterDepartmentId = ref<number | null>(null)

const form = reactive({
  toDepartmentId: 0,
  subjectName: '',
  semester: 'AUTUMN' as 'AUTUMN' | 'SPRING',
  academicYearId: 0,
  lectureHours: 0,
  practicalHours: 0,
  labHours: 0,
  seminarHours: 0,
  ratingHours: 0,
  note: '',
})

const hourFields = [
  { key: 'lectureHours' as const, label: "Ma'ruza" },
  { key: 'practicalHours' as const, label: 'Amaliy' },
  { key: 'labHours' as const, label: 'Lab' },
  { key: 'seminarHours' as const, label: 'Seminar' },
  { key: 'ratingHours' as const, label: 'Reyting' },
]

const canCreate = computed(() => auth.isDekan || auth.hasFullAccess)

const filteredDepartments = computed(() => {
  if (!filterFacultyId.value) return departments.value
  return departments.value.filter((d) => d.facultyId === filterFacultyId.value)
})

const createDepartments = computed(() => {
  // Dekan o'z fakultetidan tashqari kafedralarga yuboradi — barcha kafedralar
  return departments.value
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
    label: 'Tasdiqlangan',
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

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function statusLabel(s: TalabnomaStatus) {
  const map: Record<TalabnomaStatus, string> = {
    NEW: 'Kutilmoqda',
    ACCEPTED: 'Qabul qilingan',
    REJECTED: 'Rad etilgan',
    PARTIAL: 'Qisman taqsimlangan',
    ALLOCATED: 'Taqsimlangan',
  }
  return map[s] || s
}

function statusClass(s: TalabnomaStatus) {
  const map: Record<TalabnomaStatus, string> = {
    NEW: 'bg-amber-50 text-amber-700',
    ACCEPTED: 'bg-emerald-50 text-emerald-700',
    REJECTED: 'bg-rose-50 text-rose-700',
    PARTIAL: 'bg-sky-50 text-sky-700',
    ALLOCATED: 'bg-indigo-50 text-indigo-700',
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
  const [f, d, y] = await Promise.all([
    facultyApi.list(),
    departmentApi.list(),
    academicYearApi.list(),
  ])
  faculties.value = unwrapList(f.data)
  departments.value = unwrapList(d.data) as typeof departments.value
  years.value = unwrapList(y.data)
}

function openCreate() {
  form.toDepartmentId = 0
  form.subjectName = ''
  form.semester = 'AUTUMN'
  form.academicYearId = 0
  form.lectureHours = 0
  form.practicalHours = 0
  form.labHours = 0
  form.seminarHours = 0
  form.ratingHours = 0
  form.note = ''
  modalOpen.value = true
}

async function save() {
  saving.value = true
  try {
    await talabnomaApi.create({
      toDepartmentId: form.toDepartmentId,
      subjectName: form.subjectName,
      semester: form.semester,
      academicYearId: form.academicYearId || undefined,
      lectureHours: form.lectureHours,
      practicalHours: form.practicalHours,
      labHours: form.labHours,
      seminarHours: form.seminarHours,
      ratingHours: form.ratingHours,
      note: form.note || undefined,
    })
    modalOpen.value = false
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    saving.value = false
  }
}

async function accept(item: Talabnoma) {
  const ok = await confirmAction(`"${item.code}" qabul qilinsinmi? Fan yuklamaga qo'shiladi.`, 'Qabul')
  if (!ok) return
  try {
    await talabnomaApi.accept(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

async function reject(item: Talabnoma) {
  const ok = await confirmAction(`"${item.code}" rad etilsinmi?`, 'Rad etish')
  if (!ok) return
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
    [i.code, i.toDepartmentName, i.subjectName, i.totalHours, i.requestStatus, i.createdByName].join(';'),
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
  await loadLookups()
  await load()
})
</script>
