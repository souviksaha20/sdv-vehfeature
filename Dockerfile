FROM eclipse-temurin:11-jdk-alpine
VOLUME /tmp
COPY target/*.jar vehfeature-0.0.1-SNAPSHOT-jar-with-dependencies.jar
ENTRYPOINT ["java","-jar","vehfeature-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]
EXPOSE 8080