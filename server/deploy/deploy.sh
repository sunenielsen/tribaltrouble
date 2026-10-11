#!/usr/bin/env bash
#
# Runs ON the target box (test or prod). Idempotent: provisions the directory
# layout and systemd units on first run, then on every run atomically swaps the
# 'current' release, restarts the services, health-checks them, and rolls back
# if the new version does not come up.
#
# Usage:  deploy.sh <release-tag>
# Expects ~/incoming/ to contain (uploaded by CI):
#   dist/                  the unpacked server build
#   matchmaker.service     systemd unit (with __SERVICE_USER__ placeholder)
#   router.service         systemd unit
#
set -euo pipefail

TAG="${1:?usage: deploy.sh <release-tag>}"

APP_ROOT=/opt/tribaltrouble
RELEASES="$APP_ROOT/releases"
SHARED="$APP_ROOT/shared"
CURRENT="$APP_ROOT/current"
INCOMING="$HOME/incoming"
SERVICE_USER="$(whoami)"

# Ports the two servers listen on (see RouterInterface / MatchmakingServerInterface).
MATCHMAKER_PORT=33214
ROUTER_PORT=11221

echo ">> Deploying $TAG as user $SERVICE_USER"

# --- 1. Provision layout (idempotent) ---------------------------------------
sudo mkdir -p "$RELEASES" "$SHARED" "$SHARED/logs" /var/games
sudo chown -R "$SERVICE_USER" "$APP_ROOT" /var/games

# --- 2. Seed shared config on first run; never overwrite real secrets --------
if [ ! -f "$SHARED/server.properties" ]; then
  cp "$INCOMING/dist/server.properties" "$SHARED/server.properties"
  echo "!! Created $SHARED/server.properties from the template."
  echo "!! Fill in the real DB password / Discord / Steam values, then re-run the deploy."
fi

# --- 3. Stage the new release ------------------------------------------------
DEST="$RELEASES/$TAG"
rm -rf "$DEST"
mkdir -p "$DEST"
cp -a "$INCOMING/dist/." "$DEST/"
# Point config + logs at the persistent shared copies so deploys never clobber them.
rm -f "$DEST/server.properties"
ln -sfn "$SHARED/server.properties" "$DEST/server.properties"
rm -rf "$DEST/logs"
ln -sfn "$SHARED/logs" "$DEST/logs"
chmod +x "$DEST"/bin/* 2>/dev/null || true

# --- 4. Install / refresh systemd units (idempotent) -------------------------
for unit in matchmaker router; do
  sed "s/__SERVICE_USER__/$SERVICE_USER/g" "$INCOMING/$unit.service" \
    | sudo tee "/etc/systemd/system/$unit.service" >/dev/null
done
sudo systemctl daemon-reload
sudo systemctl enable matchmaker router >/dev/null 2>&1 || true

# --- 5. Atomic swap (remember the previous target for rollback) --------------
PREV="$(readlink -f "$CURRENT" 2>/dev/null || true)"
ln -sfn "$DEST" "$CURRENT"

restart_services() { sudo systemctl restart matchmaker router; }

in_unit() { grep -Eq '/(matchmaker|router)\.service$' "/proc/$1/cgroup" 2>/dev/null; }

# Servers or restart-loop launch scripts running outside our units (a hand-started
# pre-systemd install) hold or retake the ports; launchers go first so nothing respawns.
stop_strays() {
  local pattern pid stopped=""
  for pattern in '^(/usr)?/bin/(ba)?sh ([^ ]*/)?(router|matchmaker)$' \
                 'com\.oddlabs\.(routerserver\.RouterServer|matchserver\.MatchmakingServer)'; do
    for pid in $(pgrep -f -- "$pattern"); do
      in_unit "$pid" && continue
      echo "!! Stopping stray $pid: $(ps -o args= -p "$pid" | cut -c1-100)"
      sudo kill "$pid" 2>/dev/null || true
      stopped="$stopped $pid"
    done
  done
  for pid in $stopped; do
    timeout 10 tail --pid="$pid" -f /dev/null || sudo kill -9 "$pid" 2>/dev/null || true
  done
}

# An open port is not enough: it must be our unit listening on it.
serves() {
  local pid
  pid="$(sudo ss -ltnpH "sport = :$1" | grep -o 'pid=[0-9]*' | head -1 | cut -d= -f2)"
  [ -n "$pid" ] && in_unit "$pid"
}

is_healthy() {
  systemctl is-active --quiet matchmaker || return 1
  systemctl is-active --quiet router     || return 1
  serves "$MATCHMAKER_PORT" || return 1
  serves "$ROUTER_PORT"
}

stop_strays

echo ">> Restarting services on $TAG"
restart_services

# Give the JVMs time to bind their ports; poll up to ~30s.
healthy=0
for _ in $(seq 1 15); do
  if is_healthy; then healthy=1; break; fi
  sleep 2
done

# --- 6. Roll back if the new version is not healthy --------------------------
if [ "$healthy" -ne 1 ]; then
  echo "!! Health check FAILED for $TAG"
  sudo ss -ltnp "( sport = :$MATCHMAKER_PORT or sport = :$ROUTER_PORT )" || true
  if [ -n "$PREV" ] && [ -d "$PREV" ]; then
    echo "!! Rolling back to $(basename "$PREV")"
    ln -sfn "$PREV" "$CURRENT"
    restart_services
  else
    echo "!! No previous release to roll back to (first deploy?)."
    echo "!! If this box is new, confirm $SHARED/server.properties has real secrets."
  fi
  exit 1
fi

echo ">> $TAG is live and healthy."

# --- 7. Drop every release except the one now live (redeploy a tag to roll back) ---
live="$(readlink -f "$CURRENT")"
for old in "$RELEASES"/*/; do
  [ -d "$old" ] || continue
  [ "$(readlink -f "$old")" = "$live" ] && continue
  rm -rf "$old"
done

echo ">> Done."
