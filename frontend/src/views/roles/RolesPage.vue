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
            <Shield :stroke-width="1.75" class="h-5 w-5" />
          </div>
          <div>
            <h2 class="text-lg font-bold text-slate-900 dark:text-white">
              Rollar va Huquqlar
              <span class="font-semibold text-slate-500">(Permissions)</span>
            </h2>
            <p class="mt-1 max-w-xl text-sm text-slate-500 dark:text-slate-400">
              Har bir rol uchun asosiy ruxsatlarni ko‘ring va tahrirlang. Ruxsatlar tizim
              bo‘ylab amallar uchun ishlatiladi.
            </p>
          </div>
        </div>
        <button
          type="button"
          class="inline-flex shrink-0 items-center justify-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-[0_8px_20px_rgba(79,70,229,0.28)] transition hover:bg-indigo-700"
          @click="startCreate"
        >
          <Plus class="h-4 w-4" :stroke-width="2.25" />
          Yangi rol yaratish
        </button>
      </div>

      <div v-if="loading" class="py-16 text-center text-sm text-slate-500">Yuklanmoqda...</div>

      <div v-else class="mt-6 grid grid-cols-1 gap-4 xl:grid-cols-2">
        <article
          v-for="card in roleCards"
          :key="card.role.id"
          class="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 transition hover:border-indigo-200 hover:shadow-sm dark:border-slate-600 dark:bg-slate-800/60"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex min-w-0 items-start gap-3">
              <div
                class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl"
                :class="card.theme.iconWrap"
              >
                <Shield class="h-5 w-5" :class="card.theme.icon" :stroke-width="1.75" />
              </div>
              <div class="min-w-0">
                <h3 class="truncate font-bold text-slate-900 dark:text-white">
                  {{ displayRoleTitle(card.role.name) }}
                </h3>
                <p class="mt-0.5 text-sm text-slate-500">
                  {{ card.userCount }} ta foydalanuvchi
                </p>
              </div>
            </div>
            <span
              class="shrink-0 rounded-lg border border-slate-200 px-2.5 py-1 text-[11px] font-semibold text-slate-600 dark:border-slate-600 dark:text-slate-300"
            >
              {{ card.scope }}
            </span>
          </div>

          <div class="mt-4">
            <p class="text-[11px] font-bold uppercase tracking-[0.12em] text-slate-400">
              Asosiy ruxsatlar:
            </p>
            <div class="mt-2.5 flex flex-wrap gap-2">
              <span
                v-for="perm in card.previewPerms"
                :key="perm.id"
                class="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-slate-50 px-2.5 py-1 text-xs font-medium text-slate-700 dark:border-slate-600 dark:bg-slate-700/40 dark:text-slate-200"
              >
                <Check class="h-3 w-3 text-emerald-500" :stroke-width="2.5" />
                {{ perm.labelUz || perm.name }}
              </span>
              <span
                v-if="!card.previewPerms.length"
                class="text-xs text-slate-400"
              >
                Ruxsat biriktirilmagan
              </span>
              <span
                v-else-if="card.extraCount > 0"
                class="inline-flex items-center rounded-full border border-dashed border-slate-300 px-2.5 py-1 text-xs font-medium text-slate-500"
              >
                +{{ card.extraCount }}
              </span>
            </div>
          </div>

          <div class="mt-5 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-700">
            <div class="flex gap-1">
              <button
                type="button"
                class="rounded-lg px-2 py-1.5 text-xs font-medium text-slate-500 hover:bg-slate-50 hover:text-slate-700 dark:hover:bg-white/5"
                @click="startEdit(card.role)"
              >
                Nomini o‘zgartirish
              </button>
              <button
                type="button"
                class="rounded-lg px-2 py-1.5 text-xs font-medium text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-500/10"
                @click="removeRole(card.role)"
              >
                O‘chirish
              </button>
            </div>
            <button
              type="button"
              class="text-sm font-semibold text-indigo-600 hover:text-indigo-700 dark:text-indigo-300"
              @click="openPermissions(card.role)"
            >
              Huquqlarni tahrirlash →
            </button>
          </div>
        </article>
      </div>
    </div>

    <!-- Create / rename role -->
    <Modal v-if="roleFormOpen" full-screen-backdrop @close="roleFormOpen = false">
      <template #body>
        <div
          class="relative w-full max-w-md rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingRoleId ? 'Rolni tahrirlash' : 'Yangi rol yaratish' }}
          </h3>
          <form class="space-y-4" @submit.prevent="saveRole">
            <div>
              <label class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
                Rol nomi
              </label>
              <input
                v-model="roleName"
                required
                placeholder="Masalan: DEKAN"
                class="h-11 w-full rounded-xl border border-slate-200 bg-white px-4 text-sm text-slate-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/15 dark:border-slate-600 dark:bg-slate-900 dark:text-white"
              />
            </div>
            <div class="flex justify-end gap-2">
              <button
                type="button"
                class="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-700 dark:border-slate-600 dark:text-slate-300"
                @click="roleFormOpen = false"
              >
                Bekor
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-60"
              >
                Saqlash
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>

    <!-- Permissions editor -->
    <Drawer v-if="permDrawerOpen && selectedRole" @close="permDrawerOpen = false">
      <template #title>
        {{ selectedRole.name }} — ruxsatlar
      </template>
      <div class="space-y-2">
        <label
          v-for="perm in allPermissions"
          :key="perm.id"
          class="flex cursor-pointer items-center justify-between gap-3 rounded-xl border border-slate-100 px-3 py-2.5 hover:bg-slate-50 dark:border-slate-700 dark:hover:bg-white/5"
        >
          <span class="flex min-w-0 flex-col">
            <span class="text-sm font-medium text-slate-800 dark:text-slate-200">
              {{ perm.labelUz || perm.name }}
            </span>
            <span class="truncate text-xs text-slate-400">{{ perm.name }}</span>
          </span>
          <input
            type="checkbox"
            class="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
            :checked="assignedIds.has(perm.id)"
            :disabled="permBusy === perm.id"
            @change="togglePermission(perm, ($event.target as HTMLInputElement).checked)"
          />
        </label>
      </div>
    </Drawer>
  </ManagementLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Check, Plus, Shield } from 'lucide-vue-next'
