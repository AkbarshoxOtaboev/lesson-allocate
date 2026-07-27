import api from './http'
import type { NamedEntity, Subject } from '@/types/api'

type ListParams = Record<string, string | number | undefined>

function crud<TListItem = NamedEntity>(base: string) {
  return {
    list: (params?: ListParams) => api.get<TListItem[]>(base, { params }),
    getById: (id: number) => api.get<TListItem>(`${base}/${id}`),
    create: (payload: Record<string, unknown>) => api.post<TListItem>(`${base}/create`, payload),
    update: (id: number, payload: Record<string, unknown>) =>
      api.put<TListItem>(`${base}/update/${id}`, payload),
    remove: (id: number) => api.delete(`${base}/delete/${id}`),
  }
}

export const facultyApi = crud('/faculties')
export const departmentApi = crud('/departments')
export const teacherApi = crud('/teachers')
export const subjectApi = crud<Subject>('/subjects')
export const academicYearApi = crud('/academic-years')
