import api from './http'
import type { User } from '@/types/api'

export interface UserFormPayload {
  username: string
  password?: string
  fullName?: string
  phone?: string
  roleIds?: number[]
  profileImage?: File | null
}

function toFormData(payload: UserFormPayload): FormData {
  const form = new FormData()
  form.append('username', payload.username)
  if (payload.password) form.append('password', payload.password)
  if (payload.fullName != null) form.append('fullName', payload.fullName)
  if (payload.phone != null) form.append('phone', payload.phone)
  if (payload.roleIds?.length) {
    payload.roleIds.forEach((id) => form.append('roleIds', String(id)))
  }
  if (payload.profileImage) form.append('profileImage', payload.profileImage)
  return form
}

export const usersApi = {
  list() {
    return api.get<User[]>('/users')
  },
  getById(id: number) {
    return api.get<User>(`/users/${id}`)
  },
  create(payload: UserFormPayload) {
    return api.post<User>('/users/create', toFormData(payload), {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  update(id: number, payload: UserFormPayload) {
    return api.put<User>(`/users/update/${id}`, toFormData(payload), {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  remove(id: number) {
    return api.delete(`/users/delete/${id}`)
  },
  changeStatus(id: number) {
    return api.put<User>(`/users/change/status/${id}`)
  },
}
