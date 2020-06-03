FROM maven:3-jdk-11 as build
WORKDIR /app

ARG GITHUB_USER
ARG GITHUB_TOKEN
ENV GITHUB_USER=$GITHUB_USER GITHUB_TOKEN=$GITHUB_TOKEN
COPY pom.xml settings.xml /app/
COPY src src
RUN mvn -B package -DskipTests --settings settings.xml

FROM openjdk:11-jre-slim
WORKDIR /app
VOLUME /tmp
COPY start-fhir.sh /app
RUN chmod +x start-fhir.sh
ENTRYPOINT [ "/app/start-fhir.sh" ]
EXPOSE 8084

COPY --from=build /app/target/cds-fhir-server.war /app