FROM openjdk:11
ADD target/Traveller-0.0.1-SNAPSHOT.jar traveller.jar
ENTRYPOINT ["java", "-jar","traveller.jar"]
EXPOSE 8080

