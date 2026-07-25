import api from './http'
import type { NamedEntity } from '@/types/api'

type ListParams = Record<string, string | number | undefined>

function crud(base: string) {
  return {
    list: (params?: ListParams) => api.get<NamedEntity[]>(base, { params }),
    getById: (id: number) => api.get<NamedEntity>(`${base}/${id}`),
    create: (payload: Record<string, unknown>) => api.post<NamedEntity>(`${base}/create`, payload),
    update: (id: number, payload: Record<string, unknown>) =>
      api.put<NamedEntity>(`${base}/update/${id}`, payload),
    remove: (id: number) => api.delete(`${base}/delete/${id}`),
  }
}

export const facultyApi = crud('/faculties')
export const departmentApi = crud('/departments')
export const teacherApi = crud('/teachers')
export const subjectApi = crud('/subjects')
