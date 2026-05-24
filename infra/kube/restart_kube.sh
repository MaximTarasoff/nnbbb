#!/bin/bash

# ШАГ 1: поднятие сервисов приложения

# Запустили локальный Kubernetes-кластер с помощью minikube, используя Docker как драйвер
# (кластер будет запущен внутри докер контейнера)
minikube start --driver=docker

# Создали ConfigMap с именем selenoid-config, файл будет доступен под ключом browsers.json
kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

# Устанавливаем Helm чарт с именем релиза nbank, беря шаблоны из ./nbank-chart
# Это создаст все ресурсв, описанные в шаблонах Helm (Deployment, Service)
helm install nbank ./nbank-chart

# Все сервисы в namespace=default
kubectl get svc

# Все поды в namespace=default
kubectl get pods

# Логи конкретного сервиса
kubectl logs deployment/backend

# Проброс портов на локальную машину
kubectl port-forward svc/postgres 5433:5432 > /dev/null 2>&1 &
kubectl port-forward svc/frontend 3000:80 > /dev/null 2>&1 &#  > /dev/null 2>&1 & (проброс порта в фоновом режиме)
kubectl port-forward svc/backend 4111:4111 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid 4444:4444 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid-ui 8080:8080 > /dev/null 2>&1 &

# Увеличить количество реплик frontend/selenoid-ui до 3
kubectl scale deployment/frontend --replicas=3
kubectl scale deployment/selenoid-ui --replicas=3
kubectl get pods