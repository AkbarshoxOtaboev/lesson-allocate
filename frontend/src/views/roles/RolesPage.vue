<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Rollar va ruxsatlar" />

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-12">
      <div
        class="xl:col-span-5 rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
      >
        <div class="flex items-center justify-between border-b border-gray-200 px-5 py-4 dark:border-gray-800">
          <h3 class="font-semibold text-gray-800 dark:text-white/90">Rollar</h3>
          <button class="text-sm font-medium text-brand-500" @click="startCreate">+ Yangi</button>
        </div>

        <div v-if="error" class="px-5 py-3 text-sm text-error-600">{{ error }}</div>
        <div v-if="loading" class="px-5 py-6 text-sm text-gray-500">Yuklanmoqda...</div>

        <ul v-else class="divide-y divide-gray-200 dark:divide-gray-800">
          <li
            v-for="role in roles"
            :key="role.id"
            class="flex cursor-pointer items-center justify-between px-5 py-3 hover:bg-gray-50 dark:hover:bg-white/[0.02]"
            :class="{ 'bg-brand-50/60 dark:bg-brand-500/10': selectedRole?.id === role.id }"
            @click="selectRole(role)"
          >
            <div>
              <p class="font-medium text-gray-800 dark:text-white/90">{{ role.name }}</p>
              <p class="text-theme-xs text-gray-500">ID: {{ role.id }}</p>
            </div>
            <div class="flex gap-2" @click.stop>
              <button class="text-xs text-brand-500" @click="startEdit(role)">Tahrir</button>
              <button class="text-xs text-error-600" @click="removeRole(role)">O‘chirish</button>
            </div>
          </li>
        </ul>

        <form
          v-if="roleFormOpen"
          class="space-y-3 border-t border-gray-200 px-5 py-4 dark:border-gray-800"
          @submit.prevent="saveRole"
        >
          <input
            v-model="roleName"
            required
            placeholder="Rol nomi"
            class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
          />
          <div class="flex gap-2">
            <button
              type="submit"
              class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
              :disabled="saving"
            >
              Saqlash
            </button>
            <button
              type="button"
              class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
              @click="roleFormOpen = false"
            >
              Bekor
            </button>
          </div>
        </form>
      </div>

      <div
        class="xl:col-span-7 rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
      >
        <div class="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
          <h3 class="font-semibold text-gray-800 dark:text-white/90">
            {{ selectedRole ? `${selectedRole.name} — ruxsatlar` : 'Rolni tanlang' }}
          </h3>
        </div>

        <div v-if="!selectedRole" class="px-5 py-10 text-sm text-gray-500">
          Chapdan rolni tanlang
        </div>

        <div v-else class="max-h-[560px] space-y-2 overflow-y-auto p-4">
          <label
            v-for="perm in allPermissions"
            :key="perm.id"
            class="flex items-center justify-between rounded-lg border border-gray-100 px-3 py-2 dark:border-gray-800"
          >
            <span class="text-sm text-gray-700 dark:text-gray-300">{{ perm.name }}</span>
            <input
              type="checkbox"
              class="rounded border-gray-300"
              :checked="assignedIds.has(perm.id)"
              :disabled="permBusy === perm.id"
              @change="togglePermission(perm, ($event.target as HTMLInputElement).checked)"
            />
          </label>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { rolesApi } from '@/api/roles'
import { getErrorMessage } from '@/api/http'
import type { Permission, Role } from '@/types/api'

const roles = ref<Role[]>([])
const allPermissions = ref<Permission[]>([])
const assignedIds = ref<Set<number>>(new Set())
const selectedRole = ref<Role | null>(null)
const loading = ref(false)
const saving = ref(false)
const permBusy = ref<number | null>(null)
const error = ref('')
const roleFormOpen = ref(false)
const roleName = ref('')
const editingRoleId = ref<number | null>(null)

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

async function loadRoles() {
  loading.value = true
  error.value = ''
  try {
    const [rolesRes, permsRes] = await Promise.all([
      rolesApi.list(),
      rolesApi.getAllPermissions(),
    ])
    roles.value = unwrapList(rolesRes.data)
    allPermissions.value = unwrapList(permsRes.data)
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    loading.value = false
  }
}

async function selectRole(role: Role) {
  selectedRole.value = role
  try {
    const { data } = await rolesApi.getPermissions(role.id)
    assignedIds.value = new Set(unwrapList(data).map((p) => p.id))
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

function startCreate() {
  editingRoleId.value = null
  roleName.value = ''
  roleFormOpen.value = true
}

function startEdit(role: Role) {
  editingRoleId.value = role.id
  roleName.value = role.name
  roleFormOpen.value = true
}

async function saveRole() {
  saving.value = true
  try {
    if (editingRoleId.value) await rolesApi.update(editingRoleId.value, roleName.value)
    else await rolesApi.create(roleName.value)
    roleFormOpen.value = false
    await loadRoles()
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function removeRole(role: Role) {
  if (!confirm(`"${role.name}" o‘chirilsinmi?`)) return
  try {
    await rolesApi.remove(role.id)
    if (selectedRole.value?.id === role.id) {
      selectedRole.value = null
      assignedIds.value = new Set()
    }
    await loadRoles()
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

async function togglePermission(perm: Permission, checked: boolean) {
  if (!selectedRole.value) return
  permBusy.value = perm.id
  try {
    if (checked) await rolesApi.assignPermission(selectedRole.value.id, perm.id)
    else await rolesApi.removePermission(selectedRole.value.id, perm.id)
    const next = new Set(assignedIds.value)
    if (checked) next.add(perm.id)
    else next.delete(perm.id)
    assignedIds.value = next
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    permBusy.value = null
  }
}

onMounted(loadRoles)
</script>
