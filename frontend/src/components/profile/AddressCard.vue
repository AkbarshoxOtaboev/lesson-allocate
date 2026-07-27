<template>
  <div>
    <div class="p-5 border border-gray-200 rounded-2xl dark:border-gray-800 lg:p-6">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h4 class="text-lg font-semibold text-gray-800 dark:text-white/90 lg:mb-6">
            {{ t('profile.addressTitle') }}
          </h4>

          <div class="grid grid-cols-1 gap-4 lg:grid-cols-2 lg:gap-7 2xl:gap-x-32">
            <div>
              <p class="mb-2 text-xs leading-normal text-gray-500 dark:text-gray-400">
                {{ t('profile.country') }}
              </p>
              <p class="text-sm font-medium text-gray-800 dark:text-white/90">
                {{ auth.user?.country || '—' }}
              </p>
            </div>

            <div>
              <p class="mb-2 text-xs leading-normal text-gray-500 dark:text-gray-400">
                {{ t('profile.cityRegion') }}
              </p>
              <p class="text-sm font-medium text-gray-800 dark:text-white/90">
                {{ cityRegion || '—' }}
              </p>
            </div>

            <div>
              <p class="mb-2 text-xs leading-normal text-gray-500 dark:text-gray-400">
                {{ t('profile.postalCode') }}
              </p>
              <p class="text-sm font-medium text-gray-800 dark:text-white/90">
                {{ auth.user?.postalCode || '—' }}
              </p>
            </div>

            <div>
              <p class="mb-2 text-xs leading-normal text-gray-500 dark:text-gray-400">
                {{ t('profile.taxId') }}
              </p>
              <p class="text-sm font-medium text-gray-800 dark:text-white/90">
                {{ auth.user?.taxId || '—' }}
              </p>
            </div>
          </div>
        </div>

        <button
          type="button"
          class="flex w-full items-center justify-center gap-2 rounded-full border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 shadow-theme-xs hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200 lg:inline-flex lg:w-auto"
          @click="openModal"
        >
          {{ t('common.edit') }}
        </button>
      </div>
      <p v-if="error" class="mt-3 text-sm text-error-600">{{ error }}</p>
      <p v-if="success" class="mt-3 text-sm text-success-600">{{ success }}</p>
    </div>

    <Modal v-if="open" @close="open = false">
      <template #body>
        <div
          class="no-scrollbar relative w-full max-w-[700px] overflow-y-auto rounded-3xl bg-white p-4 dark:bg-gray-900 lg:p-11"
        >
          <button
            type="button"
            class="transition-color absolute right-5 top-5 z-999 flex h-11 w-11 items-center justify-center rounded-full bg-gray-100 text-gray-400 hover:bg-gray-200 hover:text-gray-600 dark:bg-gray-700 dark:bg-white/[0.05] dark:text-gray-400 dark:hover:bg-white/[0.07] dark:hover:text-gray-300"
            @click="open = false"
          >
            ✕
          </button>
          <div class="px-2 pr-14">
            <h4 class="mb-2 text-2xl font-semibold text-gray-800 dark:text-white/90">
              {{ t('profile.editAddress') }}
            </h4>
            <p class="mb-6 text-sm text-gray-500 dark:text-gray-400 lg:mb-7">
              {{ t('profile.editHint') }}
            </p>
          </div>
          <form class="flex flex-col" @submit.prevent="save">
            <div class="grid grid-cols-1 gap-x-6 gap-y-5 px-2 lg:grid-cols-2">
              <div>
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                  {{ t('profile.country') }}
                </label>
                <input
                  v-model="form.country"
                  type="text"
                  class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                />
              </div>
              <div>
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                  {{ t('profile.city') }}
                </label>
                <input
                  v-model="form.city"
                  type="text"
                  class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                />
              </div>
              <div>
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                  {{ t('profile.region') }}
                </label>
                <input
                  v-model="form.region"
                  type="text"
                  class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                />
              </div>
              <div>
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                  {{ t('profile.postalCode') }}
                </label>
                <input
                  v-model="form.postalCode"
                  type="text"
                  class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                />
              </div>
              <div class="lg:col-span-2">
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-400">
                  {{ t('profile.taxId') }}
                </label>
                <input
                  v-model="form.taxId"
                  type="text"
                  class="dark:bg-dark-900 h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:focus:border-brand-800"
                />
              </div>
            </div>
            <div class="mt-6 flex items-center gap-3 lg:justify-end">
              <button
                type="button"
                class="flex w-full justify-center rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] sm:w-auto"
                @click="open = false"
              >
                {{ t('common.close') }}
              </button>
              <button
                type="submit"
                :disabled="saving"
                class="flex w-full justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60 sm:w-auto"
              >
                {{ saving ? t('common.saving') : t('common.save') }}
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Modal from './Modal.vue'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage } from '@/api/http'

const { t } = useI18n()
const auth = useAuthStore()
const open = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  country: '',
  city: '',
  region: '',
  postalCode: '',
  taxId: '',
})

const cityRegion = computed(() => {
  const parts = [auth.user?.city, auth.user?.region].filter(Boolean)
  return parts.join(', ')
})

function openModal() {
  form.country = auth.user?.country || ''
  form.city = auth.user?.city || ''
  form.region = auth.user?.region || ''
  form.postalCode = auth.user?.postalCode || ''
  form.taxId = auth.user?.taxId || ''
  error.value = ''
  success.value = ''
  open.value = true
}

async function save() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    await auth.updateProfile({
      country: form.country.trim() || null,
      city: form.city.trim() || null,
      region: form.region.trim() || null,
      postalCode: form.postalCode.trim() || null,
      taxId: form.taxId.trim() || null,
    })
    open.value = false
    success.value = t('profile.saved')
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}
</script>
