d fromn# Maison Yvonne

Monolith: Spring Boot (Kotlin) backend that serves the React frontend bundle.

    backend/   Kotlin + Spring Boot, API under /api
    frontend/  React + TypeScript + Vite

The public website lives on `/`, the pilot app on `/app`.

## Develop

Needs a local MongoDB (`brew services start mongodb-community`). The `dev` profile
seeds the pilot data into the `maisonyvonne` database on first run.

    ./gradlew :backend:bootRun          # http://localhost:8080/app
    cd frontend && npm run dev          # Vite on :5173, proxies /api to :8080

When running `ApplicationKt` directly from IntelliJ, set the program argument
`--spring.profiles.active=dev`; otherwise the dev-only seeder is not enabled.

Demo accounts (dev profile only, password `yvonne`):

    yasmina@maisonyvonne.be   admin
    louis@maisonyvonne.be     admin
    sophie@example.com        family, linked to Louisa

Inspect the data with Compass on `mongodb://localhost:27017` → database `maisonyvonne`.

## Build & run one jar

    ./gradlew build          # builds the frontend into the jar's static resources
    java -jar backend/build/libs/backend.jar

Production expects `SPRING_MONGODB_URI` and `SPRING_MONGODB_DATABASE` and, behind HTTPS,
`SERVER_SERVLET_SESSION_COOKIE_SECURE=true`.

## Test

    ./gradlew test           # PilotFlowTest runs against MongoDB via Testcontainers (needs Docker)
