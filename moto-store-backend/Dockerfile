# Build stage: usar una imagen válida de Maven + Temurin
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Si la plataforma (Render) construye desde la raíz del repo, permite
# ajustar la carpeta de la aplicación con el build-arg `APP_DIR`.
ARG APP_DIR=moto-store-backend
WORKDIR /app

# Copiar primero archivos clave para aprovechar la cache de Docker
# Si el build context es la raíz del repo, esto copiará `${APP_DIR}/pom.xml`.
COPY ${APP_DIR}/pom.xml ./
COPY ${APP_DIR}/src ./src

# Compilar el proyecto usando Maven dentro del contenedor
RUN mvn -B -DskipTests package

# Runtime stage usando JRE estable
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
