<template>
  <AdminLayout>
    <div class="space-y-4">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-xl font-bold text-slate-900 dark:text-white">O'qituvchilar yuklamasi</h2>
          <p class="mt-1 text-sm text-slate-500">
            Rolga qarab: admin — barcha; dekan — fakultet; kafedra — o'z kafedrasi
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button
            v-if="canCreateTeacher"
            type="button"
            class="inline-flex items-center gap-2 rounded-xl bg-brand-500 px-4 py-2.5 text-sm font-semibold text-white hover:bg-brand-600"
            @click="openCreateTeacher"
          >
            + O'qituvchi qo'shish
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl bg-teal-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-teal-700"
            @click="exportCsv"
          >
            Excelga yuklash
          </button>
        </div>
      </div>

      <div
        class="flex flex-col gap-3 rounded-2xl border border-slate-100 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800 lg:flex-row lg:items-end"
      >
        <div v-if="auth.hasFullAccess || auth.isDekan" class="min-w-[180px] flex-1">
          <label class="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Fakultet
          </label>
          <select
            v-model="filterFacultyId"
            class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
            :disabled="auth.isDekan"
          >
            <option :value="null">Barcha fakultetlar</option>
            <option v-for="f in faculties" :key="f.id" :value="f.id">{{ f.name }}</option>
          </select>
        </div>
        <div class="min-w-[180px] flex-1">
          <label class="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Kafedra
          </label>
          <select
            v-model="filterDepartmentId"
            class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
            :disabled="auth.isKafedra"
          >
            <option :value="null">Barcha kafedralar</option>
            <option v-for="d in filteredDepartments" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
        </div>
        <div class="min-w-[220px] flex-[1.4]">
          <label class="mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-400">
            Qidiruv
          </label>
          <input
            v-model="search"
            type="search"
            placeholder="O'qituvchini izlash"
            class="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm dark:border-slate-600 dark:bg-slate-900"
          />
        </div>
      </div>

      <div
        class="overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800"
      >
        <div v-if="loading" class="px-5 py-10 text-sm text-slate-500">Yuklanmoqda...</div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="border-b border-slate-100 bg-slate-50/80 dark:border-slate-700 dark:bg-slate-900/40">
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">#</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">O'qituvchi</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Kafedra</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Ish stavkasi</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Fanlar</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Ma'ruza</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Amaliy</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Lab</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Seminar</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Reyting</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Auditorik soat</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Mustaqil</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Guruhlar</th>
                <th class="px-3 py-3 text-left text-xs font-semibold text-slate-500">Talabalar soni</th>
                <th class="px-3 py-3 text-right text-xs font-semibold text-slate-500">Amallar</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-700">
              <tr
                v-for="(row, index) in filteredRows"
                :key="row.id"
                class="hover:bg-slate-50/70 dark:hover:bg-white/5"
              >
                <td class="px-3 py-3 text-slate-500">{{ index + 1 }}</td>
                <td class="px-3 py-3 font-medium text-slate-800 dark:text-white">
                  {{ row.fullName || row.name }}
                </td>
                <td class="px-3 py-3 text-slate-600 dark:text-slate-300">
                  {{ row.departmentName || '—' }}
                </td>
                <td class="px-3 py-3 text-slate-600">{{ formatStavka(row.stavka) }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.subjectCount ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.lectureHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.practicalHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.labHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.seminarHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.ratingHours ?? 0 }}</td>
                <td class="px-3 py-3 font-semibold text-slate-800 dark:text-white">
                  {{ row.totalHours ?? 0 }}
                </td>
                <td class="px-3 py-3 text-slate-600">{{ row.independentHours ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.groupCount ?? 0 }}</td>
                <td class="px-3 py-3 text-slate-600">{{ row.studentCount ?? 0 }}</td>
                <td class="px-3 py-3">
                  <div class="relative flex justify-end">
                    <button
                      type="button"
                      class="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 shadow-sm transition hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800"
                      title="Amallar"
                      @click.stop="toggleActionsMenu(row.id)"
                    >
                      <SlidersHorizontal class="size-4" :stroke-width="2" />
                    </button>
                    <div
                      v-if="openActionsId === row.id"
                      class="absolute right-0 top-full z-50 mt-1.5 w-56 rounded-xl border border-slate-100 bg-white p-1.5 shadow-lg dark:border-slate-700 dark:bg-slate-900"
                      @click.stop
                    >
                      <button
                        type="button"
                        class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-indigo-700 transition hover:bg-indigo-50 dark:text-indigo-300 dark:hover:bg-indigo-500/10"
                        @click="openTeacherDrawer(row)"
                      >
                        <Eye class="size-4 shrink-0" :stroke-width="2" />
                        Ko'rish
                      </button>
                      <button
                        v-if="canEditTeacher"
                        type="button"
                        class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-amber-700 transition hover:bg-amber-50 dark:text-amber-300 dark:hover:bg-amber-500/10"
                        @click="openEditTeacher(row)"
                      >
                        <Pencil class="size-4 shrink-0" :stroke-width="2" />
                        Tahrirlash
                      </button>
                      <button
                        type="button"
                        class="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium text-teal-700 transition hover:bg-teal-50 disabled:opacity-50 dark:text-teal-300 dark:hover:bg-teal-500/10"
                        :disabled="exportingId === row.id"
                        @click="exportTeacherAllocation(row)"
                      >
                        <FileSpreadsheet class="size-4 shrink-0" :stroke-width="2" />
                        {{ exportingId === row.id ? 'Yuklanmoqda...' : 'Dars taqsimoti (Excel)' }}
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredRows.length" class="bg-slate-50/80 font-semibold dark:bg-slate-900/40">
                <td colspan="4" class="px-3 py-3 text-slate-700 dark:text-slate-200">Jami</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.subjectCount }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.lectureHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.practicalHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.labHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.seminarHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.ratingHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.totalHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.independentHours }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.groupCount }}</td>
                <td class="px-3 py-3 text-slate-700 dark:text-slate-200">{{ totals.studentCount }}</td>
                <td class="px-3 py-3"></td>
              </tr>
              <tr v-if="!filteredRows.length">
                <td colspan="15" class="px-5 py-10 text-center text-slate-500">Ma'lumot topilmadi</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <Drawer v-if="drawerOpen && selectedRow" size="xl" @close="closeTeacherDrawer">
      <template #title>O'qituvchi yuklamasi</template>
      <template #subtitle>Fanlar, guruhlar, haftalik jadval va semestr taqsimoti</template>

      <div class="space-y-5">
        <div
          class="rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-900/40"
        >
          <div class="flex flex-col gap-4 sm:flex-row sm:items-start">
            <div
              class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-brand-50 text-lg font-bold text-brand-600 dark:bg-brand-500/15 dark:text-brand-300"
            >
              <img
                v-if="selectedTeacher?.image"
                :src="selectedTeacher.image"
                :alt="teacherDisplayName"
                class="h-16 w-16 rounded-full object-cover"
              />
              <span v-else>{{ teacherInitials }}</span>
            </div>
            <div class="min-w-0 flex-1">
              <h4 class="text-lg font-semibold text-slate-900 dark:text-white">
                {{ teacherDisplayName }}
              </h4>
              <p class="mt-0.5 text-sm text-slate-500">
                {{ selectedTeacher?.staffPositionName || selectedTeacher?.academicRankName || '—' }}
                <span v-if="selectedRow.departmentName"> • {{ selectedRow.departmentName }}</span>
              </p>
              <div class="mt-3 flex flex-wrap gap-2">
                <span
                  v-if="selectedRow.facultyName"
                  class="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300"
                >
                  {{ selectedRow.facultyName }}
                </span>
                <span
                  v-if="selectedTeacher?.academicDegreeName"
                  class="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300"
                >
                  {{ selectedTeacher.academicDegreeName }}
                </span>
                <span
                  class="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300"
                >
                  {{ formatStavka(selectedRow.stavka) }} stavka
                </span>
                <span
                  class="rounded-full bg-brand-50 px-2.5 py-1 text-xs font-medium text-brand-700 dark:bg-brand-500/15 dark:text-brand-300"
                >
                  {{ drawerAllocatedHours }} soat
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="flex rounded-xl bg-slate-100 p-1 dark:bg-slate-800">
          <button
            v-for="tab in drawerTabs"
            :key="tab.id"
            type="button"
            class="flex-1 rounded-lg px-3 py-2 text-sm font-semibold transition"
            :class="
              drawerTab === tab.id
                ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-700 dark:text-white'
                : 'text-slate-500 hover:text-slate-700 dark:text-slate-400'
            "
            @click="drawerTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="drawerLoading" class="py-10 text-center text-sm text-slate-500">
          Yuklanmoqda...
        </div>

        <template v-else-if="drawerTab === 'fanlar'">
          <div class="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-700">
            <table class="min-w-full text-sm">
              <thead>
                <tr class="border-b border-slate-100 bg-slate-50/80 dark:border-slate-700 dark:bg-slate-800/60">
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Fan</th>
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Ma'ruza</th>
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Amaliy</th>
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Lab</th>
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Seminar</th>
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Reyting</th>
                  <th class="px-3 py-2.5 text-left text-xs font-semibold text-slate-500">Jami</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100 dark:divide-slate-700">
                <tr v-for="item in allocations" :key="item.allocationId || item.subjectId">
                  <td class="px-3 py-3">
                    <p class="font-medium text-slate-800 dark:text-white">
                      {{ item.subjectName || '—' }}
                    </p>
                    <p class="text-xs text-slate-400">
                      {{ item.subjectCode || '' }}
                      <span v-if="item.semester"> · {{ semesterLabel(item.semester) }}</span>
                      <span v-if="item.courseYear"> · {{ item.courseYear }}-kurs</span>
                    </p>
                    <p v-if="item.groups?.length" class="mt-0.5 text-xs text-brand-600 dark:text-brand-400">
                      {{ item.groups.map((g) => g.name).join(', ') }}
                    </p>
                  </td>
                  <td class="px-3 py-3 text-slate-600">{{ item.lectureHours ?? 0 }}</td>
                  <td class="px-3 py-3 text-slate-600">{{ item.practicalHours ?? 0 }}</td>
                  <td class="px-3 py-3 text-slate-600">{{ item.labHours ?? 0 }}</td>
                  <td class="px-3 py-3 text-slate-600">{{ item.seminarHours ?? 0 }}</td>
                  <td class="px-3 py-3 text-slate-600">{{ item.ratingHours ?? 0 }}</td>
                  <td class="px-3 py-3 font-semibold text-brand-600 dark:text-brand-400">
                    {{ item.totalHours ?? 0 }}
                  </td>
                </tr>
                <tr v-if="allocations.length" class="bg-slate-50/80 font-semibold dark:bg-slate-800/50">
                  <td class="px-3 py-3 text-slate-700 dark:text-slate-200">Jami</td>
                  <td class="px-3 py-3">{{ allocationTotals.lectureHours }}</td>
                  <td class="px-3 py-3">{{ allocationTotals.practicalHours }}</td>
                  <td class="px-3 py-3">{{ allocationTotals.labHours }}</td>
                  <td class="px-3 py-3">{{ allocationTotals.seminarHours }}</td>
                  <td class="px-3 py-3">{{ allocationTotals.ratingHours }}</td>
                  <td class="px-3 py-3 text-brand-600 dark:text-brand-400">
                    {{ allocationTotals.totalHours }}
                  </td>
                </tr>
                <tr v-if="!allocations.length">
                  <td colspan="7" class="px-3 py-8 text-center text-slate-500">
                    Bu o'qituvchiga hali fan taqsimlanmagan
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <template v-else-if="drawerTab === 'jadval'">
          <div class="space-y-3">
            <div
              v-for="group in semesterGroups"
              :key="group.key"
              class="rounded-xl border border-slate-200 p-4 dark:border-slate-700"
            >
              <h5 class="mb-3 text-sm font-semibold text-slate-800 dark:text-white">
                {{ group.label }}
              </h5>
              <div class="space-y-2">
                <div
                  v-for="item in group.items"
                  :key="item.allocationId || item.subjectId"
                  class="flex items-center justify-between gap-3 rounded-lg bg-slate-50 px-3 py-2.5 dark:bg-slate-800/60"
                >
                  <div class="min-w-0">
                    <p class="truncate text-sm font-medium text-slate-800 dark:text-white">
                      {{ item.subjectName }}
                    </p>
                    <p class="text-xs text-slate-400">
                      {{
                        item.groups?.length
                          ? item.groups.map((g) => g.name).join(', ')
                          : 'Guruh yo‘q'
                      }}
                      · {{ item.courseYear ? `${item.courseYear}-kurs` : 'Kurs —' }}
                    </p>
                  </div>
                  <span class="shrink-0 text-sm font-semibold text-brand-600 dark:text-brand-400">
                    {{ item.totalHours ?? 0 }} soat
                  </span>
                </div>
              </div>
            </div>
            <p
              v-if="!semesterGroups.length"
              class="rounded-xl border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-500 dark:border-slate-700"
            >
              Semestr taqsimoti uchun ma'lumot yo'q
            </p>
          </div>
        </template>

        <template v-else>
          <div
            v-if="allocations.length"
            class="rounded-xl border border-slate-200 p-4 dark:border-slate-700"
          >
            <VueApexCharts type="bar" height="280" :options="chartOptions" :series="chartSeries" />
          </div>
          <p
            v-else
            class="rounded-xl border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-500 dark:border-slate-700"
          >
            Grafik uchun taqsimlangan soatlar yo'q
          </p>
        </template>
      </div>

      <template #footer>
        <div class="flex flex-wrap justify-end gap-2">
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-200 dark:hover:bg-slate-800"
            :disabled="exportingId === selectedRow.id || !allocations.length"
            @click="exportTeacherAllocation(selectedRow)"
          >
            <FileSpreadsheet class="size-4" :stroke-width="2" />
            Excelga yuklash
          </button>
        </div>
      </template>
    </Drawer>

    <Modal v-if="teacherModalOpen" full-screen-backdrop @close="closeTeacherModal">
      <template #body>
        <div
          class="relative w-full max-w-lg rounded-2xl bg-white p-6 dark:bg-gray-900"
          @click.stop
        >
          <h3 class="mb-4 text-lg font-semibold text-gray-800 dark:text-white/90">
            {{ editingTeacherId ? "O'qituvchini tahrirlash" : "Yangi o'qituvchi" }}
          </h3>
          <form class="space-y-4" @submit.prevent="saveTeacher">
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">F.I.O.</label>
              <input
                v-model="teacherForm.name"
                required
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Fakultet</label>
              <select
                v-model="teacherForm.facultyId"
                required
                class="filter-field h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm dark:border-gray-700 dark:bg-gray-900"
                :disabled="auth.isDekan || auth.isKafedra"
                @change="onTeacherFacultyChange"
              >
                <option value="">Tanlang</option>
                <option v-for="f in faculties" :key="f.id" :value="String(f.id)">
                  {{ f.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Kafedra</label>
              <select
                v-model="teacherForm.departmentId"
                required
                class="filter-field h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm dark:border-gray-700 dark:bg-gray-900"
                :disabled="auth.isKafedra"
              >
                <option value="">Tanlang</option>
                <option v-for="d in teacherFormDepartments" :key="d.id" :value="String(d.id)">
                  {{ d.name }}
                </option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Lavozim</label>
              <select
                v-model="teacherForm.staffPosition"
                required
                class="filter-field h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm dark:border-gray-700 dark:bg-gray-900"
              >
                <option value="">Tanlang</option>
                <option v-for="pos in staffPositionOptions" :key="pos" :value="pos">
                  {{ pos }}
                </option>
              </select>
            </div>
            <div>
              <label class="mb-1 block text-sm text-gray-600 dark:text-gray-400">Stavka</label>
              <input
                v-model="teacherForm.stavka"
                type="number"
                min="0"
                max="2"
                step="0.25"
                placeholder="masalan: 1 yoki 0.5"
                class="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
            <div v-if="teacherFormError" class="text-sm text-error-600">{{ teacherFormError }}</div>
            <div class="flex justify-end gap-2">
              <button
                type="button"
                class="rounded-lg border border-gray-300 px-4 py-2.5 text-sm text-gray-700 dark:border-gray-700 dark:text-gray-300"
                @click="closeTeacherModal"
              >
                Bekor
              </button>
              <button
                type="submit"
                class="rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-60"
                :disabled="teacherSaving"
              >
                Saqlash
              </button>
            </div>
          </form>
        </div>
      </template>
    </Modal>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { Eye, FileSpreadsheet, Pencil, SlidersHorizontal } from 'lucide-vue-next'
import VueApexCharts from 'vue3-apexcharts'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Drawer from '@/components/ui/Drawer.vue'
import Modal from '@/components/ui/Modal.vue'
import { departmentApi, facultyApi, teacherApi } from '@/api/catalog'
import { getErrorMessage } from '@/api/http'
import { showError, showSuccess, showWarning } from '@/utils/swal'
import { useAuthStore } from '@/stores/auth'
import type { NamedEntity } from '@/types/api'
import type { TeacherWorkloadAllocation, TeacherWorkloadRow } from '@/types/talabnoma'
import api from '@/api/http'

type DrawerTab = 'fanlar' | 'jadval' | 'grafik'

type TeacherProfile = NamedEntity & {
  fullName?: string
  image?: string | null
  staffPositionName?: string | null
  academicRankName?: string | null
  academicDegreeName?: string | null
  departmentName?: string | null
  facultyName?: string | null
  stavka?: number | null
}

const auth = useAuthStore()
const rows = ref<TeacherWorkloadRow[]>([])
const faculties = ref<NamedEntity[]>([])
const departments = ref<(NamedEntity & { facultyId?: number })[]>([])
const loading = ref(false)
const search = ref('')
const filterFacultyId = ref<number | null>(auth.facultyId)
const filterDepartmentId = ref<number | null>(auth.departmentId)
const openActionsId = ref<number | null>(null)
const exportingId = ref<number | null>(null)

const drawerOpen = ref(false)
const drawerLoading = ref(false)
const drawerTab = ref<DrawerTab>('fanlar')
const selectedRow = ref<TeacherWorkloadRow | null>(null)
const selectedTeacher = ref<TeacherProfile | null>(null)
const allocations = ref<TeacherWorkloadAllocation[]>([])

const teacherModalOpen = ref(false)
const teacherSaving = ref(false)
const teacherFormError = ref('')
const editingTeacherId = ref<number | null>(null)
const teacherForm = reactive({
  name: '',
  facultyId: '' as string,
  departmentId: '' as string,
  staffPosition: '',
  stavka: '' as string,
})

const DEFAULT_POSITIONS = [
  "O'qituvchi",
  'Assistent',
  "Katta o'qituvchi",
  'Dotsent',
  'Professor',
  'Kafedra mudiri',
  "Stajer-o'qituvchi",
]

const canCreateTeacher = computed(
  () => auth.hasFullAccess || auth.isDekan || auth.isKafedra,
)
const canEditTeacher = computed(
  () => auth.hasFullAccess || auth.isDekan || auth.isKafedra,
)

const drawerTabs: { id: DrawerTab; label: string }[] = [
  { id: 'fanlar', label: 'Fanlar' },
  { id: 'jadval', label: 'Jadval' },
  { id: 'grafik', label: 'Grafik' },
]

const filteredDepartments = computed(() => {
  if (!filterFacultyId.value) return departments.value
  return departments.value.filter((d) => d.facultyId === filterFacultyId.value)
})

const teacherFormDepartments = computed(() => {
  if (!teacherForm.facultyId) return departments.value
  const facultyId = Number(teacherForm.facultyId)
  return departments.value.filter((d) => d.facultyId === facultyId)
})

const staffPositionOptions = computed(() => {
  const fromForm = teacherForm.staffPosition?.trim()
  const fromTeacher = selectedTeacher.value?.staffPositionName?.trim()
  const extras = [fromForm, fromTeacher].filter((v): v is string => Boolean(v))
  return [...new Set([...DEFAULT_POSITIONS, ...extras])].sort((a, b) => a.localeCompare(b, 'uz'))
})

const filteredRows = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return rows.value
  return rows.value.filter((r) =>
    `${r.fullName || ''} ${r.name || ''} ${r.departmentName || ''}`.toLowerCase().includes(q),
  )
})

