#!/usr/bin/env sh
echo "Starting a Spring Boot FHIR place on $HOSTNAME"
exec java -jar cds-fhir-server.war
