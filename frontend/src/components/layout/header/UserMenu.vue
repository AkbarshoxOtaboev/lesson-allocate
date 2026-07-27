<template>
  <div class="relative" ref="dropdownRef">
    <button
      class="flex items-center text-gray-700 dark:text-gray-400"
      @click.prevent="toggleDropdown"
    >
      <span
        class="mr-3 flex h-11 w-11 items-center justify-center overflow-hidden rounded-full bg-brand-500 text-sm font-semibold text-white"
      >
        {{ initials }}
      </span>

      <span class="block mr-1 font-medium text-theme-sm">{{ auth.displayName }}</span>
      <ChevronDownIcon :class="{ 'rotate-180': dropdownOpen }" />
    </button>

    <div
      v-if="dropdownOpen"
      class="absolute right-0 mt-[17px] flex w-[260px] flex-col rounded-2xl border border-gray-200 bg-white p-3 shadow-theme-lg dark:border-gray-800 dark:bg-gray-dark"
    >
      <div>
        <span class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400">
          {{ auth.displayName }}
        </span>
        <span class="mt-0.5 block text-theme-xs text-gray-500 dark:text-gray-400">
          {{ auth.user?.username || '—' }}
        </span>
      </div>

      <ul class="flex flex-col gap-1 pt-4 pb-3 border-b border-gray-200 dark:border-gray-800">
        <li>
          <router-link
            to="/profile"
            class="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5"
            @click="closeDropdown"
          >
            <UserCircleIcon class="text-gray-500" />
            {{ t('header.profile') }}
          </router-link>
        </li>
        <li>
          <router-link
            to="/settings"
            class="flex items-center gap-3 px-3 py-2 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5"
            @click="closeDropdown"
          >
            <SettingsIcon class="text-gray-500" />
            {{ t('header.settings') }}
          </router-link>
        </li>
      </ul>

      <button
        type="button"
        class="flex items-center gap-3 px-3 py-2 mt-3 font-medium text-gray-700 rounded-lg group text-theme-sm hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-white/5"
        @click="signOut"
      >
        <LogoutIcon class="text-gray-500" />
        {{ t('header.logout') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { UserCircleIcon, ChevronDownIcon, LogoutIcon, SettingsIcon } from '@/icons'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const auth = useAuthStore()
const router = useRouter()
const dropdownOpen = ref(false)
const dropdownRef = ref(null)

const initials = computed(() => {
  const name = auth.displayName || 'U'
  return name.slice(0, 1).toUpperCase()
})

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}

const closeDropdown = () => {
  dropdownOpen.value = false
}

const signOut = async () => {
  closeDropdown()
  await auth.logout()
  await router.push({ name: 'Signin' })
}

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
