FROM --platform="linux/amd64" gradle:7.3.3-jdk17 AS build
WORKDIR /app
COPY . .
RUN ["./gradlew", "build"]

FROM openjdk:19-jdk-slim-buster
EXPOSE 8000
COPY --from=build /app/build/libs/*.jar /app/steamgifts-auto-manager-http.jar
ENTRYPOINT ["java", "-jar", "/app/steamgifts-auto-manager-http.jar"]
