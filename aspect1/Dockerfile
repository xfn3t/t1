FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn/
COPY src src/

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
