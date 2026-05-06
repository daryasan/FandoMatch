FROM gradle:8.12-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :services:users:bootJar --no-daemon

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/services/users/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]