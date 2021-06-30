FROM maven:3.6.3-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:11
COPY --from=build /usr/src/app/target/sentiments-reporter-java-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/app/sentiments-reporter-java-1.0-SNAPSHOT-jar-with-dependencies.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/app/sentiments-reporter-java-1.0-SNAPSHOT-jar-with-dependencies.jar"]