const totals = computed(() =>
  filteredRows.value.reduce(
    (acc, row) => {
      acc.subjectCount += row.subjectCount ?? 0
      acc.lectureHours += row.lectureHours ?? 0
      acc.practicalHours += row.practicalHours ?? 0
      acc.labHours += row.labHours ?? 0
      acc.seminarHours += row.seminarHours ?? 0
      acc.ratingHours += row.ratingHours ?? 0
      acc.totalHours += row.totalHours ?? 0
      acc.independentHours += row.independentHours ?? 0
      acc.groupCount += row.groupCount ?? 0
      acc.studentCount += row.studentCount ?? 0
      return acc
    },
    {
      subjectCount: 0,
      lectureHours: 0,
      practicalHours: 0,
      labHours: 0,
      seminarHours: 0,
      ratingHours: 0,
      totalHours: 0,
      independentHours: 0,
      groupCount: 0,
      studentCount: 0,
    },
  ),
)

const teacherDisplayName = computed(
  () =>
    selectedTeacher.value?.fullName ||
    selectedTeacher.value?.name ||
    selectedRow.value?.fullName ||
    selectedRow.value?.name ||
    '—',
)

const teacherInitials = computed(() => {
  const parts = teacherDisplayName.value.split(/\s+/).filter(Boolean)
  if (!parts.length) return '—'
  return parts
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() || '')
    .join('')
})

