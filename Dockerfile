FROM maven:3-jdk-11 as deps
WORKDIR /app

ARG GITHUB_USER
ARG GITHUB_TOKEN
ENV GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN

COPY pom.xml .
COPY settings.xml /app/
RUN mvn -B -Dmaven.repo.local=/app/.m2 dependency:go-offline --settings settings.xml

FROM deps as build

COPY src src
COPY settings.xml /app/
RUN mvn -B -Dmaven.repo.local=/app/.m2 package --settings settings.xml

FROM openjdk:11-jre-slim
WORKDIR /app
VOLUME /tmp
COPY start-fhir.sh /app
RUN chmod +x start-fhir.sh
ENTRYPOINT [ "/app/start-fhir.sh" ]
EXPOSE 8084

COPY --from=build /app/target/cds-fhir-server.war /app