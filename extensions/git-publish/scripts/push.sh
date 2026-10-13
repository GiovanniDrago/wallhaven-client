#!/usr/bin/env bash
# Pushes the current branch to origin, setting the upstream on the first push. Used by the
# git-publish extension's after_implement and after_converge hooks. Safe everywhere: it is a no-op
# outside a Git repository, on a detached HEAD, or when no origin remote exists.
set -euo pipefail

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
	echo "[git-publish] Not a git repository, skipping."
	exit 0
fi

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" == "HEAD" || -z "$BRANCH" ]]; then
	echo "[git-publish] Detached HEAD, skipping."
	exit 0
fi

if ! git remote get-url origin >/dev/null 2>&1; then
	echo "[git-publish] No 'origin' remote, skipping."
	exit 0
fi

echo "[git-publish] Pushing ${BRANCH} to origin..."
git push -u origin "$BRANCH"
