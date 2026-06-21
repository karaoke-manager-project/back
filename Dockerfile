FROM docker.io/library/gradle:9.4-jdk17 AS build

WORKDIR /app

COPY . .

RUN gradle build -x test

FROM docker.io/library/eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
