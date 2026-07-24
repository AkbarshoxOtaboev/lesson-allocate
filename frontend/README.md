# URSPI Admin Frontend

TailAdmin (Vue 3 + Tailwind) asosida URSPI ichki boshqaruv tizimi frontend qismi.

## Ishga tushirish

```bash
cd frontend
npm install
npm run dev
```

Ilova: http://localhost:5173  
API (default): http://localhost:8080/api

`.env` faylida `VITE_API_BASE_URL` ni o‘zgartirish mumkin.

## Asosiy yo‘nalishlar

| Sahifa | Endpoint |
|--------|----------|
| Kirish | `POST /api/auth/login` |
| Foydalanuvchilar | `/api/users` |
| Rollar | `/api/roles` |
| Audit | `/api/audit/logs` |
| Fakultet / kafedra / guruh | `/api/faculties`, `/departments`, `/groups` |

Dev login (backend seed): `admin` / `admin`
