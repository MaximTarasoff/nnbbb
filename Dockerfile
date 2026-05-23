# базовый докер образ
# Зачем каждый раз с нуля строить базовый образ (java, mvn, git)
# голый докер образ и устанавливать java, maven
# если можно создать ораз поверх другого образа, где все уже установлено

# Маркетплейс всех докер образов - docker hub
FROM maven:3.9.9-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}


# создаем рабочую директорию и в ней работаем
WORKDIR /app
# теперь все команды будут выполняться внутри app

# копируем помник
COPY pom.xml .
# запустить сборку всех зависимостей
RUN mvn dependency:go-offline # скопировать все зависимости и использовать их в кэше

# скопировать весь проект (. . -  в текущую папку)
COPY . .

# теперь внутри есть зависимости, есть весь проект и мы готовы запускать тесты

# юзер под которым запускаем
USER root

# mvn test -P api
# mvn -DskipTests=true surefire-report:report
# лог выводился не в консоль а в файлах (2>&1) (tee - и внутрь файла и на экран консоли)
# bash file
# сть разница между RUN и CMD (CMD не выполняются сразу)
CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile: ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
    } 2>&1 | tee /app/logs/run.log"