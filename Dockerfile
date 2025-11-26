# ========================================
# ETAPA 1: BUILD (Compilación)
# ========================================
# Usamos una imagen oficial de Gradle + JDK 17 (FUNCIONA PERFECTO)
FROM gradle:8.3-jdk17-alpine AS build

WORKDIR /home/gradle/project

COPY . .

# Compilar el proyecto y generar el JAR
RUN gradle bootJar --no-daemon

# ========================================
# ETAPA 2: RUNTIME (Ejecución)
# ========================================
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar el JAR generado en la etapa build
COPY --from=build /home/gradle/project/build/libs/ExamenMercado-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
