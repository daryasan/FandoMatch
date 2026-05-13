FROM gradle:8.12-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :services:messaging:bootJar --no-daemon

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/services/messaging/build/libs/*.jar app.jar
ENTRYPOINT ["java","-Xmx512m","-XX:MaxMetaspaceSize=256m","-jar","app.jar"]
