<template>
  <AdminLayout>
    <PageBreadcrumb page-title="HEMIS integratsiya" />

    <div
      class="mx-auto max-w-2xl rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <h3 class="mb-1 font-semibold text-gray-800 dark:text-white/90">HEMIS token</h3>
      <p class="mb-4 text-sm text-gray-500">
        Admin panel tokeni lokal bazaga saqlanadi. Fakultetlar / Kafedralar sahifasidagi
        “HEMIS dan yuklash” shu token orqali
        <code class="text-xs">/v1/data/department-list</code> ga murojaat qiladi.
      </p>

      <div v-if="tokenInfo" class="mb-4 rounded-lg bg-gray-50 p-3 text-sm dark:bg-white/[0.03]">
        <p>
          Holat:
          <span :class="tokenInfo.configured ? 'text-success-600' : 'text-warning-600'">
            {{ tokenInfo.configured ? 'Saqlangan' : 'Saqlanmagan' }}
          </span>
        </p>
        <p v-if="tokenInfo.maskedToken" class="mt-1 text-gray-500">
          Token: {{ tokenInfo.maskedToken }}
        </p>
        <p class="mt-1 text-gray-500">Base URL: {{ tokenInfo.baseUrl }}</p>
      </div>

      <form class="space-y-4" @submit.prevent="saveToken">
        <div>
          <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Access token</label>
          <textarea
            v-model="form.accessToken"
            required
            rows="4"
            placeholder="Bearer token (faqat token qiymati)"
            class="w-full rounded-lg border border-gray-300 bg-transparent px-3 py-2 text-sm dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
          />
        </div>
        <div>
          <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Base URL</label>
          <input
            v-model="form.baseUrl"
            placeholder="https://student.urspi.uz/rest"
            class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
          />
        </div>
        <div v-if="error" class="text-sm text-error-600">{{ error }}</div>
        <div v-if="success" class="text-sm text-success-600">{{ success }}</div>
        <button
          type="submit"
          class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
          :disabled="saving"
        >
          {{ saving ? 'Saqlanmoqda...' : 'Tokenni saqlash' }}
        </button>
      </form>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { getErrorMessage } from '@/api/http'
import { hemisApi, type HemisTokenInfo } from '@/api/hemis'

const tokenInfo = ref<HemisTokenInfo | null>(null)
const saving = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  accessToken: '',
  baseUrl: 'https://student.urspi.uz/rest',
})

async function loadToken() {
  try {
    const { data } = await hemisApi.getToken()
    tokenInfo.value = data
    if (data.baseUrl) form.baseUrl = data.baseUrl
  } catch (e) {
    error.value = getErrorMessage(e)
  }
}

async function saveToken() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const { data } = await hemisApi.saveToken({
      accessToken: form.accessToken.trim(),
      baseUrl: form.baseUrl.trim() || undefined,
    })
    tokenInfo.value = data
    form.accessToken = ''
    success.value = 'Token lokal bazaga saqlandi'
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

onMounted(loadToken)
</script>
