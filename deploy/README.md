# deploy/README.md — dl.urspi.uz ga joylash

## 1. JAR yig‘ish (dev mashinada)

```powershell
# Windows
.\scripts\build-jar.ps1
```

```bash
# Linux / macOS
chmod +x scripts/build-jar.sh
./scripts/build-jar.sh
```

Natija: `backend/target/lesson-allocate-1.0.0.jar` (frontend ichida).

## 2. Serverga ko‘chirish

```bash
sudo mkdir -p /opt/urspi/lesson-allocate /var/urspi/uploads
sudo useradd -r -s /usr/sbin/nologin urspi || true
sudo chown -R urspi:urspi /opt/urspi /var/urspi

scp backend/target/lesson-allocate-1.0.0.jar user@server:/opt/urspi/lesson-allocate/
scp deploy/lesson-allocate.service user@server:/tmp/
scp deploy/nginx-dl.urspi.uz.conf user@server:/tmp/
```

## 3. PostgreSQL

```bash
sudo -u postgres createdb urspi_db
# yoki mavjud DB — Environment dagi DB_URL ni moslang
```

## 4. systemd

`/etc/systemd/system/lesson-allocate.service` ichida `DB_PASSWORD` va `JWT_SECRET` ni almashtiring.

```bash
sudo cp /tmp/lesson-allocate.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now lesson-allocate
sudo systemctl status lesson-allocate
```

## 5. Nginx + SSL

```bash
sudo cp /tmp/nginx-dl.urspi.uz.conf /etc/nginx/sites-available/dl.urspi.uz
sudo ln -sf /etc/nginx/sites-available/dl.urspi.uz /etc/nginx/sites-enabled/
# DNS: dl.urspi.uz → server IP
sudo certbot --nginx -d dl.urspi.uz
sudo nginx -t && sudo systemctl reload nginx
```

Brauzer: **https://dl.urspi.uz**
API: **https://dl.urspi.uz/api/...**
