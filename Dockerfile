FROM openjdk:8-alpine

COPY target/generic-upload-*.jar app.jar

CMD ["java", "-jar", "app.jar"]