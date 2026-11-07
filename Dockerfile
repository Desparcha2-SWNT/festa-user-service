# ---------- Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache de dependencias
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Código
COPY src/ src/
RUN mvn -B -q -DskipTests package

# ---------- Runtime ----------
# Alpine, igual que auth-service: trae el wget (busybox) que usa el HEALTHCHECK.
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S festa && adduser -S festa -G festa
WORKDIR /app

COPY --from=build /workspace/target/festa-user-service-*.jar app.jar
RUN chown -R festa:festa /app
USER festa

EXPOSE 8082

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8082/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]