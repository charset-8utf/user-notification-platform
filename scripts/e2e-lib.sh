#!/usr/bin/env bash
# Общие хелперы для E2E-скриптов (source, не execute).

e2e_poll_max() {
  if [ -n "${GITHUB_ACTIONS:-}${CI:-}" ]; then
    echo "${E2E_POLL_MAX:-90}"
  else
    echo "${E2E_POLL_MAX:-30}"
  fi
}

e2e_poll_sleep() {
  echo "${E2E_POLL_SLEEP:-2}"
}
