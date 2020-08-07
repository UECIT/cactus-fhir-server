# CDS-FHIR-Server
## Overview
This service implements a general FHIR Server.
Our FHIR Server is responsible for accepting, storing and retrieving FHIR resources.
This proof of concept implementation is compliant with both [v1.1](http://developer.nhs.uk/apis/cds-api-1-1-1/) and v2.0 of the CDS API Spec and supports:
- CRUD operations on resources, handling FHIR resource versions
-Responding to limited FHIR search queries meant to retrieve said resources as a bundle

## Source Code Location
The repository for this project is located in a public GitLab space here: https://gitlab.com/ems-test-harness/cds-fhir-server
                 
## Usage/Invocation
### Prerequisites
To clone the project:
```
git clone git@gitlab.com:ems-test-harness/cds-fhir-server.git
```
or
```
git clone https://gitlab.com/ems-test-harness/cds-fhir-server.git
```
Make sure everything has been installed as per the [setup guide](https://kepler.bjss.com/display/NCTH/Developer+Setup+Guide#DeveloperSetupGuide-CDSS-EMS).

### Build steps
This project is configured to run on port 8080. For local machines, this can be accessed at http://localhost:8080. To run the FHIR server, simply run the maven task:
```
mvn spring-boot:run
```

### Live usage
 In general, the FHIR Server will be accessed by the other services as needed.
However, should you wish to access the FHIR Server in isolation, the most common end-points you can hit are:

| Method | Endpoint              | Description         |
|--------|-----------------------|---------------------|
| POST   | /fhir/[resource]      | Creating a resource |
| GET    | /fhir/[resource]/[id] | Reading a resource  |
| PUT    | /fhir/[resource]      | Updating a resource |

## Project Structure
### Implementation
The FHIR Server is a Java Spring Application. It is split into three major layers:
1. Resource Providers - These contain the FHIR end points for various resources that the FHIR Server serves.
2. Service Layer - This mainly contains logic for creating references to resources and bundling together different resources for query results
3. Repository Layer - This layer is for data access containing JPA repositories for the MySQL database

There are also packages for:
- Utilities
- Configuration (For the database, security and fhir server)

### Tests
Minimal tests are provided for part of the service layer and utilities package.
                 
## Deployment
The CI pipeline in GitLab is configured to create a docker image for each commit for the commit sha and update the :latest tag to that one. For non-develop branches, a single image is made with that branch name. The docker image is published to the GitLab registry and a zip file artifact made available for deployment.
                 
For details on where the FHIR Server is deployed see [AWS Deployment](https://kepler.bjss.com/display/NCTH/AWS+Deployment) and for instructions on how to deploy see [Key Operating Procedures](https://kepler.bjss.com/display/NCTH/Key+Operating+Procedures).
                 
## Licence

Unless stated otherwise, the codebase is released under [the MIT License][mit].
This covers both the codebase and any sample code in the documentation.

The documentation is [© Crown copyright][copyright] and available under the terms
of the [Open Government 3.0][ogl] licence.

[rvm]: https://www.ruby-lang.org/en/documentation/installation/#managers
[bundler]: http://bundler.io/
[mit]: LICENCE
[copyright]: http://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/
[ogl]: http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
