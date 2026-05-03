FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common-contracts/pom.xml common-contracts/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY trip-service/pom.xml trip-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY common-contracts/src common-contracts/src
COPY notification-service/src notification-service/src

RUN mvn -pl notification-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/notification-service/target/notification-service-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
