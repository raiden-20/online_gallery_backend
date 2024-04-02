-- \set kc_user `echo "$KC_DB_USERNAME"`
-- \set kc_pass `echo "KC_DB_PASSWORD"`

-- CREATE USER current_setting('kc_user') WITH PASSWORD current_setting('kc_pass');
CREATE USER keycloak_pg_user_secret WITH PASSWORD 'keycloak_pg_pass_secret';

CREATE DATABASE keycloak;

\c keycloak;