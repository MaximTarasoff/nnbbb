#!/usr/bin/env bash

# для запуска через консоль bash ./push-tests.sh <DOCKERHUB_USER> <DOCKERHUB_TOKEN> <TAG>

# настройка переменных
IMAGE_NAME="nbank-tests"                            # имя локального образа
DOCKERHUB_USER="${1}"                               # логин Docker Hub
DOCKERHUB_TOKEN="${2}"                              # токен Docker Hub
TAG="${3:-latest}"                                  # тег образа (по умолчанию latest)
REMOTE_IMAGE="${DOCKERHUB_USER}/${IMAGE_NAME}:${TAG}"

# проверка передачи аргументов в скрипт
if [ -z "$DOCKERHUB_USER" ] || [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "Ошибка: необходимо передать имя пользователя и токен Docker Hub."
  echo "Пример: bash push-tests.sh <dockerhub_user> <dockerhub_token> [tag]"
  exit 1
fi

echo ">>> Осуществляем вход в Docker Hub как $DOCKERHUB_USER..."
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin

echo ">>> Теггируем образ..."
docker tag "$IMAGE_NAME:latest" "$REMOTE_IMAGE"

echo ">>> Отправляем образ в Docker Hub..."
docker push "$REMOTE_IMAGE"

echo ">>> Образ успешно отправлен в Docker Hub: $REMOTE_IMAGE"
echo "Скачать образ можно командой:"
echo "docker pull $REMOTE_IMAGE"