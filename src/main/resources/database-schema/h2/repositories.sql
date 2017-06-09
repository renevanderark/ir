DROP TABLE IF EXISTS repositories;

CREATE TABLE repositories (
  id number(10) AUTO_INCREMENT,
  name VARCHAR2(255) DEFAULT NULL,
  url varchar2(255) DEFAULT NULL,
  metadataPrefix varchar2(255) DEFAULT NULL,
  oai_set varchar2(50) DEFAULT NULL,
  datestamp varchar2(50) DEFAULT NULL,
  schedule NUMBER(10) NOT NULL,
  enabled NUMBER(3) DEFAULT 0 NOT NULL,
  lastHarvest TIMESTAMP(6) DEFAULT NULL,
  PRIMARY KEY (id)
);
