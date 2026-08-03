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
  directionId?: number
  directionCode?: string
  directionName?: string
  courseYear?: number
  semester?: 'AUTUMN' | 'SPRING'
  educationType?: 'KUNDUZGI' | 'KECHKI' | 'MASOFAVIY' | 'SIRTQI'
  educationLanguage?: 'UZB' | 'RUS'
  totalSubjectHours?: number
  lectureHours?: number
  practicalHours?: number
  labHours?: number
  seminarHours?: number
  independentStudyHours?: number
  ratingHours?: number
  totalHours?: number
  groupCount?: number
  studentCount?: number
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

export interface TeacherWorkloadAllocation {
  allocationId?: number
  subjectId?: number
  subjectCode?: string
  subjectName?: string
  departmentName?: string
  semester?: 'AUTUMN' | 'SPRING'
  courseYear?: number
  lectureHours?: number
  practicalHours?: number
  labHours?: number
  seminarHours?: number
  ratingHours?: number
  totalHours?: number
  independentHours?: number
  totalSubjectHours?: number
  credit?: number
  groupCount?: number
  studentCount?: number
}
