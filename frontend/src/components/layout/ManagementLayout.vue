<template>
  <AdminLayout>
    <div class="space-y-5">
      <!-- Top banner -->
      <header
        class="flex flex-col gap-4 rounded-2xl border border-slate-100 bg-white p-5 shadow-sm sm:flex-row sm:items-center sm:justify-between dark:border-slate-700 dark:bg-slate-800"
      >
        <div class="flex items-start gap-4">
          <div
            class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-300"
          >
            <Settings :stroke-width="1.75" class="h-6 w-6" />
          </div>
          <div>
            <h1 class="text-lg font-bold text-slate-900 dark:text-white sm:text-xl">
              Tizim sozlamalari va ma'lumotnomalar
            </h1>
            <p class="mt-1 max-w-2xl text-sm text-slate-500 dark:text-slate-400">
              Foydalanuvchilar, o'quv yillari, rollar hamda HEMIS integratsiyasi sozlamalarini
              boshqarish paneli.
            </p>
          </div>
        </div>
        <div
          class="inline-flex items-center gap-2 self-start rounded-full bg-emerald-50 px-3.5 py-1.5 text-xs font-semibold text-emerald-700 ring-1 ring-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-300 dark:ring-emerald-500/20 sm:self-center"
        >
          <span class="h-1.5 w-1.5 rounded-full bg-emerald-500" />
          Tizim barqaror ishlamoqda
        </div>
      </header>

      <div class="flex flex-col gap-5 lg:flex-row lg:items-start">
        <!-- Settings side nav -->
        <aside
          class="w-full shrink-0 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800 lg:w-[240px]"
        >
          <p
            class="mb-3 px-2 text-[11px] font-bold uppercase tracking-[0.14em] text-slate-400"
          >
            Sozlamalar bo'limi
          </p>
          <nav class="space-y-1">
            <router-link
              v-for="item in navItems"
              :key="item.to"
              :to="item.to"
              class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors"
              :class="
                isActive(item.to)
                  ? 'bg-indigo-50 text-indigo-700 dark:bg-indigo-500/15 dark:text-indigo-300'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-white/5 dark:hover:text-white'
              "
            >
              <component
                :is="item.icon"
                class="h-[18px] w-[18px] shrink-0"
                :class="isActive(item.to) ? 'text-indigo-600 dark:text-indigo-300' : 'text-slate-400'"
                :stroke-width="1.75"
              />
              <span class="flex-1 truncate">{{ item.label }}</span>
              <span
                v-if="isActive(item.to)"
                class="h-1.5 w-1.5 rounded-full bg-indigo-600 shadow-sm"
              />
            </router-link>
          </nav>
        </aside>

        <!-- Page content -->
        <div class="min-w-0 flex-1">
          <slot />
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  BookOpen,
  Building2,
  CalendarDays,
  ClipboardList,
  GraduationCap,
  Link2,
  Map,
  Settings,
  Shield,
  Users,
  UsersRound,
} from 'lucide-vue-next'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()

const allNavItems = [
  { to: '/users', label: 'Foydalanuvchilar', icon: Users },
  { to: '/academic-years', label: "O'quv yili", icon: CalendarDays },
  { to: '/faculties', label: 'Fakultetlar', icon: Building2 },
  { to: '/departments', label: 'Kafedralar', icon: BookOpen },
  { to: '/directions', label: "Yo'nalishlar", icon: Map },
  { to: '/groups', label: 'Guruhlar', icon: UsersRound },
  { to: '/teachers', label: "O'qituvchilar (katalog)", icon: GraduationCap },
  { to: '/roles', label: 'Rollar', icon: Shield },
  { to: '/audit', label: 'Audit log', icon: ClipboardList, superOnly: true },
  { to: '/hemis', label: 'HEMIS', icon: Link2 },
]

const navItems = computed(() =>
  allNavItems.filter((item) => !item.superOnly || auth.isSuperAdmin),
)

function isActive(path: string) {
  return route.path === path
}
</script>
