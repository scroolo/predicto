#!/usr/bin/env bash
set -euo pipefail

DOMAIN="${1:-predicto.example.com}"
echo "Setting up Predicto VPS for domain: $DOMAIN"

# --- Prerequisites ---
apt-get update
apt-get install -y docker.io docker-compose-v2 nginx certbot python3-certbot-nginx git

systemctl enable --now docker

# --- Project checkout ---
mkdir -p /opt/predicto
cd /opt/predicto

if [ ! -d .git ]; then
  git clone https://github.com/YOUR_ORG/predicto.git .
fi

# --- .env file (create if missing) ---
if [ ! -f .env ]; then
  cat > .env <<-EOF
JWT_SECRET=$(openssl rand -hex 32)
POSTGRES_PASSWORD=$(openssl rand -hex 16)
ADMIN_BOOTSTRAP_PASSWORD=change-me
CITO_API_KEY=
EOF
  echo ".env created — edit ADMIN_BOOTSTRAP_PASSWORD and CITO_API_KEY as needed"
fi

# --- Let's Encrypt (interactive first time) ---
if [ ! -d /etc/letsencrypt/live/$DOMAIN ]; then
  certbot --nginx -d $DOMAIN --non-interactive --agree-tos -m admin@$DOMAIN
fi

# --- Update nginx config with real domain ---
sed -i "s/predicto\.example\.com/$DOMAIN/g" deploy/nginx.conf

# --- Start production stack ---
docker compose -f deploy/docker-compose.yml up -d

echo "Deploy complete. Check logs: docker compose -f deploy/docker-compose.yml logs -f"
