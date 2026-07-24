export type EntityStatus = 'ACTIVE' | 'DISABLED' | 'DELETED'

export interface RoleSummary {
  id: number
  name: string
}

export interface Permission {
  id: number
  name: string
  resource?: string
  action?: string
}

export interface User {
  id: number
  username: string
  fullName?: string
  phone?: string
  profileImage?: string | null
  status: EntityStatus
  roles?: RoleSummary[]
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
