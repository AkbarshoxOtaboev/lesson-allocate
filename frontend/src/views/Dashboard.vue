<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Bosh sahifa" />

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4 md:gap-6">
      <router-link
        v-for="card in cards"
        :key="card.to"
        :to="card.to"
        class="rounded-2xl border border-gray-200 bg-white p-5 transition hover:border-brand-300 dark:border-gray-800 dark:bg-white/[0.03] dark:hover:border-brand-500/40"
      >
        <p class="text-sm text-gray-500 dark:text-gray-400">{{ card.label }}</p>
        <p class="mt-2 text-lg font-semibold text-gray-800 dark:text-white/90">
          {{ card.title }}
        </p>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ card.hint }}</p>
      </router-link>
    </div>

    <div
      class="mt-6 rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <h3 class="mb-2 text-lg font-semibold text-gray-800 dark:text-white/90">
        Xush kelibsiz, {{ auth.displayName }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        URSPI ichki boshqaruv tizimi: foydalanuvchilar, rollar, fakultet–kafedra–guruh
        tuzilmasi, o‘quv maʼlumotnomalari va audit jurnali.
      </p>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const cards = [
  { to: '/users', label: 'RBAC', title: 'Foydalanuvchilar', hint: 'USER_* ruxsatlari' },
  { to: '/roles', label: 'RBAC', title: 'Rollar va ruxsatlar', hint: 'ROLE_* ruxsatlari' },
  { to: '/faculties', label: 'Tuzilma', title: 'Fakultetlar', hint: 'Tashkiliy tuzilma' },
  { to: '/audit', label: 'Xavfsizlik', title: 'Audit log', hint: 'SUPER_ADMIN' },
]
</script>
