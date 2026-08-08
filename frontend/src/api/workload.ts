import api from './http'

export type AllocationStatus = 'UNALLOCATED' | 'PARTIAL' | 'ALLOCATED'

export interface HourBucket {
  total: number
  allocated: number
  remaining: number
}

export interface AllocatedGroup {
  id: number
  name: string
  studentCount?: number
}

export interface WorkloadRow {
  subjectId: number
  subjectName: string
  subjectCode?: string
  departmentId?: number
  departmentName?: string
  facultyId?: number
  facultyName?: string
  sourceFacultyId?: number
  sourceFacultyName?: string
  talabnomaCode?: string
  semester?: 'AUTUMN' | 'SPRING'
  courseYear?: number
  lectureHours?: number
  seminarHours?: number
  practicalHours?: number
  labHours?: number
  ratingHours?: number
  independentStudyHours?: number
  totalHours?: number
  allocatedHours?: number
  remainingHours?: number
  groupCount?: number
  studentCount?: number
  allocationStatus?: AllocationStatus
}

export interface WorkloadDetail {
  subjectId: number
  subjectName: string
  subjectCode?: string
  departmentId?: number
  departmentName?: string
  facultyId?: number
  facultyName?: string
  sourceFacultyId?: number
  sourceFacultyName?: string
  talabnomaCode?: string
  semester?: 'AUTUMN' | 'SPRING'
  credit?: number
  totalSubjectHours?: number
  independentStudyHours?: number
  totalHours?: number
  allocatedHours?: number
  remainingHours?: number
  allocationStatus?: AllocationStatus
  lecture?: HourBucket
  seminar?: HourBucket
  practical?: HourBucket
  lab?: HourBucket
  rating?: HourBucket
  groups?: AllocatedGroup[]
  allocations?: Array<{
    id: number
    teacherId: number
    teacherName: string
    employmentStaffName?: string
    workloadRate?: number
    lectureHours: number
    seminarHours: number
    practicalHours: number
    labHours: number
    ratingHours: number
    totalHours: number
    groups?: AllocatedGroup[]
  }>
}

export interface TeacherLoad {
  id: number
  name: string
  departmentName?: string
  staffPositionName?: string
  employmentStaffName?: string
  existingWorkloadRate?: number
  totalAssignedHours?: number
  loadLabel?: string
  existingLectureHours?: number
  existingSeminarHours?: number
  existingPracticalHours?: number
  existingLabHours?: number
  existingRatingHours?: number
  existingGroups?: AllocatedGroup[]
}

export interface DashboardHours {
  totalHours: number
  allocatedHours: number
  unallocatedHours: number
  byFaculty?: HoursByGroup[]
  byDepartment?: HoursByGroup[]
}

export interface HoursByGroup {
  id: number
  name: string
  facultyId?: number
  facultyName?: string
  fanHours?: number
  lectureHours?: number
  seminarHours?: number
  practicalHours?: number
  labHours?: number
  ratingHours?: number
  auditoriyHours?: number
  independentHours?: number
  overallHours?: number
  totalHours: number
  allocatedHours: number
  unallocatedHours: number
}

type ListParams = Record<string, string | number | undefined>

export const workloadApi = {
  list: (params?: ListParams) => api.get<WorkloadRow[]>('/workloads', { params }),
  detail: (subjectId: number) => api.get<WorkloadDetail>(`/workloads/${subjectId}`),
  teachers: (subjectId: number) => api.get<TeacherLoad[]>(`/workloads/${subjectId}/teachers`),
  allocate: (payload: Record<string, unknown>) =>
    api.post<WorkloadDetail>('/workloads/allocate', payload),
  dashboardHours: () => api.get<DashboardHours>('/workloads/dashboard-hours'),
}
