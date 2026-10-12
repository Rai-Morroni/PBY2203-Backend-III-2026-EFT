# Etapa 1: Compilación
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila el proyecto saltándose las pruebas para mayor velocidad
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copia el JAR generado en la etapa 1
COPY --from=build /app/target/*.jar app.jar
# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]