<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Parolni o'zgartirish" />

    <div class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] lg:p-6">
      <div class="max-w-xl">
        <h3 class="mb-2 text-lg font-semibold text-gray-800 dark:text-white/90">
          Parolni o'zgartirish
        </h3>
        <p class="mb-6 text-sm text-gray-500 dark:text-gray-400">
          Eski parolni kiriting, so'ng yangi parolni ikki marta tasdiqlang.
        </p>

        <form class="space-y-4" @submit.prevent="submit">
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Eski parol</label>
            <input
              v-model="form.oldPassword"
              type="password"
              class="form-field"
              autocomplete="current-password"
              required
            />
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">Yangi parol</label>
            <input
              v-model="form.newPassword"
              type="password"
              class="form-field"
              autocomplete="new-password"
              minlength="8"
              required
            />
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">
              Yangi parolni qayta kiriting
            </label>
            <input
              v-model="form.confirmPassword"
              type="password"
              class="form-field"
              autocomplete="new-password"
              minlength="8"
              required
            />
          </div>

          <p v-if="error" class="text-sm text-error-600">{{ error }}</p>

          <div class="pt-2">
            <button
              type="submit"
              class="rounded-xl bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
              :disabled="saving"
            >
              {{ saving ? "Saqlanmoqda..." : "Tasdiqlash" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import { authApi } from '@/api/auth'
import { getErrorMessage } from '@/api/http'
import { showSuccess } from '@/utils/swal'

const router = useRouter()
const saving = ref(false)
const error = ref('')
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function submit() {
  error.value = ''
  if (form.newPassword !== form.confirmPassword) {
    error.value = 'Yangi parollar bir xil emas'
    return
  }
  saving.value = true
  try {
    await authApi.changePassword({ ...form })
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    await showSuccess("Parol muvaffaqiyatli yangilandi")
    await router.push({ name: 'Profile' })
  } catch (e) {
    error.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-field {
  height: 2.75rem;
  width: 100%;
  border-radius: 0.75rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.875rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}

:global(.dark) .form-field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
