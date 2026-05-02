# Deployment Guide (EC2 + GitHub Pages + Neon)

This guide deploys:
- Backend (`Spring Boot`) to `AWS EC2` with Docker
- Frontend (`Vite/React`) to `GitHub Pages`
- Database on `Neon` (already created by you)

## 1) What was added

- `.github/workflows/deploy-backend-ec2.yml`
  - Deploys backend to EC2 over SSH
  - Uses `docker-compose.prod.yml` for production behavior
- `docker-compose.prod.yml`
  - Disables local Postgres dependency for backend in production
  - Forces backend to use env vars (Neon credentials from EC2 `.env`)
- `.env.example`
  - Updated to production-friendly variables (`VITE_API_BASE_URL`, Neon vars)
- `frontend/.env.production.example`
  - Example frontend API base URL

## 2) One-time AWS EC2 setup

### 2.1 Launch EC2

- Use Ubuntu 22.04 LTS
- Instance type: `t3.small` or better
- Security Group inbound:
  - `22` (SSH) from your IP (or secure range)
  - `8080` from `0.0.0.0/0` (or only from your frontend/proxy if locked down)
  - Optional: `80/443` if you later add Nginx + TLS

### 2.2 Install Docker on EC2

SSH into your instance and run:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin git
sudo usermod -aG docker ubuntu
newgrp docker
```

### 2.3 Clone project and create backend env file

```bash
cd /home/ubuntu
git clone https://github.com/<your-username>/<your-repo>.git ConsultHub
cd ConsultHub
cp .env.example .env
```

Edit `/home/ubuntu/ConsultHub/.env` and set at least:

```env
SPRING_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://<your-neon-host>/<your-db>?sslmode=require&channel_binding=require
SPRING_DATASOURCE_USERNAME=<your-neon-user>
SPRING_DATASOURCE_PASSWORD=<your-neon-password>
SPRING_JPA_HIBERNATE_DDL_AUTO=update
DB_SSLMODE=require
DB_CHANNEL_BINDING=require
JWT_SECRET=<very-long-random-secret>
JWT_EXP_MS=3600000
GEMINI_API_KEY=<optional-if-using-chat>
```

Test manually once:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build backend
curl http://127.0.0.1:8080/actuator/health
```

## 3) GitHub secrets for backend workflow

In repo settings -> `Secrets and variables` -> `Actions`, create:

- `EC2_HOST` = public IPv4 or DNS of EC2
- `EC2_USER` = `ubuntu`
- `EC2_SSH_KEY` = private SSH key content (`.pem`) used for EC2 login

Then run workflow:
- Actions -> `Deploy Backend to EC2` -> `Run workflow`

Any push to `main` that touches backend files also auto-deploys.

## 4) Backend domain (recommended)

If you have a domain:
- Create `A` record: `api.your-domain.com` -> EC2 public IP
- Use this URL as frontend API base URL

Without domain, use:
- `http://<EC2_PUBLIC_IP>:8080`

## 5) GitHub Pages setup for frontend

### 5.1 GitHub repo settings

- Settings -> Pages
- Source should be `GitHub Actions` (not branch deploy)

### 5.2 Frontend action secret

Create secret:
- `VITE_API_BASE_URL` = `http://<EC2_PUBLIC_IP>:8080`
  - or `https://api.your-domain.com` if using domain/TLS

### 5.3 Custom domain (optional but already supported in repo)

`frontend/public/CNAME` is currently set. Keep it only if that domain is yours.

- If using custom domain:
  - Leave/update `frontend/public/CNAME`
  - Add DNS `CNAME` record from your domain/subdomain to `<username>.github.io`
- If not using custom domain:
  - Remove `frontend/public/CNAME`

### 5.4 Trigger frontend deploy

- Push any frontend change to `main`, or
- Run `Deploy Frontend (GitHub Pages)` manually from Actions tab

## 6) DNS + CORS checklist

- Your backend currently allows all origins (`CorsConfig` uses `allowedOriginPatterns("*")`).
- This works for deployment, but for production hardening you should later restrict to your exact frontend domain.

## 7) Smoke test after deploy

1. Open frontend URL (GitHub Pages URL or custom domain)
2. Verify login/register requests hit your backend domain/IP
3. Verify backend health:
   - `http://<backend-host>:8080/actuator/health`
4. Verify DB writes/read succeed (create a user/booking)

## 8) Common failure fixes

- `Workflow SSH failed`:
  - Wrong `EC2_HOST`, `EC2_USER`, or invalid `EC2_SSH_KEY`
- `docker compose: command not found`:
  - Docker compose plugin not installed on EC2
- `Backend starts but DB connection fails`:
  - Neon URL/user/password incorrect
  - Missing `sslmode=require&channel_binding=require`
- `Frontend builds but API calls fail`:
  - `VITE_API_BASE_URL` secret missing/wrong
  - Backend port `8080` blocked in security group
