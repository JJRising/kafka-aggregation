FROM amazoncorretto:17-alpine
VOLUME /app
COPY ./generator*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
