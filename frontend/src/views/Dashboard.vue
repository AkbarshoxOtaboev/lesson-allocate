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

    <!-- Fakultetlar — to'liq kenglik -->
    <div
      class="mt-6 overflow-hidden rounded-2xl border border-gray-200 bg-white px-5 pt-5 pb-3 dark:border-gray-800 dark:bg-white/[0.03] sm:px-6"
    >
      <div class="mb-4">
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
          Fakultetlar kesimida soatlar
        </h3>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Umumiy, taqsimlangan va taqsimlanmagan dars soatlari
        </p>
      </div>
      <div class="max-w-full overflow-x-auto custom-scrollbar">
        <div class="min-w-[560px] w-full">
          <VueApexCharts
            v-if="!loading && facultyCategories.length"
            type="bar"
            height="340"
            :options="facultyChartOptions"
            :series="facultySeries"
          />
          <p v-else class="py-16 text-center text-sm text-gray-500">
            {{ loading ? 'Yuklanmoqda...' : 'Hali fan yuklamasi yo‘q' }}
          </p>
        </div>
      </div>
    </div>

    <!-- Har fakultet: 2 tadan bitta qatorda -->
    <div
      v-if="!loading && facultyDepartmentCharts.length"
      class="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-2"
    >
      <div
        v-for="block in facultyDepartmentCharts"
        :key="block.facultyId"
        class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
      >
        <div
          class="border-b border-gray-100 bg-gradient-to-r from-[#EEF2FF] via-white to-[#ECFDF5] px-4 py-3 dark:border-gray-800 dark:from-brand-500/10 dark:via-transparent dark:to-success-500/10 sm:px-5"
        >
          <div class="flex flex-wrap items-start justify-between gap-2">
            <div class="min-w-0">
              <h3 class="truncate text-base font-semibold text-gray-800 dark:text-white/90 sm:text-lg">
                {{ block.facultyName }}
              </h3>
              <p class="mt-0.5 text-xs text-gray-500 dark:text-gray-400 sm:text-sm">
                Kafedralar bo‘yicha taqsimot
              </p>
            </div>
            <div class="flex flex-wrap gap-1.5">
              <span
                class="rounded-full bg-brand-50 px-2.5 py-1 text-[11px] font-semibold text-brand-600 dark:bg-brand-500/15 dark:text-brand-300"
              >
                {{ block.totalHours }} soat
              </span>
              <span
                class="rounded-full bg-success-50 px-2.5 py-1 text-[11px] font-semibold text-success-600 dark:bg-success-500/15 dark:text-success-300"
              >
                {{ block.percent }}%
              </span>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 gap-1 p-3 sm:grid-cols-12 sm:items-center sm:p-4">
          <div class="max-w-full overflow-x-auto custom-scrollbar sm:col-span-8">
            <div class="min-w-[280px] w-full">
              <VueApexCharts
                type="bar"
                height="260"
                :options="block.barOptions"
                :series="block.barSeries"
              />
            </div>
          </div>
          <div class="flex justify-center sm:col-span-4">
            <VueApexCharts
              type="radialBar"
              height="220"
              :options="block.radialOptions"
              :series="block.radialSeries"
            />
          </div>
        </div>
      </div>
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
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { departmentApi, facultyApi, teacherApi } from '@/api/catalog'
import { workloadApi, type HoursByGroup } from '@/api/workload'
import { getErrorMessage } from '@/api/http'
import { showError } from '@/utils/swal'
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
const totalHours = ref(0)
const allocatedHours = ref(0)
const unallocatedHours = ref(0)
const byFaculty = ref<HoursByGroup[]>([])
const byDepartment = ref<HoursByGroup[]>([])

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
    to: '/workloads' as string | null,
    label: 'Jami soatlar',
    value: totalHours.value,
    icon: ClockIcon,
  },
  {
    key: 'allocated-hours',
    to: '/workloads' as string | null,
    label: 'Taqsimlangan soatlar',
    value: allocatedHours.value,
    icon: CheckIcon,
  },
  {
    key: 'unallocated-hours',
    to: '/workloads' as string | null,
    label: 'Taqsimlanmagan soatlar',
    value: unallocatedHours.value,
    icon: ListIcon,
  },
])

