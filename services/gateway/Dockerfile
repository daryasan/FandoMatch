FROM gradle:8.12-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :services:gateway:bootJar --no-daemon

ENV GRADLE_OPTS="-Xms512m -Xmx1536m -XX:MaxMetaspaceSize=512m"

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/services/gateway/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]