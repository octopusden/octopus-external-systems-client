#!/usr/bin/env bash
# Advances TeamCity 2026 past the FIRST_START_SCREEN maintenance wizard by
# replaying the welcome wizard via REST. Required because the maintenance
# state blocks /app/rest/* (returns 503) until first-start is acknowledged,
# even when the data directory is pre-seeded.
#
# Idempotent: if TC is already past maintenance, the 4 POSTs no-op and the
# script exits as soon as /login.html responds.

set -euo pipefail

HOST="${1:?usage: $0 <host:port>}"
COOKIES="$(mktemp)"
trap 'rm -f "$COOKIES"' EXIT

echo "[confirm-tc] target: http://$HOST"

# Fetch maintenance page and extract CSRF token
body=""
for _ in $(seq 1 60); do
  body="$(curl -sf -c "$COOKIES" "http://$HOST/mnt" 2>/dev/null || true)"
  if [ -n "$body" ]; then break; fi
  # already past maintenance?
  code=$(curl -s -o /dev/null -w "%{http_code}" "http://$HOST/login.html")
  if [ "$code" = "200" ]; then
    echo "[confirm-tc] /login.html already 200, nothing to do"
    exit 0
  fi
  sleep 5
done

csrf="$(echo "$body" | sed -nE 's/.*tc-csrf-token" content="([^"]+)".*/\1/p' | head -1)"
if [ -z "$csrf" ]; then
  echo "[confirm-tc] no CSRF token; TC may already be past maintenance"
  exit 0
fi
echo "[confirm-tc] CSRF: $csrf"

post() {
  local ep="$1" data="$2" code
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "X-TC-CSRF-Token: $csrf" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -b "$COOKIES" -c "$COOKIES" \
    --data "$data" \
    "http://$HOST/mnt/do/$ep")
  echo "[confirm-tc] POST /mnt/do/$ep -> $code"
}

post "goNewInstallation" "restore=false"
sleep 2
post "saveUserInputOnDBsettingsPage" "dbType=HSQLDB2&connHost=&connInst=&connDB=&connIntegratedSecurity=-&connUser=&connPwd="
sleep 2
post "goNewDatabase" "dbType=HSQLDB2"
sleep 2
post "acceptLicenseAgreementAndSendUsageStatistics" ""

echo "[confirm-tc] waiting for /login.html to be reachable..."
for i in $(seq 1 90); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "http://$HOST/login.html")
  if [ "$code" = "200" ]; then
    echo "[confirm-tc] /login.html is 200, TC ready"
    exit 0
  fi
  if [ "$code" = "302" ]; then
    final=$(curl -sL -o /dev/null -w "%{url_effective}" "http://$HOST/login.html")
    if echo "$final" | grep -q "setupAdmin.html"; then
      echo "[confirm-tc] WARNING: TC redirects to setupAdmin.html — admin user missing in seed"
      exit 2
    fi
    echo "[confirm-tc] /login.html redirects to $final, treating as ready"
    exit 0
  fi
  echo "  attempt $i: $code"
  sleep 5
done

echo "[confirm-tc] TIMEOUT: /login.html never reached 200"
exit 1
