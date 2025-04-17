FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/bank-cards-management-api.jar app.jar

COPY src/main/resources/jwt /app/jwt
COPY src/main/resources/secret-key.txt /app/secret-key.txt

ENV JWT_ACCESS_PATH=/app/jwt/access.txt
ENV JWT_REFRESH_PATH=/app/jwt/refresh.txt
ENV SECRET_KEY=/app/secret-key.txt

ENTRYPOINT ["java", "-jar", "app.jar"]
