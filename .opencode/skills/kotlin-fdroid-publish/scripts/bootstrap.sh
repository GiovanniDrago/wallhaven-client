#!/usr/bin/env bash
set -euo pipefail

data_home="${XDG_DATA_HOME:-$HOME/.local/share}/opencode/kotlin-fdroid-publish"
venv="${FDROID_VENV:-$data_home/venv}"
check_only=0

usage() {
  cat <<'EOF'
Usage: scripts/bootstrap.sh [--check-only] [--venv PATH]

Installs fdroidserver in a private virtual environment and prints the paths to use.
It does not install glab, Docker, Java, or the Android SDK automatically.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --check-only)
      check_only=1
      shift
      ;;
    --venv)
      [[ $# -ge 2 ]] || { echo "Missing value for --venv" >&2; exit 1; }
      venv="$2"
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

need() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "error: required command not found: $1" >&2
    return 1
  fi
}

need python3
python3 - <<'PY'
import sys
if sys.version_info < (3, 9):
    raise SystemExit("error: Python 3.9+ is required")
print(f"python: {sys.version.split()[0]}")
PY

need git
printf 'git: %s\n' "$(git --version)"
if command -v keytool >/dev/null 2>&1; then
  printf 'keytool: %s\n' "$(keytool -help 2>&1 | head -1 || true)"
else
  echo "warning: keytool not found; signing fingerprints cannot be checked" >&2
fi

if command -v glab >/dev/null 2>&1; then
  printf 'glab: %s\n' "$(glab --version 2>/dev/null | head -1 || true)"
  if glab auth status >/dev/null 2>&1; then
    echo "glab: authenticated"
    glab auth status 2>&1 | sed 's/^/  /'
  else
    echo "warning: glab is installed but not authenticated" >&2
  fi
else
  echo "warning: glab not found; automatic draft MR creation will be unavailable" >&2
  echo "         install it from https://gitlab.com/gitlab-org/cli" >&2
fi

if command -v docker >/dev/null 2>&1; then
  printf 'docker: %s\n' "$(docker --version 2>/dev/null || true)"
else
  echo "warning: docker not found; the local buildserver test will be unavailable" >&2
fi

if [[ $check_only -eq 1 ]]; then
  exit 0
fi

if [[ ! -x "$venv/bin/python" ]]; then
  mkdir -p "$(dirname "$venv")"
  python3 -m venv "$venv"
fi

if command -v uv >/dev/null 2>&1; then
  uv pip install --python "$venv/bin/python" --upgrade fdroidserver
else
  "$venv/bin/python" -m pip install --upgrade pip
  "$venv/bin/python" -m pip install --upgrade fdroidserver
fi

"$venv/bin/python" - <<'PY'
import yaml
print(f"PyYAML: {yaml.__version__}")
PY

"$venv/bin/fdroid" --version 2>/dev/null || true

cat <<EOF

FDROID_VENV=$venv
FDROID_BIN=$venv/bin/fdroid
PYTHON_BIN=$venv/bin/python
EOF
