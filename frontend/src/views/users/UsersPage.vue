<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Foydalanuvchilar" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <div>
          <h3 class="font-semibold text-gray-800 dark:text-white/90">Foydalanuvchilar ro‘yxati</h3>
        </div>
        <button
          type="button"
          class="inline-flex items-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
          @click="openCreate"
        >
          Yangi foydalanuvchi
        </button>
      </div>

      <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Foydalanuvchi</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Telefon</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Rollar</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Status</th>
              <th class="px-5 py-3 text-left text-theme-xs font-medium text-gray-500">Oxirgi kirish</th>
              <th class="px-5 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(user, index) in users" :key="user.id">
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-5 py-4">
                <p class="font-medium text-gray-800 text-theme-sm dark:text-white/90">
                  {{ user.fullName || user.username }}
                </p>
                <p class="text-theme-xs text-gray-500">@{{ user.username }}</p>
              </td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">{{ user.phone || '—' }}</td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">
                {{ user.roles?.map((r) => r.name).join(', ') || '—' }}
              </td>
              <td class="px-5 py-4">
                <button
                  type="button"
                  class="rounded-full px-2 py-0.5 text-theme-xs font-medium"
                  :class="statusClass(user.status)"
                  title="Statusni o‘zgartirish"
                  @click="toggleStatus(user)"
                >
                  {{ user.status }}
                </button>
              </td>
              <td class="px-5 py-4 text-theme-sm text-gray-500">
                {{ formatDate(user.lastLogin) }}
              </td>
              <td class="px-5 py-4">
                <div class="flex justify-end gap-2">
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-warning-500 hover:bg-warning-50 dark:hover:bg-warning-500/10"
                    title="Tahrirlash"
                    @click="openEdit(user)"
                  >
                    <PencilAltIcon class="size-5" />
                  </button>
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-error-600 hover:bg-error-50 dark:hover:bg-error-500/10"
                    title="O‘chirish"
                    @click="removeUser(user)"
                  >
                    <TrashIcon class="size-5" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!users.length">
              <td colspan="7" class="px-5 py-8 text-center text-sm text-gray-500">
                Maʼlumot topilmadi
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div
          class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingId ? 'Foydalanuvchini tahrirlash' : 'Yangi foydalanuvchi' }}
          </h3>
          <form class="space-y-4" @submit.prevent="saveUser">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Username</label>
              <input
                v-model="form.username"
                required
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                Parol {{ editingId ? '(ixtiyoriy)' : '' }}
              </label>
              <input
                v-model="form.password"
                type="password"
                :required="!editingId"
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">To‘liq ism</label>
              <input
                v-model="form.fullName"
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Telefon</label>
              <input
                v-model="form.phone"
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Rollar</label>
              <div class="max-h-40 space-y-2 overflow-y-auto rounded-lg border border-gray-200 p-3 dark:border-gray-700">
                <label
                  v-for="role in roles"
                  :key="role.id"
                  class="flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300"
                >
                  <input
                    v-model="form.roleIds"
                    type="checkbox"
                    :value="role.id"
                    class="rounded border-gray-300"
                  />
                  {{ role.name }}
                </label>
              </div>
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Profil rasmi</label>
              <input type="file" accept="image/*" @change="onFileChange" />
            </div>
            <div v-if="formError" class="text-sm text-error-600">{{ formError }}</div>
            <div class="flex justify-end gap-2 pt-2">
              <button
                type="button"
                class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
                @click="modalOpen = false"
              >
                Bekor
              </button>
              <button
                type="submit"
                class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
                :disabled="saving"
              >
                {{ saving ? 'Saqlanmoqda...' : 'Saqlash' }}
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import { usersApi } from '@/api/users'
import { rolesApi } from '@/api/roles'
import { getErrorMessage } from '@/api/http'
import { PencilAltIcon, TrashIcon } from '@/icons'
import type { Role, User } from '@/types/api'

const users = ref<User[]>([])
const roles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const formError = ref('')
const modalOpen = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  username: '',
  password: '',
  fullName: '',
  phone: '',
  roleIds: [] as number[],
  profileImage: null as File | null,
})

function statusClass(status?: string) {
  if (status === 'ACTIVE') return 'bg-success-50 text-success-700 dark:bg-success-500/15 dark:text-success-500'
  if (status === 'DISABLED') return 'bg-warning-50 text-warning-700 dark:bg-warning-500/15 dark:text-warning-400'
  return 'bg-gray-100 text-gray-600 dark:bg-white/5 dark:text-gray-400'
}

function formatDate(value?: string | null) {
  if (!value) return '—'
  return new Date(value).toLocaleString()
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.fullName = ''
  form.phone = ''
  form.roleIds = []
  form.profileImage = null
  formError.value = ''
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(user: User) {
  editingId.value = user.id
  form.username = user.username
  form.password = ''
  form.fullName = user.fullName || ''
  form.phone = user.phone || ''
  form.roleIds = user.roles?.map((r) => r.id) || []
  form.profileImage = null
  formError.value = ''
  modalOpen.value = true
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  form.profileImage = input.files?.[0] || null
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [usersRes, rolesRes] = await Promise.all([usersApi.list(), rolesApi.list()])
    users.value = unwrapList(usersRes.data)
    roles.value = unwrapList(rolesRes.data)
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    loading.value = false
  }
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

async function saveUser() {
  saving.value = true
  formError.value = ''
  try {
    const payload = {
      username: form.username,
      password: form.password || undefined,
      fullName: form.fullName,
      phone: form.phone,
      roleIds: form.roleIds,
      profileImage: form.profileImage,
    }
    if (editingId.value) await usersApi.update(editingId.value, payload)
    else await usersApi.create(payload)
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function toggleStatus(user: User) {
  try {
    await usersApi.changeStatus(user.id)
    await load()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

async function removeUser(user: User) {
  if (!confirm(`"${user.username}" o‘chirilsinmi? (soft delete)`)) return
  try {
    await usersApi.remove(user.id)
    await load()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

onMounted(load)
</script>
