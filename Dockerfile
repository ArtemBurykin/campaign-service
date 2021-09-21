FROM gradle:6.7.0-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon

FROM amazoncorretto:11
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*-fat.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]