#!/usr/bin/env bash
# Builds the personal release APK in CI for the current (or given) branch and copies it into the
# Android shared folder. It is the "after_implement" hook target of the apk-delivery extension and
# only runs on the development VM; everywhere else it skips cleanly.
set -euo pipefail

REPO="${REPO:-GiovanniDrago/wallhaven-client}"
WORKFLOW="${WORKFLOW:-personal-release.yml}"
DEST="${DEST:-/mnt/shared/debian/wallhaven-client}"

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
DELIVER_SCRIPT="${REPO_ROOT}/scripts/deliver-to-shared.sh"

if [[ ! -d "$DEST" ]] || ! command -v gh >/dev/null 2>&1; then
	echo "[apk-delivery] Skipped: not the development VM (missing $DEST or gh)."
	exit 0
fi

BRANCH="${1:-$(git rev-parse --abbrev-ref HEAD)}"
if [[ "$BRANCH" == "HEAD" || -z "$BRANCH" ]]; then
	echo "[apk-delivery] Refusing: could not determine the current branch." >&2
	exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
	echo "[apk-delivery] Refusing: the working tree is dirty. Commit or stash first." >&2
	exit 1
fi

echo "[apk-delivery] Pushing ${BRANCH} and dispatching ${WORKFLOW}..."
git push -u origin "$BRANCH"

gh workflow run "$WORKFLOW" --repo "$REPO" --ref "$BRANCH"

sleep 5
RUN_ID="$(gh run list --repo "$REPO" --workflow "$WORKFLOW" --branch "$BRANCH" --limit 1 \
	--json databaseId --jq '.[0].databaseId')"
if [[ -z "$RUN_ID" || "$RUN_ID" == "null" ]]; then
	echo "[apk-delivery] Could not find the dispatched run." >&2
	exit 1
fi

gh run watch "$RUN_ID" --repo "$REPO" --exit-status

if [[ ! -x "$DELIVER_SCRIPT" ]]; then
	echo "[apk-delivery] Missing deliver script: $DELIVER_SCRIPT" >&2
	exit 1
fi
"$DELIVER_SCRIPT"
