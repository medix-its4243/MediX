FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY config-server/pom.xml config-server/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY patient-service/pom.xml patient-service/pom.xml
COPY doctor-service/pom.xml doctor-service/pom.xml
COPY appointment-service/pom.xml appointment-service/pom.xml
COPY medical-record-service/pom.xml medical-record-service/pom.xml
COPY billing-service/pom.xml billing-service/pom.xml
COPY eureka-server/src eureka-server/src
RUN mvn -B -pl eureka-server -am -DskipTests package
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /workspace/eureka-server/target/eureka-server-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/app.jar"]
