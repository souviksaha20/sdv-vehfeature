FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-11-jdk -y

VOLUME /tmp
COPY target/*.jar .
ENTRYPOINT ["java","-jar","vehfeature-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]
EXPOSE 8080