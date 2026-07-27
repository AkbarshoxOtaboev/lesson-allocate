<template>
  <div class="p-5 mb-6 border border-gray-200 rounded-2xl dark:border-gray-800 lg:p-6">
    <div class="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
      <div class="flex flex-col items-center w-full gap-6 xl:flex-row">
        <div
          class="flex h-20 w-20 items-center justify-center overflow-hidden rounded-full border border-gray-200 bg-brand-500 text-2xl font-semibold text-white dark:border-gray-800"
        >
          <img
            v-if="auth.user?.profileImage"
            :src="auth.user.profileImage"
            alt="user"
            class="h-full w-full object-cover"
          />
          <span v-else>{{ initials }}</span>
        </div>
        <div class="order-3 xl:order-2">
          <h4
            class="mb-2 text-lg font-semibold text-center text-gray-800 dark:text-white/90 xl:text-left"
          >
            {{ auth.displayName }}
          </h4>
          <div
            class="flex flex-col items-center gap-1 text-center xl:flex-row xl:gap-3 xl:text-left"
          >
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ roleLabel }}
            </p>
            <div
              v-if="locationLabel"
              class="hidden h-3.5 w-px bg-gray-300 dark:bg-gray-700 xl:block"
            ></div>
            <p v-if="locationLabel" class="text-sm text-gray-500 dark:text-gray-400">
              {{ locationLabel }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const initials = computed(() => (auth.displayName || 'U').slice(0, 1).toUpperCase())

const roleLabel = computed(() => {
  const roles = auth.user?.roles?.map((r) => r.name).filter(Boolean) || []
  return roles.length ? roles.join(', ') : '—'
})

const locationLabel = computed(() => {
  const parts = [auth.user?.city, auth.user?.region, auth.user?.country].filter(Boolean)
  return parts.length ? parts.join(', ') : ''
})
</script>
