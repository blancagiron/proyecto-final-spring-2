# Multi-stage build
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY domain/pom.xml domain/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY infrastructure/api/pom.xml infrastructure/api/
COPY infrastructure/jpa/pom.xml infrastructure/jpa/

# Descargar dependencias (esto se cachea si no cambian los pom.xml)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/jpa/src infrastructure/jpa/src
COPY infrastructure/api/src infrastructure/api/src

# Compilar la aplicación
RUN mvn clean package -DskipTests -Pproduction

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no privilegiado
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Directorio de trabajo
WORKDIR /app

# Copiar jar desde la etapa de build
COPY --from=build /app/infrastructure/api/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]