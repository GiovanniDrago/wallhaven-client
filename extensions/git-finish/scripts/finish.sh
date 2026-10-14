#!/usr/bin/env bash
# Merges a finished feature branch into the base branch and pushes. Used by the git-finish
# extension's after_converge hook. It is a no-op when it is not safe to finish: outside a Git
# repository, on a detached HEAD, already on the base branch, with a dirty working tree, when no
# active feature is registered, or when the feature's tasks.md still has open tasks.
set -euo pipefail

BASE="${BASE_BRANCH:-main}"
REMOTE="${REMOTE:-origin}"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
	echo "[git-finish] Not a git repository, skipping."
	exit 0
fi

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" == "HEAD" || -z "$BRANCH" ]]; then
	echo "[git-finish] Detached HEAD, skipping."
	exit 0
fi

if [[ "$BRANCH" == "$BASE" ]]; then
	echo "[git-finish] Already on ${BASE}, nothing to merge."
	exit 0
fi

if [[ -n "$(git status --porcelain)" ]]; then
	echo "[git-finish] Working tree is dirty, skipping merge."
	exit 0
fi

FEATURE_DIR=""
if [[ -f .specify/feature.json ]]; then
	FEATURE_DIR="$(grep -oE '"feature_directory"[[:space:]]*:[[:space:]]*"[^"]+"' .specify/feature.json \
		| sed -E 's/.*"([^"]+)"$/\1/')"
fi

if [[ -z "$FEATURE_DIR" || ! -f "${FEATURE_DIR}/tasks.md" ]]; then
	echo "[git-finish] No active feature with a tasks.md, skipping merge."
	exit 0
fi

PENDING="$(grep -cE '^\s*-\s*\[ \]' "${FEATURE_DIR}/tasks.md" || true)"
if [[ "${PENDING:-0}" -gt 0 ]]; then
	echo "[git-finish] ${PENDING} open task(s) in ${FEATURE_DIR}/tasks.md, skipping merge."
	exit 0
fi

echo "[git-finish] Publishing ${BRANCH}..."
git push -u "$REMOTE" "$BRANCH"

echo "[git-finish] Merging ${BRANCH} into ${BASE}..."
git switch "$BASE"
if ! git pull --ff-only "$REMOTE" "$BASE"; then
	echo "[git-finish] Could not fast-forward ${BASE} from ${REMOTE}; aborting merge." >&2
	git switch "$BRANCH"
	exit 0
fi

git merge --no-ff "$BRANCH" -m "Merge ${BRANCH} into ${BASE}"
git push "$REMOTE" "$BASE"

echo "[git-finish] Merged ${BRANCH} into ${BASE} and pushed."
