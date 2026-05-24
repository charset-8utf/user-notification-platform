#!/usr/bin/env sh
# Однократно переименовать каталоги под единый нейминг (из родителя user-notification-platform).
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

if [ -d "UserServiceSpringBoot" ] && [ ! -d "user-service" ]; then
  echo "→ UserServiceSpringBoot → user-service"
  mv "UserServiceSpringBoot" "user-service"
fi

POSTMAN_COL="$ROOT/user-service/postman/collections"
if [ -d "$POSTMAN_COL/UserServiceSpringBoot API-1" ] && [ ! -d "$POSTMAN_COL/user-service API-1" ]; then
  echo "→ Postman collection folder"
  mv "$POSTMAN_COL/UserServiceSpringBoot API-1" "$POSTMAN_COL/user-service API-1"
fi

echo "Готово. Открой в IDE папку: $ROOT"
echo "Путь к сервису: $ROOT/user-service"
