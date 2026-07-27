import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import VueApexCharts from 'vue3-apexcharts'
import { i18n } from './i18n'

import './assets/main.css'
import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'
import 'jsvectormap/dist/jsvectormap.css'
import 'flatpickr/dist/flatpickr.css'
import 'sweetalert2/dist/sweetalert2.min.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(VueApexCharts)

app.mount('#app')
