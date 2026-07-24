import api from './http'
import type { AuditLog } from '@/types/api'

export const auditApi = {
  list() {
    return api.get<AuditLog[]>('/audit/logs')
  },
}
