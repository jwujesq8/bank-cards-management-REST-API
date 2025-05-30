FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/bank-cards-management-api.jar app.jar

COPY secrets/encryption-util /app/encryption-util
COPY secrets/jwt /app/jwt

ENV SECRET_KEY_PATH=/app/encryption-util/secret-key.txt
ENV JWT_ACCESS_PATH=/app/jwt/access.txt
ENV JWT_REFRESH_PATH=/app/jwt/refresh.txt

ENTRYPOINT ["java", "-jar", "app.jar"]