import ManagementLayout from '@/components/layout/ManagementLayout.vue'
import Modal from '@/components/ui/Modal.vue'
import Drawer from '@/components/ui/Drawer.vue'
import { rolesApi } from '@/api/roles'
import { usersApi } from '@/api/users'
import { getErrorMessage } from '@/api/http'
import { confirmAction, showError } from '@/utils/swal'
import type { Permission, Role, User } from '@/types/api'

const PREVIEW_LIMIT = 6

const roles = ref<Role[]>([])
const allPermissions = ref<Permission[]>([])
const permissionsByRole = ref<Record<number, Permission[]>>({})
const users = ref<User[]>([])
const selectedRole = ref<Role | null>(null)
const assignedIds = ref<Set<number>>(new Set())
const loading = ref(false)
const saving = ref(false)
const permBusy = ref<number | null>(null)
const roleFormOpen = ref(false)
const permDrawerOpen = ref(false)
const roleName = ref('')
const editingRoleId = ref<number | null>(null)

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function roleTheme(name: string) {
  const n = name.toUpperCase()
  if (n.includes('SUPER') || n === 'ADMIN') {
    return {
      iconWrap: 'bg-rose-50 text-rose-600 dark:bg-rose-500/15 dark:text-rose-300',
      icon: 'text-rose-600 dark:text-rose-300',
    }
  }
  if (n.includes('DEKAN')) {
    return {
      iconWrap: 'bg-blue-50 text-blue-600 dark:bg-blue-500/15 dark:text-blue-300',
      icon: 'text-blue-600 dark:text-blue-300',
    }
  }
  if (n.includes('KAFEDRA')) {
    return {
      iconWrap: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-500/15 dark:text-emerald-300',
      icon: 'text-emerald-600 dark:text-emerald-300',
    }
  }
  if (n.includes('O‘QUV') || n.includes("O'QUV") || n.includes('ACADEMIC')) {
    return {
      iconWrap: 'bg-orange-50 text-orange-600 dark:bg-orange-500/15 dark:text-orange-300',
      icon: 'text-orange-600 dark:text-orange-300',
    }
  }
  return {
    iconWrap: 'bg-indigo-50 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-300',
    icon: 'text-indigo-600 dark:text-indigo-300',
  }
}

