<template>
  <Modal full-screen-backdrop @close="emit('close')">
    <template #body>
      <div
        class="relative flex max-h-[90vh] w-full max-w-5xl flex-col rounded-2xl bg-white dark:bg-gray-900"
        @click.stop
      >
        <div class="border-b border-gray-200 px-6 py-4 dark:border-gray-800">
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
            HEMIS dan yuklash — {{ title }}
          </h3>
          <p class="mt-1 text-sm text-gray-500">{{ endpointHint }}</p>
        </div>

        <div class="space-y-4 overflow-y-auto px-6 py-4">
          <template v-if="isTeachers">
            <div class="grid grid-cols-2 gap-3 md:grid-cols-4">
              <div>
                <label class="mb-1 block text-xs text-gray-500">page</label>
                <input v-model.number="employeeFilters.page" type="number" min="1" class="field" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">limit (1–200)</label>
                <input
                  v-model.number="employeeFilters.limit"
                  type="number"
                  min="1"
                  max="200"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">type</label>
                <select v-model="employeeFilters.type" class="field">
                  <option value="teacher">teacher</option>
                  <option value="employee">employee</option>
                  <option value="all">all</option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_department</label>
                <input
                  v-model.number="employeeFilters.department"
                  type="number"
                  min="1"
                  placeholder="department id"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">search</label>
                <input
                  v-model="employeeFilters.search"
                  type="text"
                  placeholder="ism / employee id"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_employee_type</label>
                <input v-model="employeeFilters.employeeType" type="text" class="field" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_staff_position</label>
                <input v-model="employeeFilters.staffPosition" type="text" class="field" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_employee_status</label>
                <input v-model="employeeFilters.employeeStatus" type="text" class="field" />
              </div>
            </div>
          </template>

          <template v-else-if="isGroups">
            <div class="grid grid-cols-2 gap-3 md:grid-cols-4">
              <div>
                <label class="mb-1 block text-xs text-gray-500">page</label>
                <input v-model.number="groupFilters.page" type="number" min="1" class="field" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">limit (1–200)</label>
                <input
                  v-model.number="groupFilters.limit"
                  type="number"
                  min="1"
                  max="200"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">id</label>
                <input
                  v-model.number="groupFilters.id"
                  type="number"
                  min="1"
                  placeholder="group id"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_department</label>
                <input
                  v-model.number="groupFilters.department"
                  type="number"
                  min="1"
                  placeholder="department id"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_curriculum</label>
                <input
                  v-model.number="groupFilters.curriculum"
                  type="number"
                  min="1"
                  placeholder="curriculum id"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_specialty</label>
                <input
                  v-model.number="groupFilters.specialty"
                  type="number"
                  min="1"
                  placeholder="specialty id"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_education_type</label>
                <input v-model="groupFilters.educationType" type="text" class="field" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_education_form</label>
                <input v-model="groupFilters.educationForm" type="text" class="field" />
              </div>
            </div>
          </template>

          <template v-else-if="!isTeachers">
            <div class="grid grid-cols-2 gap-3 md:grid-cols-5">
              <div>
                <label class="mb-1 block text-xs text-gray-500">page</label>
                <input v-model.number="deptFilters.page" type="number" min="1" class="field" />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">limit (1–200)</label>
                <input
                  v-model.number="deptFilters.limit"
                  type="number"
                  min="1"
                  max="200"
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">active</label>
                <select v-model="deptFilters.active" class="field">
                  <option value="1">1 (faol)</option>
                  <option value="0">0 (nofaol)</option>
                  <option value="all">all</option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">_structure_type</label>
                <input
                  v-model="deptFilters.structureType"
                  type="text"
                  placeholder="11, 12, 13..."
                  class="field"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs text-gray-500">parent</label>
                <input
                  v-model.number="deptFilters.parent"
                  type="number"
                  min="1"
                  placeholder="parent id"
                  class="field"
                />
              </div>
            </div>
          </template>

          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
              :disabled="loading"
              @click="load"
            >
              {{ loading ? 'Yuklanmoqda...' : "Ro‘yxatni yuklash" }}
            </button>
            <button
              type="button"
              class="rounded-lg border border-brand-300 px-4 py-2.5 text-sm text-brand-600 disabled:opacity-60 dark:border-brand-500/40"
              :disabled="syncing || !hasItems"
              @click="sync"
            >
              {{ syncing ? 'Saqlanmoqda...' : 'Lokal bazaga sync' }}
            </button>
            <button
              type="button"
              class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm dark:border-gray-700"
              @click="emit('close')"
            >
              Yopish
            </button>
          </div>

          <div v-if="error" class="text-sm text-error-600">{{ error }}</div>
          <div v-if="syncResult" class="text-sm text-success-600">
            Sync: fetched={{ syncResult.fetched }}, created={{ syncResult.created }},
            updated={{ syncResult.updated }}, skipped={{ syncResult.skipped }}
          </div>
          <p v-if="meta" class="text-xs text-gray-500">
            Sahifa {{ meta.page || currentPage }} / {{ meta.pageCount || '—' }} · jami
            {{ meta.totalCount ?? itemCount }}
          </p>

          <div class="max-h-[420px] overflow-auto rounded-lg border border-gray-200 dark:border-gray-800">
            <table v-if="isTeachers" class="min-w-full">
              <thead class="sticky top-0 bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">№</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">F.I.Sh.</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Lavozim</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Kafedra</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Turi</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
                <tr v-for="(item, index) in employees" :key="item.id">
                  <td class="px-3 py-2 text-sm text-gray-500">{{ index + 1 }}</td>
                  <td class="px-3 py-2 text-sm font-medium text-gray-800 dark:text-white/90">
                    {{ employeeName(item) }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.staffPosition?.name || item.staffPosition?.code || '—' }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.department?.name || '—' }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.employeeType?.name || item.employeeType?.code || '—' }}
                  </td>
                </tr>
                <tr v-if="!employees.length && !loading">
                  <td colspan="5" class="px-3 py-8 text-center text-sm text-gray-500">
                    Parametrlarni to‘ldiring va “Ro‘yxatni yuklash” ni bosing
                  </td>
                </tr>
              </tbody>
            </table>

            <table v-else-if="isGroups" class="min-w-full">
              <thead class="sticky top-0 bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">№</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Nomi</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">HEMIS kafedra</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Lokal kafedra</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Mutaxassislik</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Ta'lim tili</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
                <tr v-for="(item, index) in groups" :key="item.id">
                  <td class="px-3 py-2 text-sm text-gray-500">{{ index + 1 }}</td>
                  <td class="px-3 py-2 text-sm font-medium text-gray-800 dark:text-white/90">
                    {{ item.name }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.department?.name || '—' }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    <span
                      :class="localDepartmentName(item) ? 'text-emerald-600 dark:text-emerald-400' : 'text-amber-600 dark:text-amber-400'"
                    >
                      {{ localDepartmentName(item) || 'Topilmadi' }}
                    </span>
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ groupSpecialtyName(item) }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.educationLang?.name || item.educationLang?.code || '—' }}
                  </td>
                </tr>
                <tr v-if="!groups.length && !loading">
                  <td colspan="6" class="px-3 py-8 text-center text-sm text-gray-500">
                    Parametrlarni to‘ldiring va “Ro‘yxatni yuklash” ni bosing
                  </td>
                </tr>
              </tbody>
            </table>

            <table v-else class="min-w-full">
              <thead class="sticky top-0 bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">№</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Nomi</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Kod</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Parent</th>
                  <th class="px-3 py-2 text-left text-xs font-medium text-gray-500">Turi</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
                <tr v-for="(item, index) in departments" :key="item.id">
                  <td class="px-3 py-2 text-sm text-gray-500">{{ index + 1 }}</td>
                  <td class="px-3 py-2 text-sm font-medium text-gray-800 dark:text-white/90">
                    {{ item.name }}
                  </td>
                  <td class="px-3 py-2 text-sm text-gray-500">{{ item.code || '—' }}</td>
                  <td class="px-3 py-2 text-sm text-gray-500">{{ item.parent ?? '—' }}</td>
                  <td class="px-3 py-2 text-sm text-gray-500">
                    {{ item.structureType?.name || item.structureType?.code || '—' }}
                  </td>
                </tr>
                <tr v-if="!departments.length && !loading">
                  <td colspan="5" class="px-3 py-8 text-center text-sm text-gray-500">
                    Parametrlarni to‘ldiring va “Ro‘yxatni yuklash” ni bosing
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </template>
  </Modal>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import Modal from '@/components/ui/Modal.vue'
import { departmentApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import {
  hemisApi,
  type HemisDepartment,
  type HemisDepartmentListResponse,
  type HemisEmployee,
  type HemisEmployeeListResponse,
  type HemisGroup,
  type HemisGroupListResponse,
  type HemisSyncResult,
} from '@/api/hemis'

const props = defineProps<{
  target: 'faculties' | 'departments' | 'teachers' | 'groups'
}>()

const emit = defineEmits<{
  close: []
  synced: []
}>()

const isTeachers = computed(() => props.target === 'teachers')
const isGroups = computed(() => props.target === 'groups')
const title = computed(() =>
  props.target === 'faculties'
    ? 'Fakultetlar'
    : props.target === 'departments'
      ? 'Kafedralar'
      : props.target === 'groups'
        ? 'Guruhlar'
        : "O'qituvchilar",
)
const endpointHint = computed(() =>
  isTeachers.value
    ? 'GET /v1/data/employee-list parametrlari bo‘yicha'
    : isGroups.value
      ? 'GET /v1/data/group-list parametrlari bo‘yicha'
      : 'GET /v1/data/department-list parametrlari bo‘yicha',
)

const deptFilters = reactive({
  page: 1,
  limit: 50,
  active: '1',
  structureType: '',
  parent: undefined as number | undefined,
})

const employeeFilters = reactive({
  page: 1,
  limit: 50,
  type: 'teacher',
  department: undefined as number | undefined,
  search: '',
  employeeType: '',
  staffPosition: '',
  employeeStatus: '',
})

const groupFilters = reactive({
  page: 1,
  limit: 50,
  id: undefined as number | undefined,
  department: undefined as number | undefined,
  curriculum: undefined as number | undefined,
  specialty: undefined as number | undefined,
  educationType: '',
  educationForm: '',
})

const departments = ref<HemisDepartment[]>([])
const employees = ref<HemisEmployee[]>([])
const groups = ref<HemisGroup[]>([])
const localDepartments = ref<Array<{ id: number; name: string; hemisId?: number | null; facultyName?: string | null }>>([])
const meta = ref<Pick<
  HemisDepartmentListResponse | HemisEmployeeListResponse | HemisGroupListResponse,
  'page' | 'pageCount' | 'totalCount' | 'pageSize'
> | null>(null)
const loading = ref(false)
const syncing = ref(false)
const error = ref('')
const syncResult = ref<HemisSyncResult | null>(null)

const hasItems = computed(() => {
  if (isTeachers.value) return employees.value.length > 0
  if (isGroups.value) return groups.value.length > 0
  return departments.value.length > 0
})
const itemCount = computed(() => {
  if (isTeachers.value) return employees.value.length
  if (isGroups.value) return groups.value.length
  return departments.value.length
})
const currentPage = computed(() => {
  if (isTeachers.value) return employeeFilters.page
  if (isGroups.value) return groupFilters.page
  return deptFilters.page
})

function employeeName(item: HemisEmployee) {
  if (item.fullName) return item.fullName
  return [item.secondName, item.firstName, item.thirdName].filter(Boolean).join(' ') || '—'
}

function groupSpecialtyName(item: HemisGroup) {
  return item.specilaty?.name || item.specialty?.name || '—'
}

function localDepartmentName(item: HemisGroup) {
  const hemisId = item.department?.id
  if (!hemisId) return ''
  const local = localDepartments.value.find((d) => d.hemisId === hemisId)
  return local?.name || ''
}

async function loadLocalDepartments() {
  if (!isGroups.value) return
  try {
    const { data } = await departmentApi.list()
    localDepartments.value = Array.isArray(data) ? data : []
  } catch {
    localDepartments.value = []
  }
}

function deptQuery() {
  return {
    page: deptFilters.page || 1,
    limit: deptFilters.limit || 50,
    active: deptFilters.active || '1',
    structureType: deptFilters.structureType || undefined,
    parent: deptFilters.parent || undefined,
  }
}

function employeeQuery() {
  return {
    page: employeeFilters.page || 1,
    limit: employeeFilters.limit || 50,
    type: employeeFilters.type || 'teacher',
    department: employeeFilters.department || undefined,
    search: employeeFilters.search || undefined,
    employeeType: employeeFilters.employeeType || undefined,
    staffPosition: employeeFilters.staffPosition || undefined,
    employeeStatus: employeeFilters.employeeStatus || undefined,
  }
}

function groupQuery() {
  return {
    page: groupFilters.page || 1,
    limit: groupFilters.limit || 50,
    id: groupFilters.id || undefined,
    department: groupFilters.department || undefined,
    curriculum: groupFilters.curriculum || undefined,
    specialty: groupFilters.specialty || undefined,
    educationType: groupFilters.educationType || undefined,
    educationForm: groupFilters.educationForm || undefined,
  }
}

async function load() {
  loading.value = true
  error.value = ''
  syncResult.value = null
  try {
    if (isTeachers.value) {
      const { data } = await hemisApi.fetchEmployees(employeeQuery())
      employees.value = data.items || []
      groups.value = []
      departments.value = []
      meta.value = {
        page: data.page,
        pageCount: data.pageCount,
        totalCount: data.totalCount,
        pageSize: data.pageSize,
      }
    } else if (isGroups.value) {
      const { data } = await hemisApi.fetchGroups(groupQuery())
      groups.value = data.items || []
      employees.value = []
      departments.value = []
      meta.value = {
        page: data.page,
        pageCount: data.pageCount,
        totalCount: data.totalCount,
        pageSize: data.pageSize,
      }
    } else {
      const { data } = await hemisApi.fetchDepartments(deptQuery())
      departments.value = data.items || []
      employees.value = []
      groups.value = []
      meta.value = {
        page: data.page,
        pageCount: data.pageCount,
        totalCount: data.totalCount,
        pageSize: data.pageSize,
      }
    }
  } catch (e) {
    error.value = getErrorMessage(e)
    departments.value = []
    employees.value = []
    groups.value = []
    meta.value = null
  } finally {
    loading.value = false
  }
}

async function sync() {
  syncing.value = true
  error.value = ''
  try {
    const { data } =
      props.target === 'faculties'
        ? await hemisApi.syncFaculties(deptQuery())
        : props.target === 'departments'
          ? await hemisApi.syncDepartments(deptQuery())
          : props.target === 'groups'
            ? await hemisApi.syncGroups(groupQuery())
            : await hemisApi.syncTeachers(employeeQuery())
    syncResult.value = data
    emit('synced')
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    syncing.value = false
  }
}

onMounted(() => {
  if (isGroups.value) {
    void loadLocalDepartments()
  }
})
</script>

<style scoped>
.field {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}
:global(.dark) .field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
