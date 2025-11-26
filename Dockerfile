# ========================================
# ETAPA 1: BUILD (Compilación)
# ========================================
FROM eclipse-temurin:17-jdk as build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# ========================================
# ETAPA 2: RUNTIME (Ejecución)
# ========================================
FROM eclipse-temurin:17-jre

WORKDIR /app

EXPOSE 8080

COPY --from=build /app/build/libs/ExamenMercado-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
