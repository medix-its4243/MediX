CREATE ROLE medical LOGIN PASSWORD 'medical';
CREATE DATABASE auth_db OWNER medical;
CREATE DATABASE patient_db OWNER medical;
CREATE DATABASE doctor_db OWNER medical;
CREATE DATABASE appointment_db OWNER medical;
CREATE DATABASE record_db OWNER medical;
CREATE DATABASE billing_db OWNER medical;
