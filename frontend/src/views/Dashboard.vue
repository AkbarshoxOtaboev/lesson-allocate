<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Bosh sahifa" />

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 md:gap-6">
      <component
        :is="card.to ? 'router-link' : 'div'"
        v-for="card in cards"
        :key="card.key"
        v-bind="card.to ? { to: card.to } : {}"
        class="flex items-center justify-between rounded-2xl border border-gray-100 bg-white p-4 shadow-theme-xs transition dark:border-gray-800 dark:bg-white/[0.03] md:p-5"
        :class="
          card.to
            ? 'hover:border-brand-200 hover:shadow-theme-sm dark:hover:border-brand-500/30'
            : 'cursor-default'
        "
      >
        <div class="min-w-0 pr-3">
          <p class="text-xs font-medium leading-snug text-gray-500 dark:text-gray-400 sm:text-sm">
            {{ card.label }}
          </p>
          <p class="mt-2 text-2xl font-bold tracking-tight text-gray-800 dark:text-white/90 md:text-3xl">
            {{ loading ? '—' : card.value }}
          </p>
        </div>
        <div
          class="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-[#E8F1FF] text-[#3B82F6] dark:bg-blue-light-500/15 dark:text-blue-light-400"
        >
          <component :is="card.icon" class="size-6" />
        </div>
      </component>
    </div>

    <div
      class="mt-6 rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <h3 class="mb-2 text-lg font-semibold text-gray-800 dark:text-white/90">
        Xush kelibsiz, {{ auth.displayName }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        URSPI ichki boshqaruv tizimi: fakultet, kafedra va o‘qituvchilar tuzilmasi.
      </p>
      <p v-if="error" class="mt-3 text-sm text-error-600">{{ error }}</p>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { departmentApi, facultyApi, teacherApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import {
  BoxCubeIcon,
  CheckIcon,
  ClockIcon,
  FolderIcon,
  ListIcon,
  UserGroupIcon,
} from '@/icons'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(true)
const error = ref('')
const facultyCount = ref(0)
const departmentCount = ref(0)
const teacherCount = ref(0)
/** Keyinchalik yuklanadi */
const totalHours = ref(0)
const allocatedHours = ref(0)
const unallocatedHours = ref(0)

const cards = computed(() => [
  {
    key: 'faculties',
    to: '/faculties',
    label: 'Fakultetlar soni',
    value: facultyCount.value,
    icon: BoxCubeIcon,
  },
  {
    key: 'departments',
    to: '/departments',
    label: 'Kafedralar soni',
    value: departmentCount.value,
    icon: FolderIcon,
  },
  {
    key: 'teachers',
    to: '/teachers',
    label: "O'qituvchilar soni",
    value: teacherCount.value,
    icon: UserGroupIcon,
  },
  {
    key: 'total-hours',
    to: null as string | null,
    label: 'Jami soatlar',
    value: totalHours.value,
    icon: ClockIcon,
  },
  {
    key: 'allocated-hours',
    to: null as string | null,
    label: 'Taqsimlangan soatlar',
    value: allocatedHours.value,
    icon: CheckIcon,
  },
  {
    key: 'unallocated-hours',
    to: null as string | null,
    label: 'Taqsimlanmagan soatlar',
    value: unallocatedHours.value,
    icon: ListIcon,
  },
])

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

async function loadStats() {
  loading.value = true
  error.value = ''
  try {
    const scope = auth.catalogScopeParams()
    const [fac, dep, tea] = await Promise.all([
      facultyApi.list(),
      departmentApi.list(scope.facultyId ? { facultyId: scope.facultyId } : undefined),
      teacherApi.list(
        Object.keys(scope).length
          ? {
              ...(scope.facultyId ? { facultyId: scope.facultyId } : {}),
              ...(scope.departmentId ? { departmentId: scope.departmentId } : {}),
            }
          : undefined,
      ),
    ])
    facultyCount.value = unwrapList(fac.data).length
    departmentCount.value = unwrapList(dep.data).length
    teacherCount.value = unwrapList(tea.data).length
    totalHours.value = 0
    allocatedHours.value = 0
    unallocatedHours.value = 0
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>
