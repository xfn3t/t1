FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY metric-datasource-starter/pom.xml ./metric-datasource-starter/
COPY starter.tester/pom.xml ./starter.tester/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B dependency:go-offline

COPY metric-datasource-starter ./metric-datasource-starter
COPY starter.tester ./starter.tester

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean install -DskipTests -T1C

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /workspace/starter.tester/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
