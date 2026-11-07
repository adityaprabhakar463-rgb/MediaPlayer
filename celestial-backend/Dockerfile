# M0 build/run image
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew* gradle* settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew 2>/dev/null || true
# Prefer wrapper if present; fallback expects gradle in environment (not in this image)
RUN ./gradlew --no-daemon installDist || gradle --no-daemon installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/install/celestial-backend/ /app/
EXPOSE 8080
ENTRYPOINT ["/app/bin/celestial-backend"]
