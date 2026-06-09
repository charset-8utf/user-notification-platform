#!/usr/bin/env bash
# Push текущей ветки в GitLab (зеркало GitHub). Требует проект на GitLab и SSH-ключ.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

BRANCH="${1:-$(git branch --show-current)}"
GITLAB_REMOTE="${GITLAB_REMOTE:-gitlab}"
GITLAB_URL="${GITLAB_URL:-git@gitlab.com:charset-8utf/user-notification-platform.git}"

if ! git remote get-url "${GITLAB_REMOTE}" >/dev/null 2>&1; then
  echo "Adding remote ${GITLAB_REMOTE} → ${GITLAB_URL}"
  git remote add "${GITLAB_REMOTE}" "${GITLAB_URL}"
else
  git remote set-url "${GITLAB_REMOTE}" "${GITLAB_URL}"
fi

echo "Pushing ${BRANCH} to ${GITLAB_REMOTE}..."
git push -u "${GITLAB_REMOTE}" "${BRANCH}"

echo "Done. Pipeline: ${GITLAB_URL%.git}/-/pipelines"
