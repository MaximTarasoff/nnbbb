#!/usr/bin/env bash
set -e

# запуск по команде ./run-tests-with-docker-compose.sh (или c аргументами ui/api/all)

IMAGE_NAME="nbank-tests"
TEST_PROFILE=${1:-all}   # Аргумент запуска: api, ui, all
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR="./test-output/$TIMESTAMP"
COMPOSE_FILE=./infra/docker_compose/docker-compose.yml
HOST_PWD=$(pwd -W 2>/dev/null || pwd)  #для запусков на Linux и Windows

# Подготовка окружения
echo ">>> Подготавливаем директории для результатов..."
mkdir -p "$HOST_PWD/$TEST_OUTPUT_DIR/logs" "$HOST_PWD/$TEST_OUTPUT_DIR/results" "$HOST_PWD/$TEST_OUTPUT_DIR/report"

# Функция для запуска тестов по профилю
run_tests() {
  local profile=$1
  echo ">>> Запускаем тесты с профилем: $profile"

  echo ">>> Запуск окружения Docker Compose"
  docker compose -f "$COMPOSE_FILE" up -d

  # Запускаем контейнер
  docker run --rm \
    -v "$HOST_PWD/$TEST_OUTPUT_DIR/logs":/app/logs \
    -v "$HOST_PWD/$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
    -v "$HOST_PWD/$TEST_OUTPUT_DIR/report":/app/target/site \
    -e TEST_PROFILE="$TEST_PROFILE" \
    -e APIBASEURL=http://localhost:4111 \
    -e UIBASEURL=http://localhost:3000 \
    -e SELENOID_URL=http://localhost:4444 \
    -e SELENOID_UI_URL=http://localhost:8080 \
    $IMAGE_NAME \

  echo ">>> Тесты для профиля $profile завершены!"
}

# Основная логика
if [[ "$TEST_PROFILE" == "all" ]]; then
  echo ">>> Запуск всех тестов: api и ui"
  run_tests "api"
  run_tests "ui"
else
  run_tests "$TEST_PROFILE"
fi

echo ">>> Останавливаем работу Docker Compose"
docker compose -f "$COMPOSE_FILE" down

# Вывод итогов
echo ""
echo ">>> Все тесты завершены."
echo ">>> Логи: $TEST_OUTPUT_DIR/logs/run.log"
echo ">>> Результаты: $TEST_OUTPUT_DIR/results"
echo ">>> Отчёт: $TEST_OUTPUT_DIR/report"