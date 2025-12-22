#!/usr/bin/env bash
set -euo pipefail

cmd=""
if [ "${1:-}" = "--cmd" ]; then
  shift
  cmd="${1:-}"
  if [ -z "$cmd" ]; then
    echo "[ensure-node] Error: falta el valor de --cmd" >&2
    exit 2
  fi
fi

echo "[ensure-node] Comprovant Node.js..."

if command -v node >/dev/null 2>&1; then
  ver=$(node -v)
  major=${ver#v}
  major=${major%%.*}
  echo "[ensure-node] Node detectat: $ver"
  if [ "$major" -ge 22 ]; then
    echo "[ensure-node] Versió suficient (>=22)."
    if [ -n "$cmd" ]; then
      echo "[ensure-node] Executant --cmd: $cmd"
      eval "$cmd"
    fi
    exit 0
  else
    echo "[ensure-node] Versió massa antiga (<22): $ver"
  fi
else
  echo "[ensure-node] Node no detectat."
fi

echo "[ensure-node] Comprovant nvm..."

# nvm may be a shell function; try detect common locations
if command -v nvm >/dev/null 2>&1; then
  echo "[ensure-node] nvm ja disponible com a comanda.";
elif [ -s "$HOME/.nvm/nvm.sh" ]; then
  export NVM_DIR="$HOME/.nvm"
  # shellcheck source=/dev/null
  . "$NVM_DIR/nvm.sh"
  echo "[ensure-node] s'ha carregat nvm des de $NVM_DIR";
else
  echo "[ensure-node] nvm no trobat. Intentant instal·lar nvm (nvm-sh)..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
  elif command -v wget >/dev/null 2>&1; then
    wget -qO- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
  else
    echo "[ensure-node] Error: ni curl ni wget disponibles per descarregar nvm. Instal·la'ls primer." >&2
    exit 2
  fi
  export NVM_DIR="$HOME/.nvm"
  # shellcheck source=/dev/null
  . "$NVM_DIR/nvm.sh"
  echo "[ensure-node] nvm instal·lat i carregat.";
fi

echo "[ensure-node] Instal·lant Node 23 amb nvm (si cal)..."
if nvm ls 23 >/dev/null 2>&1; then
  echo "[ensure-node] Node 23 ja instal·lat localment.";
else
  nvm install 23
fi
nvm alias default 23 >/dev/null 2>&1 || true
nvm use 23 >/dev/null 2>&1 || true

echo "[ensure-node] Versió activa de node: $(node -v)"
echo "[ensure-node] Fet."

if [ -n "$cmd" ]; then
  echo "[ensure-node] Executant --cmd: $cmd"
  eval "$cmd"
fi

exit 0
