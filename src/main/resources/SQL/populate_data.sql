USE cdss_resources;

DROP TABLE IF EXISTS cdss_resources.resource;

CREATE TABLE cdss_resources.resource (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  resource_type		VARCHAR(255),
  resource_json		VARCHAR(60000),
  PRIMARY KEY (id)
);
 