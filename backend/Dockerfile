# Build stage
FROM maven:3.9.9-eclipse-temurin-17-focal AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/classes /app/classes
COPY --from=build /app/lib /app/lib
EXPOSE 8080
CMD ["java", "-cp", "classes:lib/*", "Main"]