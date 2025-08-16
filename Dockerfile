#FROM ubuntu:latest
#LABEL authors="DESKTOP"
#
#ENTRYPOINT ["top", "-b"]

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# Stage 2: Create the final, smaller image
FROM eclipse-temurin:21-jre-alpine
# Setting the locale and character encoding for the container environment
ENV LANG C.UTF-8
WORKDIR /app
COPY --from=build /app/target/Xplore-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8081
# Adding the file.encoding property to the java command to force the JVM to use UTF-8
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]