<template>
  <admin-layout>
    <PageBreadcrumb :pageTitle="t('profile.title')" />

    <div
      class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6"
    >
      <h3 class="mb-5 text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-7">
        {{ t('profile.title') }}
      </h3>
      <p v-if="loading" class="mb-4 text-sm text-gray-500">...</p>
      <profile-card />
      <personal-info-card />
      <address-card />
    </div>
  </admin-layout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AdminLayout from '../../components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import ProfileCard from '../../components/profile/ProfileCard.vue'
import PersonalInfoCard from '../../components/profile/PersonalInfoCard.vue'
import AddressCard from '../../components/profile/AddressCard.vue'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage } from '@/api/http'
import { showError } from '@/utils/swal'

const { t } = useI18n()
const auth = useAuthStore()
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    await auth.fetchProfile()
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    loading.value = false
  }
})
</script>
