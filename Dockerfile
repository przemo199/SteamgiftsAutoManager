FROM openjdk:15-alpine
WORKDIR /app
COPY . .
EXPOSE 8000
RUN ["./gradlew", "build"]
ENTRYPOINT ["java", "-jar", "./build/libs/steamgifts-auto-manager-http.jar"]
