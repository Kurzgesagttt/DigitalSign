# Build stage com Maven e JDK 17
FROM maven:3.8.8-amazoncorretto-17-al2023 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage com apenas JRE
FROM amazoncorretto:17-al2023
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
