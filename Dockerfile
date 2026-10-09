# Build needs the JDK (toolchain 25) plus Node, because :backend:processResources
# runs the Vite build and packs the bundle into the jar.
FROM eclipse-temurin:25-jdk AS build
COPY --from=node:22 /usr/local /usr/local
WORKDIR /src
COPY . .
RUN ./gradlew --no-daemon :backend:bootJar -x test

FROM eclipse-temurin:25-jre
COPY --from=build /src/backend/build/libs/*.jar /app.jar
# Render/Cloud Run inject $PORT. MaxRAMPercentage keeps the JVM inside a 512MB instance.
CMD ["sh", "-c", "exec java -XX:MaxRAMPercentage=70 -jar /app.jar --server.port=${PORT:-8080}"]