function roleScope(name: string) {
  const n = name.toUpperCase()
  if (n.includes('SUPER') || n === 'ADMIN') return 'Barcha huquqlar'
  if (n.includes('DEKAN')) return 'Fakultet doirasi'
  if (n.includes('KAFEDRA')) return 'Kafedra doirasi'
  return 'Cheklangan'
}

function displayRoleTitle(name: string) {
  const map: Record<string, string> = {
    ADMIN: 'Admin (Tizim administratori)',
    SUPER_ADMIN: 'Super Admin (Tizim administratori)',
    DEKAN: 'Dekan',
    KAFEDRA: "Kafedra mudiri",
  }
  return map[name.toUpperCase()] || name
}

const roleCards = computed(() =>
  roles.value.map((role) => {
    const perms = permissionsByRole.value[role.id] || []
    const previewPerms = perms.slice(0, PREVIEW_LIMIT)
    const userCount = users.value.filter((u) =>
      u.roles?.some((r) => r.id === role.id || r.name === role.name),
    ).length
    return {
      role,
      theme: roleTheme(role.name),
      scope: roleScope(role.name),
      previewPerms,
      extraCount: Math.max(0, perms.length - PREVIEW_LIMIT),
      userCount,
    }
  }),
)

async function loadRoles() {
  loading.value = true
  try {
    const [rolesRes, permsRes, usersRes] = await Promise.all([
      rolesApi.list(),
      rolesApi.getAllPermissions(),
      usersApi.list().catch(() => ({ data: [] as User[] })),
    ])
    roles.value = unwrapList(rolesRes.data)
    allPermissions.value = unwrapList(permsRes.data)
    users.value = unwrapList(usersRes.data)

    const entries = await Promise.all(
      roles.value.map(async (role) => {
        try {
          const { data } = await rolesApi.getPermissions(role.id)
          return [role.id, unwrapList(data)] as const
        } catch {
          return [role.id, []] as const
        }
      }),
    )
    permissionsByRole.value = Object.fromEntries(entries)
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    loading.value = false
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
    showError(getErrorMessage(e))
  } finally {
    saving.value = false
  }
}

async function removeRole(role: Role) {
  const ok = await confirmAction(`"${role.name}" o‘chirilsinmi?`, 'O‘chirish')
  if (!ok) return
  try {
    await rolesApi.remove(role.id)
    if (selectedRole.value?.id === role.id) {
      selectedRole.value = null
      assignedIds.value = new Set()
      permDrawerOpen.value = false
    }
    await loadRoles()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

async function openPermissions(role: Role) {
  selectedRole.value = role
  permDrawerOpen.value = true
  try {
    const { data } = await rolesApi.getPermissions(role.id)
    const list = unwrapList(data)
    assignedIds.value = new Set(list.map((p) => p.id))
    permissionsByRole.value = { ...permissionsByRole.value, [role.id]: list }
  } catch (e) {
    showError(getErrorMessage(e))
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

    const roleId = selectedRole.value.id
    const current = permissionsByRole.value[roleId] || []
    permissionsByRole.value = {
      ...permissionsByRole.value,
      [roleId]: checked
        ? [...current.filter((p) => p.id !== perm.id), perm]
        : current.filter((p) => p.id !== perm.id),
    }
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    permBusy.value = null
  }
}

onMounted(loadRoles)
</script>
