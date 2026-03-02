# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargar dependencias (cache de Docker)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar JAR desde el builder
COPY --from=builder /app/target/*.jar app.jar

# Cambiar a usuario no-root
USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
