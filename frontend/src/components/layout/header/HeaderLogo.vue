<template>
  <router-link to="/" class="flex items-center gap-2 lg:hidden">
    <img
      src="/images/logo/urspi-logo.png"
      alt="UrSPI"
      class="h-9 w-9 rounded-full border border-slate-200 object-cover"
    />
    <span class="text-sm font-bold text-slate-800 dark:text-white">UrSPI Yuklama tizimi</span>
  </router-link>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>
