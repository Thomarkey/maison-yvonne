# Maison Yvonne

Monolith: Spring Boot (Kotlin) backend that serves the React frontend bundle.

    backend/   Kotlin + Spring Boot, API under /api
    frontend/  React + TypeScript + Vite

## Develop

Two terminals — Vite proxies `/api` to the backend on :8080:

    ./gradlew :backend:bootRun -x processResources
    cd frontend && npm install && npm run dev

## Build & run one jar

    ./gradlew build          # builds the frontend into the jar's static resources
    java -jar backend/build/libs/backend.jar

## Test

    ./gradlew test
