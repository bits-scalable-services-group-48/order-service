# Java 17 base image
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy application source
COPY src src

# Make gradlew executable and build
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

# Copy jar
RUN cp build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