function shortLabel(name: string, max = 22) {
  const clean = name
    .replace(/\s+kafedrasi$/i, '')
    .replace(/\s+fakulteti$/i, '')
    .replace(/\s+bo'?limi$/i, '')
  return clean.length > max ? `${clean.slice(0, max - 1)}…` : clean
}

function barChartOptions(categories: string[]) {
  return {
    colors: ['#465FFF', '#12B76A', '#F04438'],
    chart: {
      fontFamily: 'Outfit, sans-serif',
      type: 'bar',
      toolbar: { show: false },
      stacked: false,
      animations: {
        enabled: true,
        speed: 700,
        animateGradually: { enabled: true, delay: 120 },
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: '48%',
        borderRadius: 8,
        borderRadiusApplication: 'end',
      },
    },
    dataLabels: { enabled: false },
    stroke: { show: true, width: 3, colors: ['transparent'] },
    xaxis: {
      categories,
      labels: {
        style: { fontSize: '11px', colors: '#667085' },
        rotate: -35,
        rotateAlways: categories.length > 3,
      },
      axisBorder: { show: false },
      axisTicks: { show: false },
    },
    yaxis: {
      labels: { style: { fontSize: '12px', colors: '#667085' } },
    },
    legend: {
      show: true,
      position: 'top',
      horizontalAlign: 'left',
      fontFamily: 'Outfit',
      markers: { radius: 99 },
    },
    grid: {
      borderColor: '#F2F4F7',
      yaxis: { lines: { show: true } },
    },
    fill: { opacity: 1 },
    tooltip: {
      y: { formatter: (val: number) => `${val} soat` },
    },
  }
}

function stackedDeptOptions(categories: string[]) {
  return {
    colors: ['#12B76A', '#F04438'],
    chart: {
      fontFamily: 'Outfit, sans-serif',
      type: 'bar',
      stacked: true,
      toolbar: { show: false },
      animations: {
        enabled: true,
        easing: 'easeinout',
        speed: 900,
        animateGradually: { enabled: true, delay: 100 },
        dynamicAnimation: { enabled: true, speed: 350 },
      },
      dropShadow: {
        enabled: true,
        top: 6,
        left: 0,
        blur: 8,
        opacity: 0.12,
        color: '#101828',
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: '42%',
        borderRadius: 10,
        borderRadiusApplication: 'end',
        borderRadiusWhenStacked: 'last',
      },
    },
    dataLabels: { enabled: false },
    stroke: { show: true, width: 2, colors: ['transparent'] },
    xaxis: {
      categories,
      labels: {
        style: { fontSize: '11px', colors: '#667085' },
        rotate: -30,
        rotateAlways: categories.length > 2,
      },
      axisBorder: { show: false },
      axisTicks: { show: false },
    },
    yaxis: {
      labels: { style: { fontSize: '12px', colors: '#667085' } },
      title: {
        text: 'Soat',
        style: { color: '#98A2B3', fontSize: '12px', fontWeight: 500 },
      },
    },
    legend: {
      show: true,
      position: 'top',
      horizontalAlign: 'left',
      fontFamily: 'Outfit',
      markers: { radius: 99 },
    },
    grid: {
      borderColor: '#EAECF0',
      strokeDashArray: 4,
      yaxis: { lines: { show: true } },
    },
    fill: {
      type: 'gradient',
      gradient: {
        shade: 'light',
        type: 'vertical',
        shadeIntensity: 0.35,
        opacityFrom: 0.95,
        opacityTo: 0.75,
        stops: [0, 90, 100],
      },
    },
    tooltip: {
      shared: true,
      intersect: false,
      y: { formatter: (val: number) => `${val} soat` },
    },
  }
}

function radialOptions(percent: number) {
  const color = percent >= 70 ? '#12B76A' : percent >= 35 ? '#F79009' : '#F04438'
  return {
    chart: {
      fontFamily: 'Outfit, sans-serif',
      type: 'radialBar',
      sparkline: { enabled: false },
      animations: { enabled: true, speed: 1000 },
    },
    colors: [color],
    plotOptions: {
      radialBar: {
        startAngle: -120,
        endAngle: 120,
        hollow: {
          size: '68%',
        },
        track: {
          background: '#F2F4F7',
          strokeWidth: '100%',
        },
        dataLabels: {
          name: {
            show: true,
            offsetY: 28,
            color: '#667085',
            fontSize: '13px',
            fontWeight: 500,
          },
          value: {
            show: true,
            offsetY: -10,
            fontSize: '28px',
            fontWeight: 700,
            color: '#101828',
            formatter: (val: number) => `${Math.round(val)}%`,
          },
        },
      },
    },
    fill: {
      type: 'gradient',
      gradient: {
        shade: 'dark',
        type: 'horizontal',
        shadeIntensity: 0.4,
        gradientToColors: [color],
        inverseColors: false,
        opacityFrom: 1,
        opacityTo: 0.85,
        stops: [0, 100],
      },
    },
    stroke: { lineCap: 'round' },
    labels: ['Taqsimot'],
  }
}

function hoursSeries(rows: HoursByGroup[]) {
  return [
    { name: 'Umumiy', data: rows.map((r) => r.totalHours ?? 0) },
    { name: 'Taqsimlangan', data: rows.map((r) => r.allocatedHours ?? 0) },
    { name: 'Taqsimlanmagan', data: rows.map((r) => r.unallocatedHours ?? 0) },
  ]
}

function stackedDeptSeries(rows: HoursByGroup[]) {
  return [
    { name: 'Taqsimlangan', data: rows.map((r) => r.allocatedHours ?? 0) },
    { name: 'Taqsimlanmagan', data: rows.map((r) => r.unallocatedHours ?? 0) },
  ]
}

const facultyRows = computed(() =>
  byFaculty.value.filter((r) => (r.totalHours ?? 0) > 0 || (r.allocatedHours ?? 0) > 0),
)

const facultyCategories = computed(() => facultyRows.value.map((r) => shortLabel(r.name, 18)))

const facultySeries = computed(() => hoursSeries(facultyRows.value))

const facultyChartOptions = computed(() => barChartOptions(facultyCategories.value))

const facultyDepartmentCharts = computed(() => {
  const groups = new Map<
    number,
    { facultyId: number; facultyName: string; rows: HoursByGroup[] }
  >()

  for (const row of byDepartment.value) {
    if ((row.totalHours ?? 0) <= 0 && (row.allocatedHours ?? 0) <= 0) continue
    const facultyId = row.facultyId
    if (!facultyId) continue
    const existing = groups.get(facultyId)
    if (existing) {
      existing.rows.push(row)
    } else {
      groups.set(facultyId, {
        facultyId,
        facultyName: row.facultyName || `Fakultet #${facultyId}`,
        rows: [row],
      })
    }
  }

  const order = byFaculty.value.map((f) => f.id)
  return [...groups.values()]
    .sort((a, b) => {
      const ia = order.indexOf(a.facultyId)
      const ib = order.indexOf(b.facultyId)
      return (ia === -1 ? 999 : ia) - (ib === -1 ? 999 : ib)
    })
    .map((block) => {
      const categories = block.rows.map((r) => shortLabel(r.name, 20))
      const total = block.rows.reduce((s, r) => s + (r.totalHours ?? 0), 0)
      const allocated = block.rows.reduce((s, r) => s + (r.allocatedHours ?? 0), 0)
      const percent = total > 0 ? Math.round((allocated / total) * 100) : 0
      return {
        facultyId: block.facultyId,
        facultyName: block.facultyName,
        totalHours: total,
        percent,
        barSeries: stackedDeptSeries(block.rows),
        barOptions: stackedDeptOptions(categories),
        radialSeries: [percent],
        radialOptions: radialOptions(percent),
      }
    })
})

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
    const teacherParams = Object.keys(scope).length
      ? {
          ...(scope.facultyId ? { facultyId: scope.facultyId } : {}),
          ...(scope.departmentId ? { departmentId: scope.departmentId } : {}),
        }
      : undefined

    const requests = [
      auth.hasFullAccess
        ? facultyApi.list()
        : Promise.resolve({
            data:
              auth.facultyId && (auth.isDekan || auth.isKafedra)
                ? [{ id: auth.facultyId, name: auth.user?.facultyName || 'Fakultet' }]
                : [],
          }),
      auth.isKafedra && auth.departmentId
        ? Promise.resolve({
            data: [{ id: auth.departmentId, name: auth.user?.departmentName || 'Kafedra' }],
          })
        : departmentApi.list(scope.facultyId ? { facultyId: scope.facultyId } : undefined),
      teacherApi.list(teacherParams),
      workloadApi.dashboardHours(),
    ] as const

    const [fac, dep, tea, hours] = await Promise.all(requests)
    facultyCount.value = unwrapList(fac.data).length
    departmentCount.value = unwrapList(dep.data).length
    teacherCount.value = unwrapList(tea.data).length
    totalHours.value = hours.data?.totalHours ?? 0
    allocatedHours.value = hours.data?.allocatedHours ?? 0
    unallocatedHours.value = hours.data?.unallocatedHours ?? 0
    byFaculty.value = hours.data?.byFaculty ?? []
    byDepartment.value = hours.data?.byDepartment ?? []
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>