const allocationTotals = computed(() =>
  allocations.value.reduce<{
    lectureHours: number
    practicalHours: number
    labHours: number
    seminarHours: number
    ratingHours: number
    totalHours: number
    credit: number
    groupCount: number
  }>(
    (acc, item) => {
      acc.lectureHours += item.lectureHours ?? 0
      acc.practicalHours += item.practicalHours ?? 0
      acc.labHours += item.labHours ?? 0
      acc.seminarHours += item.seminarHours ?? 0
      acc.ratingHours += item.ratingHours ?? 0
      acc.totalHours += item.totalHours ?? 0
      acc.credit += item.credit ?? 0
      acc.groupCount += item.groupCount ?? 0
      return acc
    },
    {
      lectureHours: 0,
      practicalHours: 0,
      labHours: 0,
      seminarHours: 0,
      ratingHours: 0,
      totalHours: 0,
      credit: 0,
      groupCount: 0,
    },
  ),
)

const drawerAllocatedHours = computed(() => {
  if (allocations.value.length) return allocationTotals.value.totalHours
  return selectedRow.value?.totalHours ?? 0
})

const semesterGroups = computed(() => {
  const map = new Map<string, { key: string; label: string; items: TeacherWorkloadAllocation[] }>()
  for (const item of allocations.value) {
    const key = item.semester || 'UNKNOWN'
    const label =
      key === 'SPRING' ? 'Bahorgi semestr' : key === 'AUTUMN' ? 'Kuzgi semestr' : 'Semestr belgilanmagan'
    if (!map.has(key)) map.set(key, { key, label, items: [] })
    map.get(key)!.items.push(item)
  }
  return [...map.values()]
})

