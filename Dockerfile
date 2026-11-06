# ---------- Build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache de dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

# Código
COPY src/ src/
RUN ./mvnw -B -q -DskipTests package

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre AS runtime

RUN groupadd --system festa && useradd --system --gid festa festa
WORKDIR /app

COPY --from=build /workspace/target/festa-user-service-*.jar app.jar
RUN chown -R festa:festa /app
USER festa

EXPOSE 8082

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]