FROM maven:3.8.6-openjdk-18 AS build

# 1. Копируем ТОЛЬКО файлы с зависимостями
COPY pom.xml .
COPY src/main/resources/db/changelog ./src/main/resources/db/changelog

# 2. Скачиваем зависимости (кешируемый слой)
RUN mvn dependency:go-offline -B

# 3. Копируем остальной исходный код
COPY src ./src

# 4. Собираем приложение (используем кешированные зависимости)
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /target/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
