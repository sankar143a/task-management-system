# Build stage
FROM maven:3.9.9-eclipse-temurin-17-focal AS build
WORKDIR /app
# backend folder ని /app/backend లోకి copy చేయండి
COPY backend /app/backend
# backend folder లోకి వెళ్లి build చేయండి
WORKDIR /app/backend
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Compiled files ని సరైన path నుండి copy చేయండి
COPY --from=build /app/backend/target/classes /app/classes
COPY --from=build /app/backend/lib /app/lib
EXPOSE 8080
CMD ["java", "-cp", "classes:lib/*", "Main"]