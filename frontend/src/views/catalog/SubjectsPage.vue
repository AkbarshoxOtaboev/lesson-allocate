<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Fanlar" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 px-5 py-4 dark:border-gray-800"
      >
        <h3 class="font-semibold text-gray-800 dark:text-white/90">Fanlar</h3>
        <button
          type="button"
          class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600"
          @click="openCreate"
        >
          Qo‘shish
        </button>
      </div>

      <div class="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <div
          class="inline-flex rounded-lg border border-gray-200 bg-gray-50 p-1 dark:border-gray-700 dark:bg-gray-800/60"
          role="tablist"
          aria-label="Semestr filtri"
        >
          <button
            v-for="opt in semesterFilterOptions"
            :key="opt.value || 'all'"
            type="button"
            role="tab"
            class="rounded-md px-3 py-1.5 text-sm font-medium transition"
            :class="
              selectedSemester === opt.value
                ? 'bg-white text-brand-600 shadow-sm dark:bg-gray-900 dark:text-brand-400'
                : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'
            "
            @click="onSemesterFilterChange(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div
        class="grid grid-cols-1 gap-3 border-b border-gray-200 px-5 py-4 sm:grid-cols-2 lg:grid-cols-3 dark:border-gray-800"
      >
        <div>
          <label class="mb-1 block text-xs text-gray-500">Fakultet</label>
          <select v-model="selectedFacultyId" class="filter-field" @change="onFacultyFilterChange">
            <option value="">Barchasi</option>
            <option v-for="f in faculties" :key="f.id" :value="String(f.id)">{{ f.name }}</option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Kafedra</label>
          <select v-model="selectedDepartmentId" class="filter-field" @change="load">
            <option value="">Barchasi</option>
            <option v-for="d in filteredDepartments" :key="d.id" :value="String(d.id)">
              {{ d.name }}
            </option>
          </select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-gray-500">Fan nomi bo‘yicha qidirish</label>
          <input
            v-model="nameSearch"
            type="search"
            placeholder="Fan nomi yoki kodi..."
            class="filter-field"
          />
        </div>
      </div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">№</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan kodi</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan nomi</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kafedra</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kelgan fakultet</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">O'quv yili</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Semestr</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Umumiy soat</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kredit</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Auditorik soat</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Maruza</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Amaliy</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Lab</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Seminar</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Mustaqil</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Reyting</th>
              <th class="px-3 py-3 text-right text-theme-xs font-medium text-gray-500">Amallar</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 dark:divide-gray-800">
            <tr v-for="(item, index) in displayedItems" :key="item.id">
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ index + 1 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.code }}</td>
              <td class="px-3 py-4 text-theme-sm font-medium text-gray-800 dark:text-white/90">
                <button
                  type="button"
                  class="text-left text-brand-600 hover:underline dark:text-brand-400"
                  @click="openDetail(item)"
                >
                  {{ item.name }}
                </button>
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.departmentName || '—' }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">
                <span v-if="item.sourceFacultyName" class="font-medium text-indigo-700 dark:text-indigo-300">
                  {{ item.sourceFacultyName }}
                </span>
                <span v-else>—</span>
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.academicYearName || '—' }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ semesterLabel(item.semester) }}</td>
              <td class="px-3 py-4 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ item.totalSubjectHours ?? 0 }}
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ formatCredit(item.credit) }}</td>
              <td class="px-3 py-4 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ item.totalHours ?? 0 }}
              </td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.lectureHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.practicalHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.labHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.seminarHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.independentStudyHours ?? 0 }}</td>
              <td class="px-3 py-4 text-theme-sm text-gray-500">{{ item.ratingHours ?? 0 }}</td>
              <td class="px-3 py-4">
                <div class="flex justify-end gap-2">
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-warning-500 hover:bg-warning-50 dark:hover:bg-warning-500/10"
                    title="Tahrirlash"
                    @click="openEdit(item)"
                  >
                    <PencilAltIcon class="size-5" />
                  </button>
                  <button
                    type="button"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-lg text-error-600 hover:bg-error-50 dark:hover:bg-error-500/10"
                    title="O‘chirish"
                    @click="removeItem(item)"
                  >
                    <TrashIcon class="size-5" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!displayedItems.length">
              <td colspan="17" class="px-5 py-8 text-center text-sm text-gray-500">Bo‘sh</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal v-if="modalOpen" full-screen-backdrop @close="modalOpen = false">
      <template #body>
        <div
          class="relative max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingId ? 'Fanni tahrirlash' : 'Yangi fan' }}
          </h3>
          <form class="space-y-4" @submit.prevent="save">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Kafedra</label>
              <select v-model="formDepartmentId" required class="filter-field">
                <option value="">Tanlang</option>
                <option v-for="d in formDepartments" :key="d.id" :value="String(d.id)">
                  {{ d.name }}
                </option>
              </select>
            </div>

            <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">O'quv yili</label>
                <select v-model="form.academicYearId" required class="filter-field">
                  <option value="">Tanlang</option>
                  <option v-for="ay in academicYears" :key="ay.id" :value="ay.id">
                    {{ ay.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Semestr</label>
                <select v-model="form.semester" required class="filter-field">
                  <option value="AUTUMN">Kuzgi semestr</option>
                  <option value="SPRING">Bahorgi semestr</option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Ta'lim turi</label>
                <select v-model="form.educationType" required class="filter-field">
                  <option value="KUNDUZGI">Kunduzgi</option>
                  <option value="KECHKI">Kechki</option>
                  <option value="MASOFAVIY">Masofaviy</option>
                  <option value="SIRTQI">Sirtqi</option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Ta'lim tili</label>
                <select v-model="form.educationLanguage" required class="filter-field">
                  <option value="UZB">Uzb</option>
                  <option value="RUS">Rus</option>
                </select>
              </div>
            </div>

            <div class="relative">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Yo'nalish</label>
                <input
                  v-model="directionSearch"
                  type="text"
                  required
                  placeholder="Kod yoki nom bo'yicha qidiring"
                  class="form-field"
                  @focus="directionDropdownOpen = true"
                  @input="onDirectionInput"
                  @blur="closeDirectionDropdown"
                />
                <div
                  v-if="directionDropdownOpen && filteredDirections.length"
                  class="absolute z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-xl border border-gray-200 bg-white py-1 shadow-lg dark:border-gray-700 dark:bg-gray-900"
                >
                  <button
                    v-for="direction in filteredDirections"
                    :key="direction.id"
                    type="button"
                    class="flex w-full items-start justify-between gap-3 px-3 py-2 text-left hover:bg-gray-50 dark:hover:bg-gray-800"
                    @mousedown.prevent="selectDirection(direction)"
                  >
                    <span class="font-medium text-gray-800 dark:text-white/90">
                      {{ direction.directionName }}
                    </span>
                    <span class="text-xs text-gray-500">{{ direction.directionCode }}</span>
                  </button>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fan kodi</label>
                <input v-model="form.code" required class="form-field" />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fan nomi</label>
                <input v-model="form.name" required class="form-field" />
              </div>
            </div>

            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">
                Umumiy fan soati
              </label>
              <input
                v-model.number="form.totalSubjectHours"
                type="number"
                min="1"
                required
                placeholder="masalan: 1200"
                class="form-field"
              />
              <p class="mt-1 text-xs text-gray-500">Reyting soati qolgan umumiy soatga ta'sir qilmaydi.</p>
            </div>

            <div>
              <div class="mb-2 flex flex-wrap items-center justify-between gap-2">
                <p class="text-sm font-medium text-gray-700 dark:text-gray-300">Yuklama soatlari</p>
                <p
                  class="text-sm font-medium"
                  :class="remainingNonRatingHours === 0 ? 'text-success-600' : 'text-warning-600'"
                >
                  Qolgan: {{ remainingNonRatingHours }} soat
                </p>
              </div>
              <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
                <div class="relative">
                  <p
                    v-if="activeHourField === 'lectureHours'"
                    class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
                  >
                    Mavjud: {{ availableHoursFor('lectureHours') }}
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">Maruza (soat)</label>
                  <input
                    :value="form.lectureHours"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle('lectureHours')"
                    @focus="activeHourField = 'lectureHours'"
                    @mouseenter="activeHourField = 'lectureHours'"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput('lectureHours', $event)"
                  />
                </div>
                <div class="relative">
                  <p
                    v-if="activeHourField === 'practicalHours'"
                    class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
                  >
                    Mavjud: {{ availableHoursFor('practicalHours') }}
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">Amaliy (soat)</label>
                  <input
                    :value="form.practicalHours"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle('practicalHours')"
                    @focus="activeHourField = 'practicalHours'"
                    @mouseenter="activeHourField = 'practicalHours'"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput('practicalHours', $event)"
                  />
                </div>
                <div class="relative">
                  <p
                    v-if="activeHourField === 'labHours'"
                    class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
                  >
                    Mavjud: {{ availableHoursFor('labHours') }}
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">Laboratoriya</label>
                  <input
                    :value="form.labHours"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle('labHours')"
                    @focus="activeHourField = 'labHours'"
                    @mouseenter="activeHourField = 'labHours'"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput('labHours', $event)"
                  />
                </div>
                <div class="relative">
                  <p
                    v-if="activeHourField === 'seminarHours'"
                    class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
                  >
                    Mavjud: {{ availableHoursFor('seminarHours') }}
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">Seminar</label>
                  <input
                    :value="form.seminarHours"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle('seminarHours')"
                    @focus="activeHourField = 'seminarHours'"
                    @mouseenter="activeHourField = 'seminarHours'"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput('seminarHours', $event)"
                  />
                </div>
                <div class="relative">
                  <p
                    v-if="activeHourField === 'ratingHours'"
                    class="absolute -top-2 right-0 rounded-md bg-sky-50 px-2 py-1 text-[11px] font-semibold text-sky-600 shadow-sm ring-1 ring-sky-100"
                  >
                    Reyting erkin
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">Reyting</label>
                  <input
                    :value="form.ratingHours"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle('ratingHours')"
                    @focus="activeHourField = 'ratingHours'"
                    @mouseenter="activeHourField = 'ratingHours'"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput('ratingHours', $event)"
                  />
                </div>
                <div class="relative">
                  <p
                    v-if="activeHourField === 'independentStudyHours'"
                    class="absolute -top-2 right-0 rounded-md bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-600 shadow-sm ring-1 ring-emerald-100"
                  >
                    Mavjud: {{ availableHoursFor('independentStudyHours') }}
                  </p>
                  <label class="mb-1 block text-xs text-gray-500">Mustaqil ta'lim</label>
                  <input
                    :value="form.independentStudyHours"
                    type="number"
                    min="0"
                    class="form-field"
                    :title="hourInputTitle('independentStudyHours')"
                    @focus="activeHourField = 'independentStudyHours'"
                    @mouseenter="activeHourField = 'independentStudyHours'"
                    @mouseleave="activeHourField = null"
                    @blur="activeHourField = null"
                    @input="onHourInput('independentStudyHours', $event)"
                  />
                </div>
              </div>
              <p v-if="hoursWarning" class="mt-2 text-sm text-warning-600">{{ hoursWarning }}</p>
            </div>

            <div class="grid grid-cols-1 gap-3 rounded-xl bg-gray-50 p-4 sm:grid-cols-3 dark:bg-gray-800/50">
              <div>
                <p class="text-xs text-gray-500">Auditorik soat</p>
                <p class="text-lg font-semibold text-gray-800 dark:text-white/90">
                  {{ computedAuditoriyHours }}
                </p>
                <p class="text-xs text-gray-400">Maruza + amaliy + lab + seminar + reyting</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Umumiy soat</p>
                <p class="text-lg font-semibold text-gray-800 dark:text-white/90">
                  {{ computedOverallHours }}
                </p>
                <p class="text-xs text-gray-400">Auditorik + mustaqil ta'lim</p>
              </div>
              <div>
                <p class="text-xs text-gray-500">Kredit</p>
                <p class="text-lg font-semibold text-brand-600 dark:text-brand-400">
                  {{ computedCredit }}
                </p>
                <p class="text-xs text-gray-400">Umumiy fan soati / 30</p>
              </div>
            </div>

            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Guruhlar soni</label>
                <input v-model.number="form.groupCount" type="number" min="0" class="form-field" />
              </div>
              <div>
                <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Talabalar soni</label>
                <input v-model.number="form.studentCount" type="number" min="0" class="form-field" />
              </div>
            </div>

            <div v-if="formError" class="text-sm text-error-600">{{ formError }}</div>
            <div v-if="!isFullyAllocated && form.totalSubjectHours > 0" class="text-sm text-warning-600">
              Umumiy fan soati to‘liq taqsimlanmagan (qolgan: {{ remainingNonRatingHours }}). Saqlash uchun
              barcha soatlarni taqsimlang.
            </div>
            <div class="flex justify-end gap-2">
              <button
                type="button"
                class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
                @click="modalOpen = false"
              >
                Bekor
              </button>
              <button
                type="submit"
                class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="saving || !canSave"
              >
                Saqlash
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>

    <Modal v-if="detailOpen && detailItem" full-screen-backdrop @close="detailOpen = false">
      <template #body>
        <div
          class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <div class="mb-4 flex items-start justify-between gap-3">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">Fan va Yuklama</h3>
            <button
              type="button"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800"
              @click="detailOpen = false"
            >
              ×
            </button>
          </div>

          <div class="rounded-xl bg-gray-50 p-4 dark:bg-gray-800/60">
            <p class="text-base font-semibold text-gray-800 dark:text-white/90">
              {{ detailItem.name }}
            </p>
            <div class="mt-3 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
              <div>
                <span class="text-gray-500">Kafedra:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ detailItem.departmentName || '—' }}
                </span>
              </div>
              <div v-if="detailItem.sourceFacultyName">
                <span class="text-gray-500">Kelgan fakultet:</span>
                <span class="ml-1 font-medium text-indigo-700 dark:text-indigo-300">
                  {{ detailItem.sourceFacultyName }}
                </span>
                <span v-if="detailItem.talabnomaCode" class="ml-1 text-xs text-gray-400">
                  ({{ detailItem.talabnomaCode }})
                </span>
              </div>
              <div>
                <span class="text-gray-500">Semestr:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ semesterLabel(detailItem.semester) }} semestr
                </span>
              </div>
              <div>
                <span class="text-gray-500">Yo'nalish:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ detailItem.directionName || '—' }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Ta'lim:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ educationTypeLabel(detailItem.educationType) }} /
                  {{ educationLanguageLabel(detailItem.educationLanguage) }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Kredit:</span>
                <span class="ml-1 font-medium text-gray-800 dark:text-white/90">
                  {{ formatCredit(detailItem.credit) }}
                </span>
              </div>
              <div>
                <span class="text-gray-500">Umumiy soat:</span>
                <span class="ml-1 font-bold text-emerald-600">
                  {{ detailItem.totalSubjectHours ?? 0 }}
                </span>
              </div>
            </div>
          </div>

          <div class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Ma'ruza</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.lectureHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Amaliy</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.practicalHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Laboratoriya</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.labHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Reyting</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.ratingHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Seminar</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.seminarHours ?? 0 }}
              </p>
            </div>
            <div class="rounded-xl border border-gray-200 px-3 py-3 dark:border-gray-700">
              <p class="text-xs text-gray-500">Mustaqil t.</p>
              <p class="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {{ detailItem.independentStudyHours ?? 0 }}
              </p>
            </div>
          </div>

          <div class="mt-3 rounded-xl border border-gray-200 px-4 py-3 dark:border-gray-700">
            <p class="text-xs text-gray-500">Guruhlar/Talaba</p>
            <p class="mt-1 text-lg font-semibold text-gray-800 dark:text-white/90">
              {{ detailItem.groupCount ?? 0 }} / {{ detailItem.studentCount ?? 0 }}
            </p>
          </div>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import { academicYearApi, departmentApi, directionApi, facultyApi, subjectApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { confirmAction, showError } from '@/utils/swal'
import { PencilAltIcon, TrashIcon } from '@/icons'
import { useAuthStore } from '@/stores/auth'
import type { Direction, NamedEntity, Subject } from '@/types/api'

type DepartmentItem = NamedEntity & { facultyId?: number }
type HourField =
  | 'lectureHours'
  | 'practicalHours'
  | 'labHours'
  | 'seminarHours'
  | 'independentStudyHours'
  | 'ratingHours'

const semesterFilterOptions = [
  { value: '', label: 'Barchasi' },
  { value: 'AUTUMN', label: 'Kuzgi' },
  { value: 'SPRING', label: 'Bahorgi' },
] as const

interface AcademicYearItem { id: number; name: string }
interface DirectionItem extends Direction {}

const auth = useAuthStore()

const emptyForm = () => ({
  code: '',
  name: '',
  semester: 'AUTUMN' as 'AUTUMN' | 'SPRING',
  academicYearId: '' as string | number,
  directionId: '' as string | number,
  educationType: 'KUNDUZGI' as 'KUNDUZGI' | 'KECHKI' | 'MASOFAVIY' | 'SIRTQI',
  educationLanguage: 'UZB' as 'UZB' | 'RUS',
  totalSubjectHours: 0,
  lectureHours: 0,
  practicalHours: 0,
  labHours: 0,
  seminarHours: 0,
  independentStudyHours: 0,
  ratingHours: 0,
  groupCount: 0,
  studentCount: 0,
})

const items = ref<Subject[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<DepartmentItem[]>([])
const academicYears = ref<AcademicYearItem[]>([])
const directions = ref<DirectionItem[]>([])
const selectedFacultyId = ref('')
const selectedDepartmentId = ref('')
const selectedSemester = ref('')
const nameSearch = ref('')
const formDepartmentId = ref('')
const directionSearch = ref('')
const directionDropdownOpen = ref(false)
const form = ref(emptyForm())
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const formError = ref('')
const hoursWarning = ref('')
const modalOpen = ref(false)
const detailOpen = ref(false)
const detailItem = ref<Subject | null>(null)
const editingId = ref<number | null>(null)
const activeHourField = ref<HourField | null>(null)

const filteredDepartments = computed(() => {
  if (!selectedFacultyId.value) return departments.value
  const facultyId = Number(selectedFacultyId.value)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const formDepartments = computed(() => departments.value)

const filteredDirections = computed(() => {
  const q = directionSearch.value.trim().toLowerCase()
  if (!q) return directions.value.slice(0, 8)
  return directions.value
    .filter((item) => {
      const haystack = `${item.directionCode} ${item.directionName}`.toLowerCase()
      return haystack.includes(q)
    })
    .slice(0, 8)
})

const displayedItems = computed(() => {
  const q = nameSearch.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((item) => {
    const haystack = `${item.code} ${item.name}`.toLowerCase()
    return haystack.includes(q)
  })
})

const allocatedHours = computed(() => {
  const f = form.value
  return (
    orZero(f.lectureHours) +
    orZero(f.practicalHours) +
    orZero(f.labHours) +
    orZero(f.seminarHours) +
    orZero(f.independentStudyHours)
  )
})

const remainingNonRatingHours = computed(() =>
  Math.max(0, orZero(form.value.totalSubjectHours) - allocatedHours.value),
)

const isFullyAllocated = computed(
  () =>
    orZero(form.value.totalSubjectHours) > 0 &&
    allocatedHours.value === orZero(form.value.totalSubjectHours),
)

const canSave = computed(
  () => isFullyAllocated.value && Boolean(formDepartmentId.value) && Boolean(form.value.directionId),
)

const computedAuditoriyHours = computed(() => {
  const f = form.value
  return (
    orZero(f.lectureHours) +
    orZero(f.practicalHours) +
    orZero(f.labHours) +
    orZero(f.seminarHours) +
    orZero(f.ratingHours)
  )
})

const computedOverallHours = computed(
  () => computedAuditoriyHours.value + orZero(form.value.independentStudyHours),
)

const computedCredit = computed(() => {
  const total = orZero(form.value.totalSubjectHours)
  return formatCredit(total > 0 ? total / 30 : 0)
})

function orZero(value: number | undefined | null) {
  return Number.isFinite(Number(value)) ? Number(value) : 0
}

function formatCredit(value: number | undefined | null) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '0'
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

function semesterLabel(semester?: 'AUTUMN' | 'SPRING' | null) {
  if (semester === 'SPRING') return 'Bahorgi'
  if (semester === 'AUTUMN') return 'Kuzgi'
  return '—'
}

function educationTypeLabel(value?: Subject['educationType'] | null) {
  switch (value) {
    case 'KUNDUZGI':
      return 'Kunduzgi'
    case 'KECHKI':
      return 'Kechki'
    case 'MASOFAVIY':
      return 'Masofaviy'
    case 'SIRTQI':
      return 'Sirtqi'
    default:
      return '—'
  }
}

function educationLanguageLabel(value?: Subject['educationLanguage'] | null) {
  switch (value) {
    case 'UZB':
      return 'Uzb'
    case 'RUS':
      return 'Rus'
    default:
      return '—'
  }
}

function sumExcept(field: HourField) {
  const f = form.value
  const tracked: Exclude<HourField, 'ratingHours'>[] = [
    'lectureHours',
    'practicalHours',
    'labHours',
    'seminarHours',
    'independentStudyHours',
  ]
  return tracked.reduce((sum, key) => (key === field ? sum : sum + orZero(f[key])), 0)
}

function availableHoursFor(field: HourField) {
  if (field === 'ratingHours') return 0
  const total = orZero(form.value.totalSubjectHours)
  return Math.max(0, total - sumExcept(field))
}

function hourInputTitle(field: HourField) {
  if (field === 'ratingHours') return "Reyting soati umumiy taqsimotga kirmaydi"
  return `Mavjud: ${availableHoursFor(field)} soat`
}

function onDirectionInput() {
  directionDropdownOpen.value = true
  const matched = directions.value.find((item) => {
    const haystack = `${item.directionCode} - ${item.directionName}`.toLowerCase()
    return haystack === directionSearch.value.trim().toLowerCase()
  })
  form.value.directionId = matched?.id ?? ''
}

function selectDirection(direction: DirectionItem) {
  form.value.directionId = direction.id
  directionSearch.value = `${direction.directionCode} - ${direction.directionName}`
  directionDropdownOpen.value = false
}

function closeDirectionDropdown() {
  window.setTimeout(() => {
    directionDropdownOpen.value = false
  }, 120)
}

function onHourInput(field: HourField, event: Event) {
  const input = event.target as HTMLInputElement
  const raw = input.value
  const next = raw === '' ? 0 : Number(raw)
  const total = orZero(form.value.totalSubjectHours)

  if (!Number.isFinite(next) || next < 0) {
    input.value = String(form.value[field])
    return
  }

  if (total <= 0) {
    hoursWarning.value = 'Avval Umumiy fan soatini kiriting'
    form.value[field] = 0
    input.value = '0'
    return
  }

  if (field === 'ratingHours') {
    hoursWarning.value = ''
    form.value[field] = next
    return
  }

  const others = sumExcept(field)
  const maxAllowed = Math.max(0, total - others)

  if (next > maxAllowed) {
    hoursWarning.value = `Faqat ${maxAllowed} soat qoldi. ${next} soat kiritib bo'lmaydi.`
    form.value[field] = maxAllowed
    input.value = String(maxAllowed)
    return
  }

  hoursWarning.value = ''
  form.value[field] = next
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function listParams() {
  const params: Record<string, string | number> = {}
  if (selectedDepartmentId.value) params.departmentId = Number(selectedDepartmentId.value)
  else if (selectedFacultyId.value) params.facultyId = Number(selectedFacultyId.value)
  if (selectedSemester.value) params.semester = selectedSemester.value
  return Object.keys(params).length ? params : undefined
}

async function loadFilterOptions() {
  try {
    const [academicYearsRes, directionsRes] = await Promise.all([
      academicYearApi.list(),
      directionApi.list(),
    ])
    academicYears.value = unwrapList(academicYearsRes.data)
    directions.value = unwrapList(directionsRes.data)

    if (auth.hasFullAccess) {
      const [facRes, depRes] = await Promise.all([facultyApi.list(), departmentApi.list()])
      faculties.value = unwrapList(facRes.data)
      departments.value = unwrapList(depRes.data)
      return
    }

    if (auth.isDekan && auth.facultyId) {
      faculties.value = auth.user?.facultyId
        ? [{ id: auth.user.facultyId, name: auth.user.facultyName || 'Fakultet' }]
        : []
      const depRes = await departmentApi.list({ facultyId: auth.facultyId })
      departments.value = unwrapList(depRes.data)
      return
    }

    if (auth.isKafedra) {
      faculties.value = auth.user?.facultyId
        ? [{ id: auth.user.facultyId, name: auth.user.facultyName || 'Fakultet' }]
        : []
      departments.value = auth.user?.departmentId
        ? [
            {
              id: auth.user.departmentId,
              name: auth.user.departmentName || 'Kafedra',
              facultyId: auth.user.facultyId ?? undefined,
            },
          ]
        : []
      return
    }

    faculties.value = []
    departments.value = []
  } catch {
    faculties.value = []
    departments.value = []
    academicYears.value = []
    directions.value = []
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await subjectApi.list(listParams())
    items.value = unwrapList<Subject>(data)
  } catch (e) {
    showError(getErrorMessage(e))
    items.value = []
  } finally {
    loading.value = false
  }
}

function onFacultyFilterChange() {
  selectedDepartmentId.value = ''
  load()
}

function onSemesterFilterChange(value: string) {
  selectedSemester.value = value
  load()
}

function resetForm() {
  form.value = emptyForm()
  formDepartmentId.value = selectedDepartmentId.value || ''
  hoursWarning.value = ''
  directionSearch.value = ''
  directionDropdownOpen.value = false
}

function openDetail(item: Subject) {
  detailItem.value = item
  detailOpen.value = true
}

async function openCreate() {
  editingId.value = null
  formError.value = ''
  await loadFilterOptions()
  resetForm()
  modalOpen.value = true
}

async function openEdit(item: Subject) {
  editingId.value = item.id
  formError.value = ''
  hoursWarning.value = ''
  await loadFilterOptions()
  formDepartmentId.value = item.departmentId ? String(item.departmentId) : ''
  form.value = {
    code: item.code,
    name: item.name,
    semester: item.semester === 'SPRING' ? 'SPRING' : 'AUTUMN',
    academicYearId: item.academicYearId ?? '',
    directionId: item.directionId ?? '',
    educationType: item.educationType ?? 'KUNDUZGI',
    educationLanguage: item.educationLanguage ?? 'UZB',
    totalSubjectHours: item.totalSubjectHours ?? item.overallHours ?? 0,
    lectureHours: item.lectureHours ?? 0,
    practicalHours: item.practicalHours ?? 0,
    labHours: item.labHours ?? 0,
    seminarHours: item.seminarHours ?? 0,
    independentStudyHours: item.independentStudyHours ?? 0,
    ratingHours: item.ratingHours ?? 0,
    groupCount: item.groupCount ?? 0,
    studentCount: item.studentCount ?? 0,
  }
  directionSearch.value = item.directionId
    ? `${item.directionCode || ''} - ${item.directionName || ''}`.trim()
    : ''
  modalOpen.value = true
}

async function save() {
  if (!canSave.value) {
    formError.value = "Umumiy fan soati to'liq taqsimlanmagan yoki yo'nalish tanlanmagan"
    return
  }
  saving.value = true
  formError.value = ''
  try {
    const payload = {
      departmentId: Number(formDepartmentId.value),
      academicYearId: form.value.academicYearId ? Number(form.value.academicYearId) : null,
      directionId: form.value.directionId ? Number(form.value.directionId) : null,
      code: form.value.code.trim(),
      name: form.value.name.trim(),
      semester: form.value.semester,
      educationType: form.value.educationType,
      educationLanguage: form.value.educationLanguage,
      totalSubjectHours: orZero(form.value.totalSubjectHours),
      lectureHours: orZero(form.value.lectureHours),
      practicalHours: orZero(form.value.practicalHours),
      labHours: orZero(form.value.labHours),
      seminarHours: orZero(form.value.seminarHours),
      independentStudyHours: orZero(form.value.independentStudyHours),
      ratingHours: orZero(form.value.ratingHours),
      groupCount: orZero(form.value.groupCount),
      studentCount: orZero(form.value.studentCount),
    }
    if (editingId.value) await subjectApi.update(editingId.value, payload)
    else await subjectApi.create(payload)
    modalOpen.value = false
    await load()
  } catch (e) {
    formError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

async function removeItem(item: Subject) {
  const ok = await confirmAction(`"${item.name}" o‘chirilsinmi?`, 'O‘chirish')
  if (!ok) return
  try {
    await subjectApi.remove(item.id)
    await load()
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

onMounted(async () => {
  if (auth.isKafedra) {
    selectedFacultyId.value = auth.facultyId ? String(auth.facultyId) : ''
    selectedDepartmentId.value = auth.departmentId ? String(auth.departmentId) : ''
  } else if (auth.isDekan) {
    selectedFacultyId.value = auth.facultyId ? String(auth.facultyId) : ''
  }
  await loadFilterOptions()
  await load()
})
</script>

<style scoped>
.filter-field,
.form-field {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}
:global(.dark) .filter-field,
:global(.dark) .form-field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
