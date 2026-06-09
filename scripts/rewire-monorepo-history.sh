#!/usr/bin/env bash
# Импорт git-истории user-service и notification-service в монорепозиторий.
# Запускать из корня платформы. НЕ пушит в remote.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

export FILTER_BRANCH_SQUELCH_WARNING=1

if [ ! -d ".git" ]; then
  echo "ERROR: not a git repository"
  exit 1
fi

BACKUP_ROOT="${ROOT}/.git/monorepo-import-backup"
mkdir -p "${BACKUP_ROOT}"

import_service_history() {
  local svc="$1"
  local backup_src="${BACKUP_ROOT}/${svc}"
  local nested_git=""

  # Источник: nested .git в сервисе или backup после первого прогона
  if [ -d "${ROOT}/${svc}/.git" ]; then
    nested_git="${ROOT}/${svc}/.git"
    echo "Source: ${ROOT}/${svc}"
  elif [ -d "${BACKUP_ROOT}/${svc}/.git" ]; then
    nested_git="${BACKUP_ROOT}/${svc}/.git"
    echo "Source: backup ${BACKUP_ROOT}/${svc}"
  else
    echo "SKIP ${svc}: no .git found"
    return 0
  fi

  echo "=== Importing ${svc} history ==="

  # Backup текущего рабочего каталога (Gradle-миграция и т.д.)
  if [ -d "${ROOT}/${svc}" ]; then
    rm -rf "${backup_src}"
    cp -a "${ROOT}/${svc}" "${backup_src}"
    echo "Backed up working tree to ${backup_src}"
  fi

  local tmp="${ROOT}/.git/tmp-import-${svc}"
  rm -rf "${tmp}"

  if [ -d "${ROOT}/${svc}/.git" ]; then
    git clone "${ROOT}/${svc}" "${tmp}"
  else
    git clone "${BACKUP_ROOT}/${svc}" "${tmp}"
  fi

  cd "${tmp}"
  local default_branch
  default_branch="$(git symbolic-ref --short HEAD 2>/dev/null || echo main)"

  if command -v git-filter-repo >/dev/null 2>&1; then
    git filter-repo --to-subdirectory-filter "${svc}" --force
  else
    git filter-branch -f --prune-empty \
      --tree-filter "
        if [ ! -d ${svc} ]; then
          mkdir -p _sub_
          find . -mindepth 1 -maxdepth 1 ! -name _sub_ ! -name .git -exec mv {} _sub_/ \\;
          mkdir -p ${svc}
          mv _sub_/* ${svc}/ 2>/dev/null || true
          rmdir _sub_ 2>/dev/null || true
        fi
      " -- --all
  fi

  cd "${ROOT}"

  # Удаляем каталог сервиса чтобы merge не конфликтовал с untracked files
  rm -rf "${ROOT}/${svc}"

  local remote_name="import-${svc}"
  git remote remove "${remote_name}" 2>/dev/null || true
  git remote add "${remote_name}" "${tmp}"
  git fetch "${remote_name}"

  git merge "${remote_name}/${default_branch}" --allow-unrelated-histories \
    -m "Import ${svc} repository history into monorepo"

  git remote remove "${remote_name}"
  rm -rf "${tmp}"

  # Восстанавливаем рабочие изменения поверх импортированной истории
  if [ -d "${backup_src}" ]; then
    echo "Restoring working tree from backup..."
    rsync -a --exclude='.git' "${backup_src}/" "${ROOT}/${svc}/"
  fi

  rm -rf "${ROOT}/${svc}/.git" 2>/dev/null || true
  echo "Done: ${svc}"
}

echo "Platform branch: $(git branch --show-current)"
echo "Local-only operation. Remote push is NOT performed."
echo ""

import_service_history "user-service"
import_service_history "notification-service"

echo ""
echo "Verify:"
echo "  git log --oneline --graph --all | head -30"
echo "  git log --oneline -- user-service/ | head -5"
echo "  git log --oneline -- notification-service/ | head -5"