const chartSeries = computed(() => [
  {
    name: 'Soat',
    data: [
      allocationTotals.value.lectureHours,
      allocationTotals.value.practicalHours,
      allocationTotals.value.labHours,
      allocationTotals.value.seminarHours,
      allocationTotals.value.ratingHours,
    ],
  },
])

const chartOptions = computed(() => ({
  chart: {
    toolbar: { show: false },
    fontFamily: 'inherit',
  },
  plotOptions: {
    bar: {
      borderRadius: 6,
      columnWidth: '45%',
      distributed: true,
    },
  },
  colors: ['#465fff', '#12b76a', '#f79009', '#7a5af8', '#ee46bc'],
  dataLabels: { enabled: true },
  legend: { show: false },
  xaxis: {
    categories: ["Ma'ruza", 'Amaliy', 'Lab', 'Seminar', 'Reyting'],
    labels: { style: { colors: '#667085', fontSize: '12px' } },
  },
  yaxis: {
    labels: { style: { colors: '#667085', fontSize: '12px' } },
  },
  grid: { borderColor: '#eaecf0', strokeDashArray: 4 },
  tooltip: { theme: 'light' },
}))

function formatStavka(v?: number | null) {
  if (v == null) return '—'
  return Number(v).toFixed(2)
}

function semesterLabel(semester?: string | null) {
  if (semester === 'SPRING') return 'Bahorgi'
  if (semester === 'AUTUMN') return 'Kuzgi'
  return ''
}

