-- \set kc_user `echo "$KC_DB_USERNAME"`
-- \set kc_pass `echo "KC_DB_PASSWORD"`

-- CREATE USER current_setting('kc_user') WITH PASSWORD current_setting('kc_pass');

CREATE DATABASE main_db;

\c main_db;

CREATE TABLE artist(
    id UUID PRIMARY KEY UNIQUE NOT NULL,
    artist_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    cover_url VARCHAR(500),
    description VARCHAR(500),
    views INT
);

CREATE TABLE customer(
    id UUID PRIMARY KEY UNIQUE NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_date TIMESTAMP NOT NULL,
    avatar_url VARCHAR(500),
    cover_url VARCHAR(500),
    description VARCHAR(200),
    artist_id UUID REFERENCES artist(id)
);

CREATE USER keycloak_pg_user_secret WITH PASSWORD 'keycloak_pg_pass_secret';

CREATE DATABASE keycloak;

\c keycloak;