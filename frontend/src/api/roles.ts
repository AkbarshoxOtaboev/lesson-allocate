import api from './http'
import type { Permission, Role } from '@/types/api'

export const rolesApi = {
  list() {
    return api.get<Role[]>('/roles')
  },
  getById(id: number) {
    return api.get<Role>(`/roles/${id}`)
  },
  create(name: string) {
    return api.post<Role>('/roles/create', { name })
  },
  update(id: number, name: string) {
    return api.put<Role>(`/roles/update/${id}`, { name })
  },
  remove(id: number) {
    return api.delete(`/roles/delete/${id}`)
  },
  getPermissions(roleId: number) {
    return api.get<Permission[]>(`/roles/${roleId}/permissions`)
  },
  getAllPermissions() {
    return api.get<Permission[]>('/roles/all/permissions')
  },
  assignPermission(roleId: number, permissionId: number) {
    return api.post(`/roles/${roleId}/permissions/${permissionId}`)
  },
  removePermission(roleId: number, permissionId: number) {
    return api.delete(`/roles/${roleId}/permissions/${permissionId}`)
  },
}