function unwrapList<T>(data: T[] | { content?: T[]; data?: T[] }): T[] {
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.content)) return data.content
  if (Array.isArray(data?.data)) return data.data
  return []
}

function toggleActionsMenu(id: number) {
  openActionsId.value = openActionsId.value === id ? null : id
}

function closeActionsMenu() {
  openActionsId.value = null
}

function onDocumentClick() {
  closeActionsMenu()
}

function safeFileName(name: string) {
  return name
    .trim()
    .replace(/[\\/:*?"<>|]+/g, '')
    .replace(/\s+/g, '-')
    .slice(0, 80)
}

function downloadCsv(filename: string, lines: string[]) {
  const blob = new Blob([['\uFEFF', lines.join('\n')].join('')], {
    type: 'text/csv;charset=utf-8;',
  })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

function parseStavka(raw: string | number | null | undefined): number | null {
  if (raw === null || raw === undefined || raw === '') return null
  const value = Number(String(raw).trim())
  return Number.isFinite(value) ? value : null
}

function resetTeacherForm() {
  teacherForm.name = ''
  teacherForm.facultyId = auth.facultyId ? String(auth.facultyId) : filterFacultyId.value ? String(filterFacultyId.value) : ''
  teacherForm.departmentId = auth.isKafedra && auth.departmentId
    ? String(auth.departmentId)
    : filterDepartmentId.value
      ? String(filterDepartmentId.value)
      : ''
  teacherForm.staffPosition = ''
  teacherForm.stavka = ''
  teacherFormError.value = ''
}

function onTeacherFacultyChange() {
  if (!auth.isKafedra) teacherForm.departmentId = ''
}

async function openCreateTeacher() {
  editingTeacherId.value = null
  resetTeacherForm()
  teacherModalOpen.value = true
}

async function openEditTeacher(row: TeacherWorkloadRow) {
  closeActionsMenu()
  editingTeacherId.value = row.id
  teacherFormError.value = ''
  try {
    const { data } = await teacherApi.getById(row.id)
    const t = data as TeacherProfile & {
      departmentId?: number
      facultyId?: number
      staffPositionName?: string | null
      stavka?: number | null
    }
    teacherForm.name = t.fullName || t.name || row.fullName || row.name || ''
    teacherForm.facultyId = t.facultyId
      ? String(t.facultyId)
      : row.facultyId
        ? String(row.facultyId)
        : ''
    teacherForm.departmentId = t.departmentId
      ? String(t.departmentId)
      : row.departmentId
        ? String(row.departmentId)
        : ''
    teacherForm.staffPosition = t.staffPositionName || ''
    teacherForm.stavka =
      t.stavka === null || t.stavka === undefined ? '' : String(t.stavka)
    teacherModalOpen.value = true
  } catch (e) {
    showError(getErrorMessage(e))
  }
}

function closeTeacherModal() {
  teacherModalOpen.value = false
  editingTeacherId.value = null
  teacherFormError.value = ''
}

async function saveTeacher() {
  if (!teacherForm.facultyId || !teacherForm.departmentId || !teacherForm.staffPosition) {
    teacherFormError.value = 'Fakultet, kafedra va lavozimni tanlang'
    return
  }
  teacherSaving.value = true
  teacherFormError.value = ''
  try {
    const payload = {
      name: teacherForm.name.trim(),
      departmentId: Number(teacherForm.departmentId),
      staffPositionName: teacherForm.staffPosition,
      stavka: parseStavka(teacherForm.stavka),
    }
    if (editingTeacherId.value) {
      await teacherApi.update(editingTeacherId.value, payload)
      showSuccess("O'qituvchi yangilandi")
    } else {
      await teacherApi.create(payload)
      showSuccess("O'qituvchi qo'shildi")
    }
    closeTeacherModal()
    await load()
  } catch (e) {
    teacherFormError.value = getErrorMessage(e)
  } finally {
    teacherSaving.value = false
  }
}

async function loadCatalog() {
  try {
    const [fRes, dRes] = await Promise.all([
      facultyApi.list(auth.catalogScopeParams()),
      departmentApi.list(auth.catalogScopeParams()),
    ])
    faculties.value = unwrapList(fRes.data)
    departments.value = unwrapList(dRes.data) as (NamedEntity & { facultyId?: number })[]
  } catch {
    faculties.value = []
    departments.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const params: Record<string, number> = { ...auth.catalogScopeParams() }
    if (auth.hasFullAccess) {
      if (filterFacultyId.value) params.facultyId = filterFacultyId.value
      if (filterDepartmentId.value) params.departmentId = filterDepartmentId.value
    } else if (auth.isDekan && filterDepartmentId.value) {
      params.departmentId = filterDepartmentId.value
    }
    const { data } = await api.get<TeacherWorkloadRow[]>('/teachers/workload-summary', { params })
    rows.value = unwrapList(data)
  } catch (e) {
    showError(getErrorMessage(e))
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function openTeacherDrawer(row: TeacherWorkloadRow) {
  closeActionsMenu()
  selectedRow.value = row
  selectedTeacher.value = null
  allocations.value = []
  drawerTab.value = 'fanlar'
  drawerOpen.value = true
  drawerLoading.value = true
  try {
    const [teacherRes, allocRes] = await Promise.all([
      teacherApi.getById(row.id),
      api.get<TeacherWorkloadAllocation[]>(`/teachers/${row.id}/workload-allocations`),
    ])
    selectedTeacher.value = teacherRes.data as TeacherProfile
    allocations.value = unwrapList(allocRes.data)
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    drawerLoading.value = false
  }
}

function closeTeacherDrawer() {
  drawerOpen.value = false
  selectedRow.value = null
  selectedTeacher.value = null
  allocations.value = []
}

async function exportTeacherAllocation(row: TeacherWorkloadRow) {
  exportingId.value = row.id
  try {
    const { data } = await api.get<TeacherWorkloadAllocation[]>(
      `/teachers/${row.id}/workload-allocations`,
    )
    const items = unwrapList(data)
    if (!items.length) {
      showWarning("Bu o'qituvchiga hali fan taqsimlanmagan")
      return
    }

    const teacherName = row.fullName || row.name || `teacher-${row.id}`
    const header = [
      '#',
      'Fan kodi',
      'Fan nomi',
      'Kafedra',
      'Semestr',
      'Kurs',
      'Talaba',
      'Guruh',
      "Ma'ruza",
      'Amaliy',
      'Lab',
      'Seminar',
      'Reyting',
      'Umumiy soat',
    ]

    let sumLecture = 0
    let sumPractical = 0
    let sumLab = 0
    let sumSeminar = 0
    let sumRating = 0
    let sumTotal = 0
    let sumGroups = 0
    let sumStudents = 0

    const lines = items.map((item, index) => {
      const lecture = item.lectureHours ?? 0
      const practical = item.practicalHours ?? 0
      const lab = item.labHours ?? 0
      const seminar = item.seminarHours ?? 0
      const rating = item.ratingHours ?? 0
      const totalHours = item.totalHours ?? lecture + practical + lab + seminar + rating
      const groups = item.groupCount ?? 0
      const students = item.studentCount ?? 0

      sumLecture += lecture
      sumPractical += practical
      sumLab += lab
      sumSeminar += seminar
      sumRating += rating
      sumTotal += totalHours
      sumGroups += groups
      sumStudents += students

      return [
        index + 1,
        item.subjectCode || '',
        item.subjectName || '',
        item.departmentName || '',
        semesterLabel(item.semester),
        item.courseYear ? `${item.courseYear}-kurs` : '',
        students,
        groups,
        lecture,
        practical,
        lab,
        seminar,
        rating,
        totalHours,
      ].join(';')
    })

    lines.push(
      [
        '',
        '',
        'Jami',
        '',
        '',
        '',
        sumStudents,
        sumGroups,
        sumLecture,
        sumPractical,
        sumLab,
        sumSeminar,
        sumRating,
        sumTotal,
      ].join(';'),
    )

    const meta = [
      `O'qituvchi;${teacherName}`,
      `Kafedra;${row.departmentName || ''}`,
      `Fanlar soni;${items.length}`,
      '',
      header.join(';'),
      ...lines,
    ]

    downloadCsv(`${safeFileName(teacherName)}-dars-taqsimoti.csv`, meta)
  } catch (e) {
    showError(getErrorMessage(e))
  } finally {
    exportingId.value = null
    closeActionsMenu()
  }
}

function exportCsv() {
  const header = [
    '#',
    "O'qituvchi",
    'Kafedra',
    'Stavka',
    'Fanlar',
    "Ma'ruza",
    'Amaliy',
    'Lab',
    'Seminar',
    'Reyting',
    'Auditorik soat',
    'Mustaqil',
    'Guruhlar',
    'Talabalar soni',
  ]
  const lines = filteredRows.value.map((r, i) =>
    [
      i + 1,
      r.fullName || r.name,
      r.departmentName || '',
      r.stavka ?? '',
      r.subjectCount ?? 0,
      r.lectureHours ?? 0,
      r.practicalHours ?? 0,
      r.labHours ?? 0,
      r.seminarHours ?? 0,
      r.ratingHours ?? 0,
      r.totalHours ?? 0,
      r.independentHours ?? 0,
      r.groupCount ?? 0,
      r.studentCount ?? 0,
    ].join(';'),
  )
  lines.push(
    [
      '',
      'Jami',
      '',
      '',
      totals.value.subjectCount,
      totals.value.lectureHours,
      totals.value.practicalHours,
      totals.value.labHours,
      totals.value.seminarHours,
      totals.value.ratingHours,
      totals.value.totalHours,
      totals.value.independentHours,
      totals.value.groupCount,
      totals.value.studentCount,
    ].join(';'),
  )
  downloadCsv('oqituvchilar-yuklamasi.csv', [header.join(';'), ...lines])
}

watch(filterFacultyId, () => {
  if (!auth.isKafedra) filterDepartmentId.value = null
})

watch([filterFacultyId, filterDepartmentId], () => {
  void load()
})

onMounted(async () => {
  document.addEventListener('click', onDocumentClick)
  await loadCatalog()
  await load()
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
})
</script>
