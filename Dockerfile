FROM openjdk:8-alpine

COPY generic-upload-service/target/generic-upload-*.jar app.jar

CMD java -jar app.jar
