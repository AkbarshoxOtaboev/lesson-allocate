import api from './http'
import type { Talabnoma, TalabnomaStats, TalabnomaStatus } from '@/types/talabnoma'

export interface TalabnomaCreatePayload {
  toDepartmentId: number
  subjectName: string
  subjectCode?: string
  academicYearId?: number
  semester?: 'AUTUMN' | 'SPRING'
  lectureHours?: number
  practicalHours?: number
  labHours?: number
  seminarHours?: number
  ratingHours?: number
  note?: string
}

export const talabnomaApi = {
  list(params?: {
    facultyId?: number
    departmentId?: number
    status?: TalabnomaStatus
  }) {
    return api.get<Talabnoma[]>('/talabnomalar', { params })
  },
  stats(params?: { facultyId?: number; departmentId?: number }) {
    return api.get<TalabnomaStats>('/talabnomalar/stats', { params })
  },
  newCount() {
    return api.get<{ count: number }>('/talabnomalar/new-count')
  },
  getById(id: number) {
    return api.get<Talabnoma>(`/talabnomalar/${id}`)
  },
  create(payload: TalabnomaCreatePayload) {
    return api.post<Talabnoma>('/talabnomalar/create', payload)
  },
  accept(id: number) {
    return api.post<Talabnoma>(`/talabnomalar/${id}/accept`)
  },
  reject(id: number, reason?: string) {
    return api.post<Talabnoma>(`/talabnomalar/${id}/reject`, { reason })
  },
  remove(id: number) {
    return api.delete(`/talabnomalar/delete/${id}`)
  },
}
