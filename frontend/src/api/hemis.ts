import api from './http'

export interface HemisTokenInfo {
  id?: number
  provider: string
  maskedToken?: string | null
  configured: boolean
  baseUrl?: string
  description?: string | null
  updatedAt?: string | null
}

export interface HemisDepartment {
  id: number
  name: string
  code?: string
  parent?: number | null
  active?: boolean
  structureType?: { code?: string; name?: string }
}

export interface HemisDepartmentQuery {
  page?: number
  limit?: number
  active?: string
  structureType?: string
  parent?: number | null
}

export interface HemisDepartmentListResponse {
  items: HemisDepartment[]
  page?: number
  pageCount?: number
  totalCount?: number
  pageSize?: number
}

export interface HemisEmployee {
  id: number
  fullName?: string
  shortName?: string
  firstName?: string
  secondName?: string
  thirdName?: string
  employeeIdNumber?: string
  birthDate?: string
  gender?: { code?: string; name?: string }
  department?: { id?: number; name?: string; code?: string }
  staffPosition?: { code?: string; name?: string }
  employeeStatus?: { code?: string; name?: string }
  employeeType?: { code?: string; name?: string }
  academicRank?: { code?: string; name?: string }
  academicDegree?: { code?: string; name?: string }
}

export interface HemisEmployeeQuery {
  page?: number
  limit?: number
  type?: string
  department?: number | null
  gender?: string
  staffPosition?: string
  employeeStatus?: string
  employmentForm?: string
  employmentStaff?: string
  employeeType?: string
  academicRank?: string
  academicDegree?: string
  search?: string
}

export interface HemisEmployeeListResponse {
  items: HemisEmployee[]
  page?: number
  pageCount?: number
  totalCount?: number
  pageSize?: number
}

export interface HemisSyncResult {
  fetched: number
  created: number
  updated: number
  skipped: number
}

function toDeptParams(query: HemisDepartmentQuery) {
  const params: Record<string, string | number> = {}
  if (query.page != null) params.page = query.page
  if (query.limit != null) params.limit = query.limit
  if (query.active != null && query.active !== '') params.active = query.active
  if (query.structureType != null && query.structureType !== '') {
    params._structure_type = query.structureType
  }
  if (query.parent != null && query.parent !== undefined) params.parent = query.parent
  return params
}

function toEmployeeParams(query: HemisEmployeeQuery) {
  const params: Record<string, string | number> = {}
  if (query.page != null) params.page = query.page
  if (query.limit != null) params.limit = query.limit
  if (query.type) params.type = query.type
  if (query.department != null) params._department = query.department
  if (query.gender) params._gender = query.gender
  if (query.staffPosition) params._staff_position = query.staffPosition
  if (query.employeeStatus) params._employee_status = query.employeeStatus
  if (query.employmentForm) params._employment_form = query.employmentForm
  if (query.employmentStaff) params._employment_staff = query.employmentStaff
  if (query.employeeType) params._employee_type = query.employeeType
  if (query.academicRank) params._academic_rank = query.academicRank
  if (query.academicDegree) params._academic_degree = query.academicDegree
  if (query.search) params.search = query.search
  return params
}

export const hemisApi = {
  getToken() {
    return api.get<HemisTokenInfo>('/hemis/token')
  },
  saveToken(payload: { accessToken: string; baseUrl?: string; description?: string }) {
    return api.put<HemisTokenInfo>('/hemis/token', payload)
  },
  deleteToken() {
    return api.delete('/hemis/token')
  },
  fetchDepartments(query: HemisDepartmentQuery = {}) {
    return api.get<HemisDepartmentListResponse>('/hemis/departments', {
      params: toDeptParams(query),
    })
  },
  syncFaculties(query: HemisDepartmentQuery = {}) {
    return api.post<HemisSyncResult>('/hemis/sync/faculties', {
      page: query.page ?? 1,
      limit: query.limit ?? 50,
      active: query.active ?? '1',
      structureType: query.structureType || null,
      parent: query.parent ?? null,
      fetchAllPages: true,
    })
  },
  syncDepartments(query: HemisDepartmentQuery = {}) {
    return api.post<HemisSyncResult>('/hemis/sync/departments', {
      page: query.page ?? 1,
      limit: query.limit ?? 50,
      active: query.active ?? '1',
      structureType: query.structureType || null,
      parent: query.parent ?? null,
      fetchAllPages: true,
    })
  },
  fetchEmployees(query: HemisEmployeeQuery = {}) {
    return api.get<HemisEmployeeListResponse>('/hemis/employees', {
      params: toEmployeeParams(query),
    })
  },
  syncTeachers(query: HemisEmployeeQuery = {}) {
    return api.post<HemisSyncResult>('/hemis/sync/teachers', {
      page: query.page ?? 1,
      limit: query.limit ?? 50,
      type: query.type ?? 'teacher',
      department: query.department ?? null,
      gender: query.gender || null,
      staffPosition: query.staffPosition || null,
      employeeStatus: query.employeeStatus || null,
      employmentForm: query.employmentForm || null,
      employmentStaff: query.employmentStaff || null,
      employeeType: query.employeeType || null,
      academicRank: query.academicRank || null,
      academicDegree: query.academicDegree || null,
      search: query.search || null,
      fetchAllPages: true,
    })
  },
}
