CREATE TABLE patient (
  patient_id          SERIAL PRIMARY KEY,
  nhs_number          VARCHAR(10) NOT NULL,
  name                VARCHAR(100) NOT NULL,
  date_of_birth       DATE NOT NULL,
  address             VARCHAR(100),
  postcode            VARCHAR(8),
  telephone_number    VARCHAR(20),
  email_address       VARCHAR(320),
  created_date_time   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date_time   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by          INT NOT NULL
);

ALTER TABLE patient ADD CONSTRAINT patient_nhs_number_uq UNIQUE (nhs_number);

CREATE TABLE audit_patient (
  audit_patient_id    SERIAL PRIMARY KEY,
  patient_id          INT NOT NULL,
  nhs_number          VARCHAR(10) NOT NULL,
  name                VARCHAR(100) NOT NULL,
  date_of_birth       DATE NOT NULL,
  address             VARCHAR(100),
  postcode            VARCHAR(8),
  telephone_number    VARCHAR(20),
  email_address       VARCHAR(320),
  created_date_time   TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_date_time   TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_by          INT NOT NULL,
  change_mode         CHAR(1) NOT NULL,
  change_db_date_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE client (
  client_id           SERIAL PRIMARY KEY,
  name                VARCHAR(100) NOT NULL
);

ALTER TABLE client ADD CONSTRAINT client_name_uq UNIQUE (name);

CREATE TABLE certificate (
  certificate_id      SERIAL PRIMARY KEY,
  client_id           INT NOT NULL,
  thumbprint          CHAR(64) NOT NULL,
  active              BOOLEAN NOT NULL
);

ALTER TABLE certificate
ADD CONSTRAINT fk_client
FOREIGN KEY (client_id) REFERENCES client(client_id);
