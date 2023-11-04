FROM --platform="linux/amd64" gradle:8.4.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN ["./gradlew", "build"]

FROM openjdk:21-jdk-slim-buster
EXPOSE 8000
COPY --from=build /app/build/libs/*.jar /app/steamgifts-auto-manager-http.jar
ENTRYPOINT ["java", "-jar", "/app/steamgifts-auto-manager-http.jar"]
