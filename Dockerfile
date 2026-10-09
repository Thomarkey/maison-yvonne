# Build: Gradle needs Node too, since it bundles the frontend into the jar.
FROM node:22-slim AS build
COPY --from=eclipse-temurin:25-jdk /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk PATH=/opt/java/openjdk/bin:$PATH
WORKDIR /src
COPY . .
RUN ./gradlew :backend:bootJar --no-daemon && cp backend/build/libs/backend.jar /app.jar

FROM eclipse-temurin:25-jre
COPY --from=build /app.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
