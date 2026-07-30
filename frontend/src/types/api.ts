export type EntityStatus = 'ACTIVE' | 'DISABLED' | 'DELETED'

export interface RoleSummary {
  id: number
  name: string
}

export interface Permission {
  id: number
  name: string
  labelUz?: string
  resource?: string
  action?: string
}

export interface User {
  id: number
  username: string
  fullName?: string
  phone?: string
  profileImage?: string | null
  bio?: string | null
  country?: string | null
  city?: string | null
  region?: string | null
  postalCode?: string | null
  taxId?: string | null
  status: EntityStatus
  roles?: RoleSummary[]
  facultyId?: number | null
  facultyName?: string | null
  departmentId?: number | null
  departmentName?: string | null
  lastLogin?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface Role {
  id: number
  name: string
  status?: EntityStatus
  permissions?: Permission[]
  createdAt?: string
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  tokenType?: string
}

export interface LoginResponse extends AuthTokens {
  user?: User
  username?: string
  fullName?: string
}

export interface HeaderNotificationItem {
  id: number | string
  title: string
  description: string
  meta?: string
  time?: string
  to?: string
}

export interface AuditLog {
  id: number
  username?: string
  entity?: string
  action?: string
  httpMethod?: string
  url?: string
  ipAddress?: string
  userAgent?: string
  createdAt?: string
  timestamp?: string
}

export interface NamedEntity {
  id: number
  name: string
  status?: EntityStatus
  facultyId?: number
  departmentId?: number
  description?: string
}

export interface Direction extends NamedEntity {
  directionCode: string
  directionName: string
}

export interface Subject {
  id: number
  code: string
  name: string
  status?: EntityStatus
  departmentId?: number
  departmentName?: string
  facultyId?: number
  facultyName?: string
  /** Talabnoma orqali kelgan fan — yuborgan fakultet */
  sourceFacultyId?: number | null
  sourceFacultyName?: string | null
  talabnomaCode?: string | null
  academicYearId?: number | null
  academicYearName?: string | null
  directionId?: number | null
  directionCode?: string | null
  directionName?: string | null
  courseYear?: number | null
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
  overallHours?: number
  credit?: number
  groupCount?: number
  studentCount?: number
}
