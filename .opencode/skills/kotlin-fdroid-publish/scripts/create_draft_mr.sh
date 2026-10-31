#!/usr/bin/env bash
set -euo pipefail

repo="fdroid/fdroiddata"
head=""
source_branch=""
target_branch="master"
title=""
body_file=""
label=""
execute=0

usage() {
  cat <<'EOF'
Usage: scripts/create_draft_mr.sh \
  --head NAMESPACE/fdroiddata \
  --source BRANCH \
  --title "New app: Name" \
  --body-file handoff/mr.md \
  [--repo fdroid/fdroiddata] [--target master] [--label "New App"] [--execute]

Without --execute the script prints the command and exits. It only creates a draft MR.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)       repo="$2"; shift 2 ;;
    --head)       head="$2"; shift 2 ;;
    --source)     source_branch="$2"; shift 2 ;;
    --target)     target_branch="$2"; shift 2 ;;
    --title)      title="$2"; shift 2 ;;
    --body-file)  body_file="$2"; shift 2 ;;
    --label)      label="$2"; shift 2 ;;
    --execute)    execute=1; shift ;;
    --help|-h)    usage; exit 0 ;;
    *)            echo "Unknown argument: $1" >&2; usage >&2; exit 1 ;;
  esac
done

[[ -n "$head" ]] || { echo "error: --head is required" >&2; exit 1; }
[[ -n "$source_branch" ]] || { echo "error: --source is required" >&2; exit 1; }
[[ -n "$title" ]] || { echo "error: --title is required" >&2; exit 1; }
if [[ "$title" =~ ^(Draft:|\[Draft\]|\(Draft\)) ]]; then
  echo "error: do not add a Draft prefix to --title; --draft adds it" >&2
  exit 1
fi
[[ -n "$body_file" ]] || { echo "error: --body-file is required" >&2; exit 1; }
[[ -f "$body_file" ]] || { echo "error: body file not found: $body_file" >&2; exit 1; }
[[ -s "$body_file" ]] || { echo "error: body file is empty: $body_file" >&2; exit 1; }

if grep -nE '__[A-Z0-9_]+__|\{\{[^}]+\}\}|<[A-Z][A-Z0-9_]+>|(^|[^A-Za-z])(TODO|CHANGEME|REPLACE_ME)([^A-Za-z]|$)' "$body_file"; then
  echo "error: the MR body still contains placeholders" >&2
  exit 1
fi

command=(
  glab mr create
  --repo "$repo"
  --head "$head"
  --source-branch "$source_branch"
  --target-branch "$target_branch"
  --title "$title"
  --description-file "$body_file"
  --draft
  --yes
)

if [[ -n "$label" ]]; then
  command+=(--label "$label")
fi

printf 'command:'
printf ' %q' "${command[@]}"
printf '\n'

if [[ $execute -eq 0 ]]; then
  echo "dry run: pass --execute to create the draft merge request"
  exit 0
fi

if ! command -v glab >/dev/null 2>&1; then
  echo "error: glab is not installed; install it from https://gitlab.com/gitlab-org/cli" >&2
  exit 2
fi

if ! glab auth status >/dev/null 2>&1; then
  echo "error: glab is not authenticated; run 'glab auth login'" >&2
  exit 2
fi

"${command[@]}"
