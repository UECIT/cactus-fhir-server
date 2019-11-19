FROM maven:3-jdk-11 as deps
WORKDIR /app

COPY pom.xml .
RUN mvn -B -Dmaven.repo.local=/app/.m2 dependency:go-offline

FROM deps as build

COPY src src
RUN mvn -B -Dmaven.repo.local=/app/.m2 package

FROM openjdk:11-jre-slim
WORKDIR /app
VOLUME /tmp
COPY start-fhir.sh /app
RUN chmod +x start-fhir.sh
ENTRYPOINT [ "/app/start-fhir.sh" ]
EXPOSE 8084

COPY --from=build /app/target/cds-fhir-server.war /app