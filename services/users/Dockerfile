FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :services:users:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/services/users/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]