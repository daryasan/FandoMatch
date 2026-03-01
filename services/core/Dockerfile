FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :services:core:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/services/core/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]