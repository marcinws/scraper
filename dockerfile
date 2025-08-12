# Etap 1: budowanie aplikacji
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Kopiuj pliki projektu do kontenera
COPY pom.xml .
COPY src ./src

# Buduj projekt, generuj JAR
RUN mvn clean package -DskipTests

# Etap 2: uruchomienie aplikacji
FROM eclipse-temurin:17-jre
WORKDIR /app

# Skopiuj JAR z etapu budowania
COPY --from=build /app/target/*.jar app.jar

# Eksponuj port (domyślnie Spring Boot na 8080)
EXPOSE 8080

# Uruchom aplikację
ENTRYPOINT ["java", "-jar", "app.jar"]
