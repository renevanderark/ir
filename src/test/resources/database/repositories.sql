CREATE TABLE repositories (
  id number(10),
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

CREATE SEQUENCE repositories_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER repositories_seq_tr
BEFORE INSERT ON repositories FOR EACH ROW
WHEN (NEW.id IS NULL)
  SELECT repositories_seq.NEXTVAL INTO :NEW.id FROM DUAL;

