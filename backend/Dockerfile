FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
RUN mvn -f backend/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/event-management-backend-1.0.0.jar app.jar
COPY backend/entrypoint.sh /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["sh", "/app/entrypoint.sh"]
