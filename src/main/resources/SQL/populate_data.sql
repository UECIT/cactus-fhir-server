USE cdss_supplier;

DROP TABLE IF EXISTS cdss_supplier.resource;

CREATE TABLE cdss_supplier.resource (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  resource_type		VARCHAR(255),
  resource_json		VARCHAR(60000),
  PRIMARY KEY (id)
);
 