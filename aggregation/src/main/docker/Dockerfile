FROM amazoncorretto:17-alpine
VOLUME /app
COPY ./aggregation*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
