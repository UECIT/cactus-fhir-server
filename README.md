# CDS-FHIR-Server
## Running with Docker

To run this locally just run:
```bash
docker-compose up
```

This will obtain the mySQL image, initialize the ```cdss_resources``` database and run the ```populate_data.sql``` script which creates the tables and populates with the mock data.

It will also download and run the cds-fhir-server image from the docker registry in gitlab.com.

Within the ```docker-compose.yml``` file. The mySQL host can be changed from ```cdss-mysql``` to ```localhost``` if the mysql server is being hosted on the local machine instead of the docker container.

Have a look at Docker documentation for more information.

## Running without Docker

Create a MySql database and run the sql scripts under `src/main/resources/sql`

Update the `src/main/resources/application.properties` file to point to the database.

**Note that for MySQL 8+ references to columns named system in the script need to be updated to \`system\` as it is a reserved word and must be escaped with the back-ticks**

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
