<template>
  <AdminLayout>
    <PageBreadcrumb page-title="Kafedra yuklamasi" />

    <div
      class="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <div class="flex items-center justify-end border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <button
          type="button"
          class="rounded-xl bg-teal-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-teal-700"
          @click="exportCsv"
        >
          Excelga yuklash
        </button>
      </div>
      <div class="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
        <h3 class="font-semibold text-gray-800 dark:text-white/90">Saralash va qidiruv</h3>
        <div class="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
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
            <label class="mb-1 block text-xs text-gray-500">Fakultet</label>
            <select v-model="selectedFacultyId" class="filter-field" @change="onFacultyChange">
              <option value="">Barchasi</option>
              <option v-for="f in faculties" :key="f.id" :value="String(f.id)">{{ f.name }}</option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-xs text-gray-500">Semestr</label>
            <select v-model="selectedSemester" class="filter-field" @change="load">
              <option value="">Barchasi</option>
              <option value="AUTUMN">Kuzki semestr</option>
              <option value="SPRING">Bahorki semestr</option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-xs text-gray-500">Kurs</label>
            <select v-model="selectedCourseYear" class="filter-field" @change="load">
              <option value="">Barchasi</option>
              <option value="1">1-kurs</option>
              <option value="2">2-kurs</option>
              <option value="3">3-kurs</option>
              <option value="4">4-kurs</option>
              <option value="5">5-kurs</option>
            </select>
          </div>
          <div>
            <label class="mb-1 block text-xs text-gray-500">Holati</label>
            <select v-model="selectedStatus" class="filter-field" @change="load">
              <option value="">Barchasi</option>
              <option value="ALLOCATED">Taqsimlangan</option>
              <option value="PARTIAL">To'liq taqsimlanmagan</option>
              <option value="UNALLOCATED">Taqsimlanmagan</option>
            </select>
          </div>
        </div>
      </div>
      <div v-if="loading" class="px-5 py-8 text-sm text-gray-500">Yuklanmoqda...</div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kafedra nomi</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fakultet</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kelgan fakultet</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Semestr</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Kurs</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan nomi</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Fan soati</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Ma'ruza</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Seminar</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Amaliy</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Lab</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Rayting soat</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Auditorik soat</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Mustaqil</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Umumiy soat</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Guruhlar</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Talabalar</th>
              <th class="px-3 py-3 text-left text-theme-xs font-medium text-gray-500">Holati</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in rows"
              :key="row.subjectId"
              class="border-b border-gray-100 dark:border-gray-800"
              :class="rowBg(row.allocationStatus)"
            >
              <td class="px-3 py-3 text-theme-sm text-gray-600 dark:text-gray-300">
                {{ row.departmentName || '—' }}
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600 dark:text-gray-300">
                {{ row.facultyName || '—' }}
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600 dark:text-gray-300">
                <span v-if="row.sourceFacultyName" class="font-medium text-indigo-700 dark:text-indigo-300">
                  {{ row.sourceFacultyName }}
                </span>
                <span v-else>—</span>
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600 dark:text-gray-300">
                {{ semesterLabel(row.semester) }}
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600 dark:text-gray-300">
                {{ row.courseYear ? `${row.courseYear}-kurs` : '—' }}
              </td>
              <td class="px-3 py-3 text-theme-sm font-semibold text-gray-800 dark:text-white/90">
                <button
                  type="button"
                  class="text-left hover:text-brand-600 hover:underline"
                  @click="openSubjectDetail(row.subjectId)"
                >
                  {{ row.subjectName }}
                </button>
              </td>
              <td class="px-3 py-3 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ fanHours(row) }}
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.lectureHours ?? 0 }}</td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.seminarHours ?? 0 }}</td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.practicalHours ?? 0 }}</td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.labHours ?? 0 }}</td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.ratingHours ?? 0 }}</td>
              <td class="px-3 py-3 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ row.totalHours ?? 0 }}
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">
                {{ row.independentStudyHours ?? 0 }}
              </td>
              <td class="px-3 py-3 text-theme-sm font-bold text-gray-800 dark:text-white/90">
                {{ overallHours(row) }}
              </td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.groupCount ?? 0 }}</td>
              <td class="px-3 py-3 text-theme-sm text-gray-600">{{ row.studentCount ?? 0 }}</td>
              <td class="px-3 py-3">
                <span
                  class="inline-flex rounded-full px-2.5 py-1 text-xs font-medium"
                  :class="statusBadge(row.allocationStatus)"
                >
                  {{ statusLabel(row.allocationStatus) }}
                </span>
              </td>
            </tr>
            <tr v-if="rows.length" class="bg-slate-50/80 font-semibold dark:bg-slate-900/40">
              <td colspan="6" class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">Jami</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.fanHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.lectureHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.seminarHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.practicalHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.labHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.ratingHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.totalHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.independentStudyHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.overallHours }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.groupCount }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">{{ workloadTotals.studentCount }}</td>
              <td class="px-3 py-3 text-theme-sm text-slate-700 dark:text-slate-200">—</td>
            </tr>
            <tr v-if="!rows.length">
              <td colspan="18" class="px-5 py-8 text-center text-sm text-gray-500">Bo‘sh</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 3-rasm: Fan tafsiloti + Taqsimlash -->
    <Modal v-if="detailOpen && detail" full-screen-backdrop @close="closeDetail">
      <template #body>
        <div
          class="relative max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <div class="mb-4 flex items-start justify-between gap-3">
            <div>
              <span
                class="inline-flex rounded-full border border-violet-300 px-3 py-1 text-xs font-medium text-violet-700 dark:border-violet-500/40 dark:text-violet-300"
              >
                {{ semesterLabel(detail.semester) }}
              </span>
              <h3 class="mt-3 text-xl font-bold text-gray-900 dark:text-white">
                {{ detail.subjectName }}
              </h3>
              <div class="mt-2 flex flex-wrap gap-2 text-xs text-gray-600 dark:text-gray-300">
                <span class="rounded-full bg-gray-100 px-3 py-1 dark:bg-gray-800">
                  Fakultet: <strong>{{ detail.facultyName || '—' }}</strong>
                </span>
                <span
                  v-if="detail.sourceFacultyName"
                  class="rounded-full bg-indigo-50 px-3 py-1 text-indigo-700 dark:bg-indigo-500/10 dark:text-indigo-300"
                >
                  Kelgan: <strong>{{ detail.sourceFacultyName }}</strong>
                  <template v-if="detail.talabnomaCode"> ({{ detail.talabnomaCode }})</template>
                </span>
                <span class="rounded-full bg-gray-100 px-3 py-1 dark:bg-gray-800">
                  Kafedra: <strong>{{ detail.departmentName || '—' }}</strong>
                </span>
              </div>
            </div>
            <button
              type="button"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100"
              @click="closeDetail"
            >
              ×
            </button>
          </div>

          <div class="grid grid-cols-2 gap-3 md:grid-cols-4">
            <div
              v-for="card in detailCards"
              :key="card.key"
              class="rounded-xl border border-gray-200 p-3 dark:border-gray-700"
              :class="card.emphasis ? 'border-brand-300 dark:border-brand-500/40' : ''"
            >
              <p class="text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                {{ card.label }}
              </p>
              <p class="mt-2 text-lg font-bold text-gray-900 dark:text-white">{{ card.value }}</p>
              <p
                class="mt-2 rounded-md px-2 py-1 text-xs font-medium"
                :class="card.footerClass"
              >
                {{ card.footer }}
              </p>
            </div>
          </div>

          <div
            v-if="detail.allocations?.length"
            class="mt-4 rounded-xl border border-gray-200 p-4 dark:border-gray-700"
          >
            <p class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-200">
              Taqsimlangan o‘qituvchilar
            </p>
            <ul class="space-y-2 text-sm text-gray-600 dark:text-gray-300">
              <li
                v-for="a in detail.allocations"
                :key="a.id"
                class="rounded-lg border border-gray-100 px-3 py-2 dark:border-gray-800"
              >
                <div class="flex items-center justify-between gap-2">
                  <span class="font-medium text-gray-800 dark:text-white/90">{{ a.teacherName }}</span>
                  <span class="font-semibold">{{ a.totalHours }} soat</span>
                </div>
                <div class="mt-1 space-y-0.5 text-xs text-gray-500">
                  <p>{{ normalizeEmploymentStaff(a.employmentStaffName) }}</p>
                  <p>Ish yuklamasi: {{ a.workloadRate ?? '—' }} stavka</p>
                  <p v-if="a.groups?.length">
                    Guruhlar: {{ a.groups.map((g) => g.name).join(', ') }}
                  </p>
                  <p v-else>Guruh tanlanmagan</p>
                </div>
              </li>
            </ul>
          </div>

          <div class="mt-5 flex flex-wrap gap-2">
            <button
              type="button"
              class="flex-1 rounded-xl bg-brand-500 px-4 py-3 text-sm font-semibold text-white hover:bg-brand-600"
              @click="openTeacherPicker"
            >
              ⚡ Taqsimlash
            </button>
            <button
              type="button"
              class="rounded-xl bg-gray-100 px-4 py-3 text-sm font-medium text-gray-700 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-200"
              @click="closeDetail"
            >
              Yopish
            </button>
          </div>
        </div>
      </template>
    </Modal>

    <!-- O'qituvchi tanlash -->
    <Modal v-if="teacherPickerOpen" full-screen-backdrop @close="teacherPickerOpen = false">
      <template #body>
        <div
          class="relative max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <div class="mb-4 flex items-start justify-between gap-3">
            <div>
              <h3 class="flex items-center gap-2 text-lg font-bold text-gray-900 dark:text-white">
                <span class="h-2 w-2 rounded-full bg-violet-500"></span>
                O'qituvchini tanlang
              </h3>
              <p class="mt-1 text-sm text-gray-500">
                Dars soatlarini taqsimlash uchun o'qituvchi tanlang
              </p>
            </div>
            <div class="flex items-center gap-2">
              <span
                class="rounded-full border border-brand-300 px-3 py-1 text-xs font-medium text-brand-600"
              >
                {{ teachers.length }} nafar
              </span>
              <button
                type="button"
                class="inline-flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100"
                @click="teacherPickerOpen = false"
              >
                ×
              </button>
            </div>
          </div>

          <input
            v-model="teacherSearch"
            type="search"
            placeholder="O'qituvchi ism-sharifi bo'yicha qidirish..."
            class="mb-4 h-11 w-full rounded-xl border border-brand-200 px-4 text-sm dark:border-gray-700 dark:bg-gray-900"
          />

          <div v-if="teachersLoading" class="py-6 text-sm text-gray-500">Yuklanmoqda...</div>
          <div v-else class="space-y-3">
            <button
              v-for="t in filteredTeachers"
              :key="t.id"
              type="button"
              class="flex w-full items-center gap-3 rounded-xl border border-gray-200 p-3 text-left transition hover:border-brand-300 hover:bg-brand-50/40 dark:border-gray-700 dark:hover:bg-brand-500/10"
              @click="openAllocateForm(t)"
            >
              <div
                class="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-brand-500 text-sm font-bold text-white"
              >
                {{ initials(t.name) }}
              </div>
              <div class="min-w-0 flex-1">
                <p class="truncate font-semibold text-gray-900 dark:text-white">{{ t.name }}</p>
                <p class="mt-0.5 truncate text-xs text-gray-500">
                  {{ t.departmentName || t.staffPositionName || '—' }}
                </p>
              </div>
              <div class="text-right">
                <span class="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-gray-700 dark:bg-gray-800 dark:text-gray-200">
                  {{ t.totalAssignedHours ?? 0 }} soat
                </span>
                <p class="mt-1 text-[11px] text-gray-400">Yuklamasi</p>
              </div>
            </button>
            <p v-if="!filteredTeachers.length" class="py-6 text-center text-sm text-gray-500">
              O‘qituvchi topilmadi
            </p>
          </div>
        </div>
      </template>
    </Modal>

    <!-- 4-rasm: soat ajratish -->
    <Modal v-if="allocateOpen && detail && selectedTeacher" full-screen-backdrop @close="closeAllocate">
      <template #body>
        <div
          class="relative max-h-[92vh] w-full max-w-5xl overflow-y-auto rounded-2xl bg-white p-5 dark:bg-gray-900 md:p-6"
          @click.stop
        >
          <div class="grid gap-5 lg:grid-cols-2">
            <div>
              <div class="mb-3 flex items-center justify-between">
                <span
                  class="inline-flex rounded-full border border-violet-300 px-3 py-1 text-xs font-medium text-violet-700"
                >
                  {{ semesterLabel(detail.semester) }}
                </span>
                <button
                  type="button"
                  class="inline-flex h-8 w-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100"
                  @click="closeAllocate"
                >
                  ×
                </button>
              </div>
              <h3 class="text-xl font-bold text-gray-900 dark:text-white">{{ detail.subjectName }}</h3>
              <p class="mt-1 text-sm text-gray-500">
                Fakultet: {{ detail.facultyName || '—' }}
                <template v-if="detail.sourceFacultyName">
                  · Kelgan: {{ detail.sourceFacultyName }}
                </template>
                · Kafedra: {{ detail.departmentName || '—' }}
              </p>

              <div class="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
                <div
                  v-for="card in detailCards"
                  :key="`side-${card.key}`"
                  class="rounded-xl border border-gray-200 p-2.5 dark:border-gray-700"
                >
                  <p class="text-[10px] font-semibold uppercase text-gray-500">{{ card.label }}</p>
                  <p class="mt-1 text-sm font-bold text-gray-900 dark:text-white">{{ card.value }}</p>
                  <p class="mt-1 rounded px-1.5 py-0.5 text-[10px] font-medium" :class="card.footerClass">
                    {{ card.footer }}
                  </p>
                </div>
              </div>

              <button
                type="button"
                class="mt-4 w-full rounded-xl border border-gray-200 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-200"
                @click="closeAllocate"
              >
                ← Taqsimlashni yopish
              </button>
            </div>

            <div class="rounded-2xl border border-gray-200 p-4 dark:border-gray-700">
              <div class="mb-3 flex items-center gap-2">
                <button
                  type="button"
                  class="inline-flex h-8 w-8 items-center justify-center rounded-lg border border-gray-200 text-gray-600 dark:border-gray-700"
                  @click="backToTeachers"
                >
                  ←
                </button>
                <div>
                  <p class="text-xs font-semibold uppercase tracking-wide text-gray-500">
                    Ajratiladigan soat miqdori
                  </p>
                  <p class="font-bold text-gray-900 dark:text-white">{{ selectedTeacher.name }}</p>
                </div>
              </div>

              <div class="mb-4 rounded-xl bg-sky-50 px-3 py-2 text-xs text-sky-800 dark:bg-sky-500/10 dark:text-sky-200">
                Ushbu o'qituvchiga dars yuklamasidan qancha soat berilishini belgilang. Qavs ichida
                fanning taqsimlanmagan (mavjud) soatlari ko'rsatilgan.
              </div>

              <div class="mb-4 grid gap-3 md:grid-cols-[minmax(0,1fr)_180px]">
                <div>
                  <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200">
                    Bandlik turi
                  </label>
                  <select
                    v-model="allocateForm.employmentStaffName"
                    class="h-11 w-full rounded-xl border border-gray-300 bg-transparent px-3 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  >
                    <option v-for="option in employmentStaffOptions" :key="option" :value="option">
                      {{ option }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200">
                    Ish yuklamasi
                  </label>
                  <input
                    v-model.number="allocateForm.workloadRate"
                    type="number"
                    min="0"
                    step="0.25"
                    placeholder="1, 0.75, 0.5"
                    class="h-11 w-full rounded-xl border border-gray-300 bg-transparent px-3 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  />
                </div>
              </div>

              <div class="mb-4">
                <label class="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-200">
                  Guruhlar
                </label>
                <div class="relative" ref="groupSelectRef">
                  <div
                    class="min-h-11 w-full cursor-text rounded-xl border border-gray-300 bg-transparent px-3 py-2 dark:border-gray-700 dark:bg-gray-900"
                    :class="groupDropdownOpen ? 'border-brand-400 ring-2 ring-brand-500/15' : ''"
                    @click.stop="openGroupDropdown"
                  >
                    <div v-if="selectedGroups.length" class="mb-2 flex flex-wrap gap-1.5">
                      <span
                        v-for="g in selectedGroups"
                        :key="g.id"
                        class="inline-flex items-center gap-1 rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300"
                      >
                        {{ g.name }}
                        <span class="text-brand-500/80">({{ g.studentCount ?? 0 }})</span>
                        <button
                          type="button"
                          class="text-brand-500 hover:text-brand-700"
                          @click.stop="removeSelectedGroup(g.id)"
                        >
                          ×
                        </button>
                      </span>
                    </div>
                    <div class="flex items-center gap-2">
                      <input
                        ref="groupSearchInputRef"
                        v-model="groupSearch"
                        type="search"
                        placeholder="Guruh nomi bo'yicha qidirish..."
                        class="h-8 w-full border-0 bg-transparent p-0 text-sm text-gray-800 outline-none placeholder:text-gray-400 dark:text-white/90"
                        @focus="groupDropdownOpen = true"
                        @keydown.escape.prevent="closeGroupDropdown"
                      />
                      <button
                        v-if="groupDropdownOpen"
                        type="button"
                        class="shrink-0 rounded-md px-2 py-1 text-xs font-medium text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:hover:bg-gray-800"
                        @click.stop="closeGroupDropdown"
                      >
                        Yopish
                      </button>
                    </div>
                  </div>
                  <div
                    v-if="groupDropdownOpen"
                    class="absolute z-20 mt-1 max-h-56 w-full overflow-y-auto rounded-xl border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-900"
                    @click.stop
                  >
                    <button
                      v-for="g in filteredGroupOptions"
                      :key="g.id"
                      type="button"
                      class="flex w-full items-center justify-between px-3 py-2.5 text-left text-sm hover:bg-gray-50 dark:hover:bg-gray-800"
                      :class="isGroupSelected(g.id) ? 'bg-brand-50/70 dark:bg-brand-500/10' : ''"
                      @click.stop="toggleGroup(g)"
                    >
                      <span class="text-gray-800 dark:text-white/90">{{ g.name }}</span>
                      <span class="flex items-center gap-2">
                        <span class="text-xs text-gray-500">{{ g.studentCount ?? 0 }} talaba</span>
                        <span v-if="isGroupSelected(g.id)" class="text-xs font-semibold text-brand-600">✓</span>
                      </span>
                    </button>
                    <p
                      v-if="!filteredGroupOptions.length"
                      class="px-3 py-4 text-center text-sm text-gray-500"
                    >
                      {{
                        detail?.groups?.length ? 'Guruh topilmadi' : 'Fanga guruh biriktirilmagan'
                      }}
                    </p>
                  </div>
                </div>
                <p v-if="selectedGroups.length" class="mt-1.5 text-xs text-gray-500">
                  Tanlangan: {{ selectedGroups.length }} guruh · {{ selectedGroupsStudentTotal }} talaba
                </p>
              </div>

              <div class="space-y-3">
                <div
                  v-for="field in allocateFields"
                  :key="field.key"
                  class="flex items-center justify-between gap-3 rounded-xl border border-gray-100 px-3 py-2.5 dark:border-gray-800"
                >
                  <div>
                    <p class="flex items-center gap-2 text-sm font-medium text-gray-800 dark:text-white/90">
                      <span class="h-2 w-2 rounded-full" :class="field.dot"></span>
                      {{ field.label }}
                    </p>
                    <p class="mt-0.5 text-xs text-gray-500">
                      Umumiy: {{ field.total }} soat ·
                      <span class="font-semibold text-emerald-600">Mavjud: {{ field.available }} soat</span>
                    </p>
                  </div>
                  <input
                    v-model.number="allocateForm[field.key]"
                    type="number"
                    min="0"
                    :max="field.max"
                    class="h-10 w-20 rounded-lg border border-gray-300 px-2 text-center text-sm dark:border-gray-700 dark:bg-gray-900"
                  />
                </div>
              </div>

              <p v-if="allocateError" class="mt-3 text-sm text-error-600">{{ allocateError }}</p>

              <div class="mt-4 flex gap-2">
                <button
                  type="button"
                  class="rounded-xl border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-200"
                  @click="closeAllocate"
                >
                  Bekor qilish
                </button>
                <button
                  type="button"
                  class="flex-1 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
                  :disabled="saving"
                  @click="saveAllocation"
                >
                  ✓ Taqsimotni saqlash
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import Modal from '@/components/ui/Modal.vue'
import { departmentApi, facultyApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { showError } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import {
  workloadApi,
  type AllocatedGroup,
  type AllocationStatus,
  type TeacherLoad,
  type WorkloadDetail,
  type WorkloadRow,
} from '@/api/workload'
import type { NamedEntity } from '@/types/api'

type DepartmentItem = NamedEntity & { facultyId?: number }

const auth = useAuthStore()
const faculties = ref<NamedEntity[]>([])
const departments = ref<DepartmentItem[]>([])
const rows = ref<WorkloadRow[]>([])
const selectedFacultyId = ref('')
const selectedDepartmentId = ref('')
const selectedSemester = ref('')
const selectedCourseYear = ref('')
const selectedStatus = ref('')
const loading = ref(false)
const error = ref('')

const detailOpen = ref(false)
const detail = ref<WorkloadDetail | null>(null)

const teacherPickerOpen = ref(false)
const teachers = ref<TeacherLoad[]>([])
const teachersLoading = ref(false)
const teacherSearch = ref('')

const allocateOpen = ref(false)
const selectedTeacher = ref<TeacherLoad | null>(null)
const allocateForm = reactive({
  employmentStaffName: '',
  workloadRate: 1,
  lectureHours: 0,
  seminarHours: 0,
  practicalHours: 0,
  labHours: 0,
  ratingHours: 0,
})
const selectedGroupIds = ref<number[]>([])
const groupOptions = ref<AllocatedGroup[]>([])
const groupSearch = ref('')
const groupDropdownOpen = ref(false)
const groupSelectRef = ref<HTMLElement | null>(null)
const groupSearchInputRef = ref<HTMLInputElement | null>(null)
const allocateError = ref('')
const saving = ref(false)

const filteredDepartments = computed(() => {
  if (!selectedFacultyId.value) return departments.value
  const facultyId = Number(selectedFacultyId.value)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const filteredTeachers = computed(() => {
  const q = teacherSearch.value.trim().toLowerCase()
  if (!q) return teachers.value
  return teachers.value.filter((t) => t.name.toLowerCase().includes(q))
})

const selectedGroups = computed(() =>
  groupOptions.value.filter((g) => selectedGroupIds.value.includes(g.id)),
)

const selectedGroupsStudentTotal = computed(() =>
  selectedGroups.value.reduce((sum, g) => sum + (g.studentCount ?? 0), 0),
)

const filteredGroupOptions = computed(() => {
  const q = groupSearch.value.trim().toLowerCase()
  if (!q) return groupOptions.value
  return groupOptions.value.filter((g) => g.name.toLowerCase().includes(q))
})

const employmentStaffOptions = [
  'Asosiy shtat',
  "Ichki o'rindosh",
  "Tashqi o'rindosh",
  'Soatbay',
] as const

const workloadTotals = computed(() =>
  rows.value.reduce(
    (acc, row) => {
      acc.fanHours += fanHours(row)
      acc.lectureHours += row.lectureHours ?? 0
      acc.seminarHours += row.seminarHours ?? 0
      acc.practicalHours += row.practicalHours ?? 0
      acc.labHours += row.labHours ?? 0
      acc.ratingHours += row.ratingHours ?? 0
      acc.totalHours += row.totalHours ?? 0
      acc.independentStudyHours += row.independentStudyHours ?? 0
      acc.overallHours += overallHours(row)
      acc.groupCount += row.groupCount ?? 0
      acc.studentCount += row.studentCount ?? 0
      return acc
    },
    {
      fanHours: 0,
      lectureHours: 0,
      seminarHours: 0,
      practicalHours: 0,
      labHours: 0,
      ratingHours: 0,
      totalHours: 0,
      independentStudyHours: 0,
      overallHours: 0,
      groupCount: 0,
      studentCount: 0,
    },
  ),
)

const detailCards = computed(() => {
  const d = detail.value
  if (!d) return []
  return [
    {
      key: 'lecture',
      label: "Ma'ruza",
      value: `${d.lecture?.total ?? 0} soat`,
      footer: `Mavjud: ${d.lecture?.remaining ?? 0} soat`,
      footerClass: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300',
    },
    {
      key: 'seminar',
      label: 'Seminar',
      value: `${d.seminar?.total ?? 0} soat`,
      footer: `Mavjud: ${d.seminar?.remaining ?? 0} soat`,
      footerClass: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300',
    },
    {
      key: 'lab',
      label: 'Laboratoriya',
      value: `${d.lab?.total ?? 0} soat`,
      footer: `Mavjud: ${d.lab?.remaining ?? 0} soat`,
      footerClass: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300',
    },
    {
      key: 'practical',
      label: 'Amaliy',
      value: `${d.practical?.total ?? 0} soat`,
      footer: `Mavjud: ${d.practical?.remaining ?? 0} soat`,
      footerClass: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300',
    },
    {
      key: 'rating',
      label: 'Reyting',
      value: `${d.rating?.total ?? 0} soat`,
      footer: `Mavjud: ${d.rating?.remaining ?? 0} soat`,
      footerClass: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/10 dark:text-emerald-300',
    },
    {
      key: 'independent',
      label: "Mustaqil ta'lim",
      value: `${d.independentStudyHours ?? 0} soat`,
      footer: 'Yuklama',
      footerClass: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300',
    },
    {
      key: 'credit',
      label: 'Kredit',
      value: `${formatCredit(d.credit)} kredit`,
      footer: '30 soat/kredit',
      footerClass: 'bg-sky-50 text-sky-700 dark:bg-sky-500/10 dark:text-sky-300',
      emphasis: true,
    },
    {
      key: 'auditorik',
      label: 'Auditorik soat',
      value: `${d.totalHours ?? 0} soat`,
      footer: "Ma'ruza + seminar + amaliy + lab + reyting",
      footerClass: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300',
    },
    {
      key: 'overall',
      label: 'Umumiy soat',
      value: `${(d.totalHours ?? 0) + (d.independentStudyHours ?? 0)} soat`,
      footer: 'Auditorik + mustaqil',
      footerClass: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300',
    },
  ]
})

const allocateFields = computed(() => {
  const d = detail.value
  const t = selectedTeacher.value
  if (!d || !t) return []

  const otherLecture = (d.lecture?.allocated ?? 0) - (t.existingLectureHours ?? 0)
  const otherSeminar = (d.seminar?.allocated ?? 0) - (t.existingSeminarHours ?? 0)
  const otherPractical = (d.practical?.allocated ?? 0) - (t.existingPracticalHours ?? 0)
  const otherLab = (d.lab?.allocated ?? 0) - (t.existingLabHours ?? 0)
  const otherRating = (d.rating?.allocated ?? 0) - (t.existingRatingHours ?? 0)

  return [
    {
      key: 'lectureHours' as const,
      label: "Ma'ruza",
      total: d.lecture?.total ?? 0,
      available: Math.max(0, (d.lecture?.total ?? 0) - otherLecture),
      max: Math.max(0, (d.lecture?.total ?? 0) - otherLecture),
      dot: 'bg-blue-500',
    },
    {
      key: 'seminarHours' as const,
      label: 'Seminar',
      total: d.seminar?.total ?? 0,
      available: Math.max(0, (d.seminar?.total ?? 0) - otherSeminar),
      max: Math.max(0, (d.seminar?.total ?? 0) - otherSeminar),
      dot: 'bg-violet-500',
    },
    {
      key: 'practicalHours' as const,
      label: 'Amaliy',
      total: d.practical?.total ?? 0,
      available: Math.max(0, (d.practical?.total ?? 0) - otherPractical),
      max: Math.max(0, (d.practical?.total ?? 0) - otherPractical),
      dot: 'bg-amber-500',
    },
    {
      key: 'labHours' as const,
      label: 'Laboratoriya',
      total: d.lab?.total ?? 0,
      available: Math.max(0, (d.lab?.total ?? 0) - otherLab),
      max: Math.max(0, (d.lab?.total ?? 0) - otherLab),
      dot: 'bg-rose-500',
    },
    {
      key: 'ratingHours' as const,
      label: 'Reyting',
      total: d.rating?.total ?? 0,
      available: Math.max(0, (d.rating?.total ?? 0) - otherRating),
      max: Math.max(0, (d.rating?.total ?? 0) - otherRating),
      dot: 'bg-emerald-500',
    },
  ]
})

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function orZero(v: number | undefined | null) {
  return Number.isFinite(Number(v)) ? Number(v) : 0
}

function overallHours(row: { totalHours?: number | null; independentStudyHours?: number | null }) {
  return orZero(row.totalHours) + orZero(row.independentStudyHours)
}

/** Ma'ruza + Seminar + Amaliy + Lab + Mustaqil (reytingsiz) */
function fanHours(row: {
  lectureHours?: number | null
  seminarHours?: number | null
  practicalHours?: number | null
  labHours?: number | null
  independentStudyHours?: number | null
}) {
  return (
    orZero(row.lectureHours) +
    orZero(row.seminarHours) +
    orZero(row.practicalHours) +
    orZero(row.labHours) +
    orZero(row.independentStudyHours)
  )
}

function formatCredit(value: number | undefined | null) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '0'
  return Number.isInteger(num) ? String(num) : num.toFixed(1)
}

function semesterLabel(semester?: 'AUTUMN' | 'SPRING' | null) {
  if (semester === 'SPRING') return 'Bahorki semestr'
  if (semester === 'AUTUMN') return 'Kuzki semestr'
  return '—'
}

function statusLabel(status?: AllocationStatus) {
  if (status === 'ALLOCATED') return 'Taqsimlangan'
  if (status === 'PARTIAL') return "To'liq taqsimlanmagan"
  return 'Taqsimlanmagan'
}

function statusBadge(status?: AllocationStatus) {
  if (status === 'ALLOCATED') return 'bg-emerald-100 text-emerald-700'
  if (status === 'PARTIAL') return 'bg-amber-100 text-amber-700'
  return 'bg-rose-100 text-rose-700'
}

function rowBg(status?: AllocationStatus) {
  if (status === 'ALLOCATED') return 'bg-emerald-50/70 dark:bg-emerald-500/5'
  if (status === 'PARTIAL') return 'bg-amber-50/70 dark:bg-amber-500/5'
  return 'bg-rose-50/60 dark:bg-rose-500/5'
}

function initials(name: string) {
  const parts = name.trim().split(/\s+/).filter(Boolean)
  if (!parts.length) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return `${parts[0][0] ?? ''}${parts[1][0] ?? ''}`.toUpperCase()
}

function normalizeEmploymentStaff(value?: string | null) {
  const raw = String(value || '').trim().toLowerCase()
  if (!raw) return '—'
  if (raw.includes('asosiy')) return 'Asosiy shtat'
  if (raw.includes('ichki')) return "Ichki o'rindosh"
  if (raw.includes('tashqi')) return "Tashqi o'rindosh"
  if (raw.includes('soat')) return 'Soatbay'
  return value || '—'
}

function allocationEmploymentStaffSummary(
  allocations?: Array<{ teacherName: string; employmentStaffName?: string | null }>,
) {
  if (!allocations?.length) return ''
  return allocations
    .map((item) => `${item.teacherName}: ${normalizeEmploymentStaff(item.employmentStaffName)}`)
    .join(', ')
}

async function loadFilters() {
  try {
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
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params: Record<string, string | number> = {}
    if (selectedDepartmentId.value) params.departmentId = Number(selectedDepartmentId.value)
    else if (selectedFacultyId.value) params.facultyId = Number(selectedFacultyId.value)
    if (selectedSemester.value) params.semester = selectedSemester.value
    if (selectedCourseYear.value) params.courseYear = Number(selectedCourseYear.value)
    if (selectedStatus.value) params.status = selectedStatus.value
    const { data } = await workloadApi.list(params)
    rows.value = unwrapList(data)
  } catch (e) {
    showError(getErrorMessage(e))
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function exportCsv() {
  const header = [
    'Kafedra',
    'Fakultet',
    'Kelgan fakultet',
    'Semestr',
    'Kurs',
    'Fan nomi',
    'Bandlik turi',
    'Fan soati',
    "Ma'ruza",
    'Seminar',
    'Amaliy',
    'Lab',
    'Reyting',
    'Auditorik soat',
    'Mustaqil',
    'Umumiy soat',
    'Guruhlar',
    'Talabalar',
    'Holati',
  ]
  const details = await Promise.all(
    rows.value.map(async (row) => {
      try {
        const { data } = await workloadApi.detail(row.subjectId)
        return data
      } catch {
        return null
      }
    }),
  )
  const lines = rows.value.map((row, index) =>
    [
      row.departmentName || '',
      row.facultyName || '',
      row.sourceFacultyName || '',
      semesterLabel(row.semester),
      row.courseYear ? `${row.courseYear}-kurs` : '',
      row.subjectName || '',
      allocationEmploymentStaffSummary(details[index]?.allocations),
      fanHours(row),
      row.lectureHours ?? 0,
      row.seminarHours ?? 0,
      row.practicalHours ?? 0,
      row.labHours ?? 0,
      row.ratingHours ?? 0,
      row.totalHours ?? 0,
      row.independentStudyHours ?? 0,
      overallHours(row),
      row.groupCount ?? 0,
      row.studentCount ?? 0,
      statusLabel(row.allocationStatus),
    ].join(';'),
  )
  lines.push(
    [
      'Jami',
      '',
      '',
      '',
      '',
      '',
      '',
      workloadTotals.value.fanHours,
      workloadTotals.value.lectureHours,
      workloadTotals.value.seminarHours,
      workloadTotals.value.practicalHours,
      workloadTotals.value.labHours,
      workloadTotals.value.ratingHours,
      workloadTotals.value.totalHours,
      workloadTotals.value.independentStudyHours,
      workloadTotals.value.overallHours,
      workloadTotals.value.groupCount,
      workloadTotals.value.studentCount,
      '',
    ].join(';'),
  )
  const blob = new Blob([[header.join(';'), ...lines].join('\n')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'kafedra-yuklamasi.csv'
  a.click()
  URL.revokeObjectURL(url)
}

function onFacultyChange() {
  selectedDepartmentId.value = ''
  load()
}

async function openSubjectDetail(subjectId: number) {
  try {
    const { data } = await workloadApi.detail(subjectId)
    detail.value = data
    detailOpen.value = true
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

function closeDetail() {
  detailOpen.value = false
  teacherPickerOpen.value = false
  allocateOpen.value = false
}

async function openTeacherPicker() {
  if (!detail.value) return
  teachersLoading.value = true
  teacherSearch.value = ''
  teacherPickerOpen.value = true
  try {
    const { data } = await workloadApi.teachers(detail.value.subjectId)
    teachers.value = unwrapList(data)
  } catch (e) {
    showError(getErrorMessage(e))
    teachers.value = []
  } finally {
    teachersLoading.value = false
  }
}

function openAllocateForm(teacher: TeacherLoad) {
  selectedTeacher.value = teacher
  allocateForm.employmentStaffName = normalizeEmploymentStaff(teacher.employmentStaffName)
  allocateForm.workloadRate = teacher.existingWorkloadRate ?? 1
  allocateForm.lectureHours = orZero(teacher.existingLectureHours)
  allocateForm.seminarHours = orZero(teacher.existingSeminarHours)
  allocateForm.practicalHours = orZero(teacher.existingPracticalHours)
  allocateForm.labHours = orZero(teacher.existingLabHours)
  allocateForm.ratingHours = orZero(teacher.existingRatingHours)
  selectedGroupIds.value = (teacher.existingGroups || []).map((g) => g.id)
  groupSearch.value = ''
  groupDropdownOpen.value = false
  allocateError.value = ''
  teacherPickerOpen.value = false
  allocateOpen.value = true
  void loadGroupsForAllocate()
}

function openGroupDropdown() {
  groupDropdownOpen.value = true
  nextTick(() => groupSearchInputRef.value?.focus())
}

function closeGroupDropdown() {
  groupDropdownOpen.value = false
  groupSearch.value = ''
}

function isGroupSelected(id: number) {
  return selectedGroupIds.value.includes(id)
}

function toggleGroup(group: AllocatedGroup) {
  if (isGroupSelected(group.id)) {
    selectedGroupIds.value = selectedGroupIds.value.filter((id) => id !== group.id)
  } else {
    selectedGroupIds.value = [...selectedGroupIds.value, group.id]
  }
}

function removeSelectedGroup(id: number) {
  selectedGroupIds.value = selectedGroupIds.value.filter((gid) => gid !== id)
}

function loadGroupsForAllocate() {
  const subjectGroups = (detail.value?.groups || []).map((g) => ({
    id: g.id,
    name: g.name,
    studentCount: g.studentCount ?? 0,
  }))
  const allowed = new Set(subjectGroups.map((g) => g.id))
  groupOptions.value = subjectGroups
  selectedGroupIds.value = selectedGroupIds.value.filter((id) => allowed.has(id))
}

function onGroupSelectClickOutside(event: MouseEvent) {
  if (!groupDropdownOpen.value) return
  const el = groupSelectRef.value
  if (!el) return
  const target = event.target
  if (target instanceof Node && !el.contains(target)) {
    closeGroupDropdown()
  }
}

function backToTeachers() {
  closeGroupDropdown()
  allocateOpen.value = false
  teacherPickerOpen.value = true
}

function closeAllocate() {
  closeGroupDropdown()
  allocateOpen.value = false
  selectedTeacher.value = null
}

async function saveAllocation() {
  if (!detail.value || !selectedTeacher.value) return
  saving.value = true
  allocateError.value = ''
  try {
    const { data } = await workloadApi.allocate({
      subjectId: detail.value.subjectId,
      teacherId: selectedTeacher.value.id,
      employmentStaffName: allocateForm.employmentStaffName,
      workloadRate: allocateForm.workloadRate,
      lectureHours: orZero(allocateForm.lectureHours),
      seminarHours: orZero(allocateForm.seminarHours),
      practicalHours: orZero(allocateForm.practicalHours),
      labHours: orZero(allocateForm.labHours),
      ratingHours: orZero(allocateForm.ratingHours),
      groupIds: selectedGroupIds.value,
    })
    detail.value = data
    allocateOpen.value = false
    selectedTeacher.value = null
    await load()
  } catch (e) {
    allocateError.value = getErrorMessage(e)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  // Modal ichida @click.stop bor — capture orqali tashqariga bosilganda yopiladi
  document.addEventListener('mousedown', onGroupSelectClickOutside, true)
  if (auth.isKafedra) {
    selectedFacultyId.value = auth.facultyId ? String(auth.facultyId) : ''
    selectedDepartmentId.value = auth.departmentId ? String(auth.departmentId) : ''
  } else if (auth.isDekan) {
    selectedFacultyId.value = auth.facultyId ? String(auth.facultyId) : ''
  }
  await loadFilters()
  await load()
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onGroupSelectClickOutside, true)
})
</script>

<style scoped>
.filter-field {
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(209 213 219);
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  color: rgb(31 41 55);
}
:global(.dark) .filter-field {
  border-color: rgb(55 65 81);
  background: rgb(17 24 39);
  color: rgba(255, 255, 255, 0.9);
}
</style>
