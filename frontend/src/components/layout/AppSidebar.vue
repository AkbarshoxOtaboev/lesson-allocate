<template>
  <aside
    :class="[
      'fixed mt-16 flex flex-col lg:mt-0 top-0 px-5 left-0 bg-white dark:bg-gray-900 dark:border-gray-800 text-gray-900 h-screen transition-all duration-300 ease-in-out z-99999 border-r border-gray-200',
      {
        'lg:w-[290px]': isExpanded || isMobileOpen || isHovered,
        'lg:w-[90px]': !isExpanded && !isHovered,
        'translate-x-0 w-[290px]': isMobileOpen,
        '-translate-x-full': !isMobileOpen,
        'lg:translate-x-0': true,
      },
    ]"
    @mouseenter="!isExpanded && (isHovered = true)"
    @mouseleave="isHovered = false"
  >
    <div
      :class="[
        'py-6 flex items-center gap-3',
        !isExpanded && !isHovered ? 'lg:justify-center' : 'justify-start',
      ]"
    >
      <router-link to="/" class="flex min-w-0 items-center gap-3">
        <img
          src="/images/logo/urspi-logo.png"
          alt="UrSPI"
          class="h-11 w-11 shrink-0 rounded-full border border-slate-200 object-cover"
        />
        <div v-if="isExpanded || isHovered || isMobileOpen" class="min-w-0">
          <p class="truncate text-sm font-bold text-slate-800 dark:text-white">UrSPI</p>
          <p class="truncate text-[10px] font-semibold uppercase tracking-wider text-slate-500">
            Yuklama tizimi
          </p>
        </div>
      </router-link>
    </div>

    <div class="flex flex-col overflow-y-auto duration-300 ease-linear no-scrollbar">
      <nav class="mb-6">
        <div class="flex flex-col gap-4">
          <div v-for="(menuGroup, groupIndex) in visibleMenuGroups" :key="groupIndex">
            <h2
              :class="[
                'mb-4 text-xs uppercase flex leading-[20px] text-gray-400',
                !isExpanded && !isHovered ? 'lg:justify-center' : 'justify-start',
              ]"
            >
              <template v-if="isExpanded || isHovered || isMobileOpen">
                {{ menuGroup.title }}
              </template>
              <HorizontalDots v-else />
            </h2>
            <ul class="flex flex-col gap-1.5">
              <li v-for="item in menuGroup.items" :key="item.name">
                <router-link
                  :to="item.path"
                  :class="[
                    'menu-item group',
                    {
                      'menu-item-active': isActive(item),
                      'menu-item-inactive': !isActive(item),
                    },
                    !isExpanded && !isHovered ? 'lg:justify-center' : 'lg:justify-start',
                  ]"
                >
                  <span
                    :class="
                      isActive(item) ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                    "
                  >
                    <component :is="item.icon" v-bind="item.iconProps || { class: 'h-5 w-5' }" />
                  </span>
                  <span
                    v-if="isExpanded || isHovered || isMobileOpen"
                    class="menu-item-text flex-1"
                  >{{ item.name }}</span>
                  <span
                    v-if="item.badge && (isExpanded || isHovered || isMobileOpen)"
                    class="rounded-full bg-indigo-600 px-1.5 py-0.5 text-[10px] font-bold text-white"
                  >
                    {{ item.badge }}
                  </span>
                  <span
                    v-else-if="isActive(item) && (isExpanded || isHovered || isMobileOpen)"
                    class="h-1.5 w-1.5 rounded-full bg-indigo-600"
                  />
                </router-link>
              </li>
            </ul>
          </div>
        </div>
      </nav>
    </div>
  </aside>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  BookMarked,
  BookOpen,
  Briefcase,
  LayoutDashboard,
  Settings2,
  Users,
} from 'lucide-vue-next'
import { HorizontalDots } from '../../icons'
import { useSidebar } from '@/composables/useSidebar'
import { useAuthStore } from '@/stores/auth'
import { talabnomaApi } from '@/api/talabnoma'

const { t } = useI18n()
const route = useRoute()
const auth = useAuthStore()
const { isExpanded, isMobileOpen, isHovered } = useSidebar()
const newTalabnomaCount = ref(0)

const iconProps = { class: 'h-5 w-5', strokeWidth: 1.75 }

const managementPaths = [
  '/users',
  '/academic-years',
  '/faculties',
  '/departments',
  '/directions',
  '/teachers',
  '/roles',
  '/audit',
  '/hemis',
]

const visibleMenuGroups = computed(() => {
  const items = [
    { icon: LayoutDashboard, iconProps, name: t('nav.home'), path: '/' },
    { icon: BookOpen, iconProps, name: t('nav.subjects'), path: '/subjects' },
    { icon: Briefcase, iconProps, name: t('nav.workloads'), path: '/workloads' },
    {
      icon: BookMarked,
      iconProps,
      name: t('nav.talabnoma'),
      path: '/talabnomalar',
      badge: newTalabnomaCount.value > 0 ? 'Yangi' : null,
    },
    { icon: Users, iconProps, name: t('nav.teachersMenu'), path: '/oqituvchilar' },
  ]

  if (auth.isSuperAdmin || auth.isAdmin) {
    items.push({
      icon: Settings2,
      iconProps,
      name: t('nav.management'),
      path: '/users',
      management: true,
    })
  }

  return [{ title: t('nav.main'), items }]
})

const isActive = (item) => {
  if (item.management) return managementPaths.includes(route.path)
  if (item.path === '/') return route.path === '/'
  return route.path === item.path
}

async function loadNewCount() {
  if (!auth.isAuthenticated) return
  try {
    const { data } = await talabnomaApi.newCount()
    newTalabnomaCount.value = data?.count || 0
  } catch {
    newTalabnomaCount.value = 0
  }
}

onMounted(loadNewCount)
</script>
