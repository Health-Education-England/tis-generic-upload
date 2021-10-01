FROM openjdk:8-alpine

COPY target/app.jar app.jar

CMD ["java", "-jar", "app.jar"]