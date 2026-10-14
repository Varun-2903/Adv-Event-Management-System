# Event Management API

Spring Boot REST API for the Event Management System.

## Run locally

Requires Java 17+ and MySQL. Configure the database in `src/main/resources/application.properties`, then run:

```sh
mvn spring-boot:run
```

The API listens on port 8080 by default. The default admin account is `admin@event.com` / `admin123`.

## Environment

- `DATABASE_URL` or `SPRING_DATASOURCE_URL`: JDBC database URL (Render's PostgreSQL URL is supported).
- `DATABASE_USERNAME` and `DATABASE_PASSWORD`: database credentials.
- `JWT_SECRET`: signing key for authentication tokens.
- `CORS_ALLOWED_ORIGINS`: comma-separated frontend origins.
- `ADMIN_EMAIL` and `ADMIN_INITIAL_PASSWORD`: initial administrator credentials.
- `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`: optional payment integration credentials.

## Deploy with Docker

Build this repository from its root with `Dockerfile`. The container listens on the `PORT` environment variable, defaulting to 8080.
