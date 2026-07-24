# URSPI Lesson Allocate

Ichki boshqaruv axborot tizimi — backend (Spring Boot 4.1) + frontend (Vue 3 / TailAdmin).

## Tezkor start

### Backend
```bash
cd backend
# Maven o'rnatilgan bo'lsa:
mvn spring-boot:run

# yoki lokal Maven:
..\ .tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```
API: http://localhost:8080  
Swagger: http://localhost:8080/swagger-ui.html  
Default login: `admin` / `admin`  
DB: H2 file (`./data/urspi`), console: `/h2-console`

### Frontend
```bash
cd frontend
npm install
npm run dev
```
UI: http://localhost:5173

## Profil
- Default: H2 + in-memory token blacklist
- Postgres: `mvn spring-boot:run -Dspring-boot.run.profiles=postgres`
