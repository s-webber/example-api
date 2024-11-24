CREATE OR REPLACE FUNCTION tf_patient()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_date_time := CURRENT_TIMESTAMP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_patient
BEFORE UPDATE ON patient
FOR EACH ROW
EXECUTE FUNCTION tf_patient();

CREATE OR REPLACE FUNCTION tf_audit_patient()
  RETURNS TRIGGER AS
  $BODY$
  BEGIN
    IF (TG_OP = 'DELETE') THEN
      INSERT INTO audit_patient(patient_id, nhs_number, name, date_of_birth, address, postcode, telephone_number, email_address, updated_by, created_date_time, updated_date_time, change_mode, change_db_date_time)
          VALUES (OLD.patient_id, OLD.nhs_number, OLD.name, OLD.date_of_birth, OLD.address, OLD.postcode, OLD.telephone_number, OLD.email_address, OLD.updated_by, OLD.created_date_time, OLD.updated_date_time, 'D', transaction_timestamp());
    ELSIF (TG_OP = 'UPDATE') THEN
      INSERT INTO audit_patient(patient_id, nhs_number, name, date_of_birth, address, postcode, telephone_number, email_address, updated_by, created_date_time, updated_date_time, change_mode, change_db_date_time)
          VALUES (NEW.patient_id, NEW.nhs_number, NEW.name, NEW.date_of_birth, NEW.address, NEW.postcode, NEW.telephone_number, NEW.email_address, NEW.updated_by, NEW.created_date_time, NEW.updated_date_time, 'U', transaction_timestamp());
    ELSIF (TG_OP = 'INSERT') THEN
      INSERT INTO audit_patient(patient_id, nhs_number, name, date_of_birth, address, postcode, telephone_number, email_address, updated_by, created_date_time, updated_date_time, change_mode, change_db_date_time)
          VALUES (NEW.patient_id, NEW.nhs_number, NEW.name, NEW.date_of_birth, NEW.address, NEW.postcode, NEW.telephone_number, NEW.email_address, NEW.updated_by, NEW.created_date_time, NEW.updated_date_time, 'I', transaction_timestamp());
    END IF;

    RETURN NULL; -- result is ignored since this is an AFTER trigger
  END;
  $BODY$
LANGUAGE plpgsql VOLATILE
COST 100;

CREATE TRIGGER trg_audit_patient
AFTER INSERT OR UPDATE OR DELETE ON patient
FOR EACH ROW
EXECUTE FUNCTION tf_audit_patient();
