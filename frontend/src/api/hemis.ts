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

export interface HemisSyncResult {
  fetched: number
  created: number
  updated: number
  skipped: number
}

function toParams(query: HemisDepartmentQuery) {
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
      params: toParams(query),
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
}
