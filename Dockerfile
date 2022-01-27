FROM --platform=$BUILDPLATFORM openjdk:17-alpine AS build
WORKDIR /app
COPY . .
RUN ["./gradlew", "build"]

FROM --platform=$BUILDPLATFORM openjdk:17-alpine
EXPOSE 8000
COPY --from=build /app/build/libs/*.jar /app/steamgifts-auto-manager-http.jar
ENTRYPOINT ["java", "-jar", "/app/steamgifts-auto-manager-http.jar"]
