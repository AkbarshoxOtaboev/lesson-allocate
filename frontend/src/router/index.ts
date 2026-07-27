import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(_to, _from, savedPosition) {
    return savedPosition || { left: 0, top: 0 }
  },
  routes: [
    {
      path: '/signin',
      name: 'Signin',
      component: () => import('../views/Auth/Signin.vue'),
      meta: { title: 'Kirish', public: true },
    },
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { title: 'Bosh sahifa' },
    },
    {
      path: '/users',
      name: 'Users',
      component: () => import('../views/users/UsersPage.vue'),
      meta: { title: 'Foydalanuvchilar', permission: 'USER_VIEW' },
    },
    {
      path: '/roles',
      name: 'Roles',
      component: () => import('../views/roles/RolesPage.vue'),
      meta: { title: 'Rollar', permission: 'ROLE_VIEW' },
    },
    {
      path: '/audit',
      name: 'Audit',
      component: () => import('../views/audit/AuditPage.vue'),
      meta: { title: 'Audit log', role: 'SUPER_ADMIN' },
    },
    {
      path: '/hemis',
      name: 'Hemis',
      component: () => import('../views/hemis/HemisPage.vue'),
      meta: { title: 'HEMIS', permission: 'EXTERNAL_TOKEN_VIEW' },
    },
    {
      path: '/faculties',
      name: 'Faculties',
      component: () => import('../views/catalog/CatalogCrudPage.vue'),
      meta: {
        title: 'Fakultetlar',
        permission: 'FACULTY_VIEW',
        catalog: 'faculties',
      },
      props: { kind: 'faculties' },
    },
    {
      path: '/departments',
      name: 'Departments',
      component: () => import('../views/catalog/CatalogCrudPage.vue'),
      meta: {
        title: 'Kafedralar',
        permission: 'DEPARTMENT_VIEW',
        catalog: 'departments',
      },
      props: { kind: 'departments' },
    },
    {
      path: '/teachers',
      name: 'Teachers',
      component: () => import('../views/catalog/CatalogCrudPage.vue'),
      meta: {
        title: "O'qituvchilar",
        permission: 'TEACHER_VIEW',
        catalog: 'teachers',
      },
      props: { kind: 'teachers' },
    },
    {
      path: '/academic-years',
      name: 'AcademicYears',
      component: () => import('../views/catalog/AcademicYearsPage.vue'),
      meta: {
        title: "O'quv yili",
        permission: 'ACADEMIC_YEAR_VIEW',
      },
    },
    {
      path: '/subjects',
      name: 'Subjects',
      component: () => import('../views/catalog/SubjectsPage.vue'),
      meta: {
        title: 'Fanlar',
        permission: 'SUBJECT_VIEW',
        catalog: 'subjects',
      },
    },
    {
      path: '/workloads',
      name: 'Workloads',
      component: () => import('../views/catalog/DepartmentWorkloadPage.vue'),
      meta: {
        title: 'Kafedra yuklamasi',
        permission: 'WORKLOAD_VIEW',
      },
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/Others/UserProfile.vue'),
      meta: { title: 'Profil' },
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('../views/Others/SettingsPage.vue'),
      meta: { title: 'Sozlamalar' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/Errors/FourZeroFour.vue'),
      meta: { title: '404', public: true },
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  document.title = `${to.meta.title || 'URSPI'} | URSPI Admin`

  const auth = useAuthStore()
  const isPublic = Boolean(to.meta.public)

  if (!isPublic) {
    const ok = await auth.ensureValidSession()
    if (!ok) {
      next({ name: 'Signin', query: { redirect: to.fullPath } })
      return
    }
  }

  if (to.name === 'Signin' && auth.isAuthenticated) {
    const ok = await auth.ensureValidSession()
    if (ok) {
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router
