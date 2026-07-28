<template>
  <ManagementLayout>
    <div
      class="rounded-2xl border border-slate-100 bg-white p-5 shadow-sm dark:border-slate-700 dark:bg-slate-800 sm:p-6"
    >
      <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div class="flex items-start gap-3">
          <div
            class="mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-300"
          >
            <CalendarDays class="h-5 w-5" :stroke-width="1.75" />
          </div>
          <div>
            <h2 class="text-lg font-bold text-slate-900 dark:text-white">
              O'quv yili va Semestrlar boshqaruvi
            </h2>
            <p class="mt-1 max-w-xl text-sm text-slate-500 dark:text-slate-400">
              Tizimda yuklamalar hisoblash uchun asosiy faol o'quv yilini belgilash
            </p>
          </div>
        </div>
        <button
          type="button"
          class="inline-flex shrink-0 items-center justify-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-[0_8px_20px_rgba(79,70,229,0.28)] transition hover:bg-indigo-700"
          @click="openCreate"
        >
          <Plus class="h-4 w-4" :stroke-width="2.25" />
          Yangi o'quv yili qo'shish
        </button>
      </div>

      <div v-if="loading" class="py-16 text-center text-sm text-slate-500">Yuklanmoqda...</div>

      <div v-else-if="!items.length" class="py-16 text-center text-sm text-slate-500">
        Hali o'quv yili qo‘shilmagan
      </div>

      <div v-else class="mt-6 space-y-3">
        <article
          v-for="card in yearCards"
          :key="card.item.id"
          class="flex flex-col gap-4 rounded-2xl border p-4 transition sm:flex-row sm:items-center sm:justify-between sm:p-5"
          :class="card.cardClass"
        >
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <h3 class="text-base font-bold text-slate-900 dark:text-white">
                {{ card.item.name }} o'quv yili
              </h3>
              <span
                class="rounded-full px-2.5 py-0.5 text-[11px] font-semibold"
                :class="card.badgeClass"
              >
                {{ card.badge }}
              </span>
            </div>
            <p class="mt-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
              {{ card.periodLabel }}
            </p>
            <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
              Boshlanish: {{ card.startDate }} • Tugash: {{ card.endDate }}
            </p>
          </div>

          <div class="flex flex-wrap items-center gap-2 sm:justify-end">
            <template v-if="card.state === 'active'">
              <span
                class="inline-flex items-center gap-1.5 text-sm font-semibold text-emerald-600 dark:text-emerald-400"
              >
                <CheckCircle2 class="h-5 w-5" :stroke-width="1.75" />
                Joriy faol yil
              </span>
            </template>
            <button
              v-else
              type="button"
              class="rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm font-semibold text-slate-600 transition hover:border-indigo-200 hover:bg-indigo-50 hover:text-indigo-700 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-300 dark:hover:border-indigo-500/40 dark:hover:bg-indigo-500/10"
              :disabled="activatingId === card.item.id"
              @click="makeCurrent(card.item)"
            >
              {{ activatingId === card.item.id ? 'Belgilanmoqda...' : 'Faol yil qilish' }}
            </button>

            <button
              type="button"
              class="rounded-xl border border-slate-200 px-3 py-2 text-sm font-medium text-slate-500 hover:bg-slate-50 dark:border-slate-600 dark:hover:bg-white/5"
              title="Tahrirlash"
              @click="openEdit(card.item)"
            >
              Tahrirlash
            </button>
            <button
              type="button"
              class="rounded-xl border border-rose-100 px-3 py-2 text-sm font-medium text-rose-500 hover:bg-rose-50 dark:border-rose-500/30 dark:hover:bg-rose-500/10"
              title="O‘chirish"
              @click="removeItem(card.item)"
            >
              O‘chirish
            </button>
          </div>
        </article>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div
          class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingId ? "O'quv yilini tahrirlash" : "Yangi o'quv yili" }}
          </h3>
          <form class="space-y-4" @submit.prevent="save">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                  Boshlanish yili
                </label>
                <input
                  v-model.number="startYear"
                  type="number"
                  required
                  min="2000"
                  max="2100"
                  placeholder="2025"
                  class="h-11 w-full rounded-xl border border-slate-200 bg-transparent px-4 text-sm text-gray-800 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/15 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  @input="onStartYearChange"
                />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                  Tugash yili
                </label>
                <input
                  v-model.number="endYear"
                  type="number"
                  required
                  min="2000"
                  max="2100"
                  placeholder="2026"
                  class="h-11 w-full rounded-xl border border-slate-200 bg-transparent px-4 text-sm text-gray-800 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/15 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                />
              </div>
            </div>

            <div
              class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm dark:border-gray-700 dark:bg-white/[0.03]"
            >
              <span class="text-gray-500">Nomi:</span>
              <span class="ml-2 font-medium text-gray-800 dark:text-white/90">
                {{ previewName || '—' }}
              </span>
            </div>

            <label class="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
              <input v-model="currentYear" type="checkbox" class="rounded border-gray-300" />
              Joriy o'quv yili sifatida belgilash
            </label>

            <div v-if="formError" class="text-sm text-error-600">{{ formError }}</div>
            <div class="flex justify-end gap-2">
              <button
                type="button"
                class="rounded-xl border border-slate-200 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
                @click="modalOpen = false"
              >
                Bekor
              </button>
              <button
                type="submit"
                class="rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-60"
                :disabled="saving"
              >
                Saqlash
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </ManagementLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CalendarDays, CheckCircle2, Plus } from 'lucide-vue-next'
import ManagementLayout from '@/components/layout/ManagementLayout.vue'
import Modal from '@/components/ui/Modal.vue'
import { academicYearApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { confirmAction, showError } from '@/utils/swal'
import type { EntityStatus } from '@/types/api'

type AcademicYearItem = {
  id: number
  name: string
  startYear: number
  endYear: number
  currentYear?: boolean
  status?: EntityStatus
}

const items = ref<AcademicYearItem[]>([])
const loading = ref(false)
const saving = ref(false)
const activatingId = ref<number | null>(null)
const formError = ref('')
const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const startYear = ref<number | null>(null)
const endYear = ref<number | null>(null)
const currentYear = ref(false)

const previewName = computed(() => {
  if (!startYear.value || !endYear.value) return ''
  return `${startYear.value}-${endYear.value}`
})

const yearCards = computed(() =>
  items.value.map((item) => {
    const state = resolveState(item)
    return {
      item,
      state,
      badge: state === 'active' ? 'Faol' : state === 'pending' ? 'Kutilmoqda' : 'Yakunlangan',
      badgeClass:
        state === 'active'
          ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300'
          : state === 'pending'
            ? 'bg-amber-50 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300'
            : 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300',
      cardClass:
        state === 'active'
          ? 'border-blue-200 bg-blue-50/60 dark:border-blue-500/30 dark:bg-blue-500/10'
          : 'border-slate-200 bg-white dark:border-slate-600 dark:bg-slate-800/50',
      periodLabel: `${item.startYear}/${item.endYear} o'quv davri`,
      startDate: `${item.startYear}-09-02`,
      endDate: `${item.endYear}-06-25`,
    }
  }),
)

function resolveState(item: AcademicYearItem): 'active' | 'pending' | 'finished' {
  if (item.currentYear) return 'active'
  if (item.status === 'DISABLED' || item.status === 'DELETED') return 'finished'
  const now = new Date().getFullYear()
  if (item.startYear >= now) return 'pending'
  return 'finished'
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function onStartYearChange() {
  if (startYear.value) {
    endYear.value = startYear.value + 1
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await academicYearApi.list()
    items.value = unwrapList(data) as AcademicYearItem[]
  } catch (e) {
    showError(getErrorMessage(e))
    items.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  const year = new Date().getFullYear()
  startYear.value = year
  endYear.value = year + 1
  currentYear.value = !items.value.some((i) => i.currentYear)
  formError.value = ''
  modalOpen.value = true
}

function openEdit(item: AcademicYearItem) {
  editingId.value = item.id
  startYear.value = item.startYear
  endYear.value = item.endYear
  currentYear.value = Boolean(item.currentYear)
  formError.value = ''
  modalOpen.value = true
}

async function save() {
  if (!startYear.value || !endYear.value) {
    formError.value = 'Yillarni kiriting'
    return
  }
  if (endYear.value !== startYear.value + 1) {
    formError.value = "Tugash yili boshlanishdan 1 ga katta bo'lishi kerak"
    return
  }

  saving.value = true
  formError.value = ''
  try {
    const payload = {
      startYear: startYear.value,
      endYear: endYear.value,
      currentYear: currentYear.value,
    }
    if (editingId.value) await academicYearApi.update(editingId.value, payload)
    else await academicYearApi.create(payload)
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function makeCurrent(item: AcademicYearItem) {
  activatingId.value = item.id
  try {
    await academicYearApi.update(item.id, {
      startYear: item.startYear,
      endYear: item.endYear,
      currentYear: true,
    })
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    activatingId.value = null
  }
}

async function removeItem(item: AcademicYearItem) {
  const ok = await confirmAction(`"${item.name}" o'quv yili o‘chirilsinmi?`, 'O‘chirish')
  if (!ok) return
  try {
    await academicYearApi.remove(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

onMounted(load)
</script>
