export type TalabnomaStatus = 'NEW' | 'ACCEPTED' | 'REJECTED' | 'PARTIAL' | 'ALLOCATED'

export interface AllocatedTeacherInfo {
  teacherId: number
  teacherName: string
  hours: number
}

export interface Talabnoma {
  id: number
  code: string
  fromFacultyId?: number
  fromFacultyName?: string
  toDepartmentId?: number
  toDepartmentName?: string
  toFacultyId?: number
  toFacultyName?: string
  subjectName: string
  subjectCode?: string
  academicYearId?: number
  academicYearName?: string
  semester?: 'AUTUMN' | 'SPRING'
  lectureHours?: number
  practicalHours?: number
  labHours?: number
  seminarHours?: number
  ratingHours?: number
  totalHours?: number
  allocatedHours?: number
  requestStatus: TalabnomaStatus
  note?: string
  rejectReason?: string
  linkedSubjectId?: number
  createdByName?: string
  createdAt?: string
  allocatedTeachers?: AllocatedTeacherInfo[]
}

export interface TalabnomaStats {
  total: number
  pending: number
  accepted: number
  rejected: number
  allocated: number
}

export interface TeacherWorkloadRow {
  id: number
  name: string
  fullName?: string
  departmentId?: number
  departmentName?: string
  facultyId?: number
  facultyName?: string
  stavka?: number
  subjectCount?: number
  lectureHours?: number
  practicalHours?: number
  labHours?: number
  seminarHours?: number
  ratingHours?: number
  independentHours?: number
  totalHours?: number
  groupCount?: number
  studentCount?: number
  loadLabel?: string
}
