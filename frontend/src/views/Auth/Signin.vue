<template>
  <FullScreenLayout>
    <div class="flex min-h-screen w-full bg-white font-sans">
      <!-- Left branding panel -->
      <section
        class="relative hidden min-h-screen w-1/2 flex-col items-center justify-center overflow-hidden lg:flex"
      >
        <img
          src="/images/auth/login-bg.jpg"
          alt=""
          class="absolute inset-0 h-full w-full object-cover"
          aria-hidden="true"
        />
        <div class="absolute inset-0 bg-slate-900/45" aria-hidden="true" />

        <div
          class="relative z-10 mx-8 max-w-lg rounded-2xl bg-slate-900/55 px-8 py-10 text-center shadow-2xl backdrop-blur-md ring-1 ring-white/10"
        >
          <h1 class="text-3xl font-bold leading-tight tracking-tight xl:text-4xl">
            <span class="text-white">Dars Yuklamasini</span>
            <br />
            <span class="text-[#A5B4FC]">Boshqarish Tizimi</span>
          </h1>
          <p class="mt-5 text-sm leading-relaxed text-white/90 xl:text-base">
            O'qituvchilar, kafedra mudirlari va dekanat uchun mo'ljallangan zamonaviy, tezkor va
            qulay platforma. Ta'lim jarayonlarini oson boshqaring.
          </p>
        </div>

        <div
          class="absolute bottom-8 left-1/2 z-10 -translate-x-1/2 rounded-full bg-slate-900/60 px-5 py-2 text-xs font-medium text-white/90 backdrop-blur-sm ring-1 ring-white/10"
        >
          © {{ year }} Urganch Davlat Pedagogika Instituti
        </div>
      </section>

      <!-- Right login panel -->
      <section class="flex min-h-screen w-full flex-col items-center justify-center bg-white px-4 py-10 lg:w-1/2">
        <div
          class="relative w-full max-w-[400px] overflow-hidden rounded-2xl bg-white px-8 pb-9 pt-8 shadow-[0_12px_40px_rgba(15,23,42,0.12)] ring-1 ring-slate-100"
        >
          <div
            class="absolute inset-x-0 top-0 h-[3px] bg-gradient-to-r from-blue-500 via-indigo-500 to-violet-500"
            aria-hidden="true"
          />

          <div class="flex flex-col items-center text-center">
            <img
              src="/images/auth/logo.jpg"
              alt="URSPI"
              class="h-[92px] w-[92px] rounded-full border border-slate-200 object-cover shadow-sm"
            />
            <h2 class="mt-5 text-2xl font-bold tracking-tight text-slate-900">Xush kelibsiz</h2>
            <p class="mt-1.5 text-sm text-slate-500">Davom etish uchun tizimga kiring</p>
          </div>

          <form class="mt-8 space-y-5" @submit.prevent="handleSubmit">
            <div class="space-y-1.5 text-left">
              <label
                for="username"
                class="block text-[11px] font-semibold uppercase tracking-[0.14em] text-slate-400"
              >
                Login
              </label>
              <div class="relative">
                <User
                  class="pointer-events-none absolute left-3.5 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-slate-400"
                  :stroke-width="1.75"
                />
                <input
                  id="username"
                  v-model="username"
                  type="text"
                  autocomplete="username"
                  required
                  placeholder="Loginni kiriting"
                  class="h-12 w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-11 pr-3.5 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15"
                />
              </div>
            </div>

            <div class="space-y-1.5 text-left">
              <label
                for="password"
                class="block text-[11px] font-semibold uppercase tracking-[0.14em] text-slate-400"
              >
                Parol
              </label>
              <div class="relative">
                <Lock
                  class="pointer-events-none absolute left-3.5 top-1/2 h-[18px] w-[18px] -translate-y-1/2 text-slate-400"
                  :stroke-width="1.75"
                />
                <input
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="current-password"
                  required
                  placeholder="Parolni kiriting"
                  class="h-12 w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-11 pr-11 text-sm text-slate-900 placeholder:text-slate-400 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/15"
                />
                <button
                  type="button"
                  tabindex="-1"
                  class="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-slate-400 transition hover:bg-slate-50 hover:text-slate-600"
                  :aria-label="showPassword ? 'Parolni yashirish' : 'Parolni ko‘rsatish'"
                  @click="showPassword = !showPassword"
                >
                  <EyeOff v-if="showPassword" class="h-5 w-5" :stroke-width="1.75" />
                  <Eye v-else class="h-5 w-5" :stroke-width="1.75" />
                </button>
              </div>
            </div>

            <p
              v-if="error"
              class="rounded-xl bg-rose-50 px-3 py-2.5 text-sm text-rose-700 ring-1 ring-rose-100"
            >
              {{ error }}
            </p>

            <button
              type="submit"
              :disabled="auth.loading"
              class="mt-1 flex h-12 w-full items-center justify-center rounded-xl bg-[#2563EB] text-sm font-bold text-white shadow-[0_8px_24px_rgba(37,99,235,0.35)] transition hover:bg-[#1D4ED8] active:bg-[#1E40AF] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {{ auth.loading ? 'Kutilmoqda...' : 'Tizimga kirish' }}
            </button>
          </form>
        </div>

        <p class="mt-6 text-center text-xs text-slate-400 lg:hidden">
          © {{ year }} Urganch Davlat Pedagogika Instituti
        </p>
      </section>
    </div>
  </FullScreenLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Eye, EyeOff, Lock, User } from 'lucide-vue-next'
import FullScreenLayout from '@/components/layout/FullScreenLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { getErrorMessage } from '@/api/http'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const year = new Date().getFullYear()
const username = ref('')
const password = ref('')
const showPassword = ref(false)
const error = ref('')

async function handleSubmit() {
  error.value = ''
  try {
    await auth.login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (e) {
    error.value = getErrorMessage(e, 'Login yoki parol noto‘g‘ri')
  }
}
</script>
