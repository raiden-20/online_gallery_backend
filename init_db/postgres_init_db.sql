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

CREATE TABLE art(
     id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
     name VARCHAR(200),
     type VARCHAR(15),
     price BIGINT,
     artist_id UUID REFERENCES artist(id),
     owner_id UUID REFERENCES customer(id),
     sold BOOLEAN,
     description VARCHAR(500),
     size VARCHAR(20),
     create_date TIMESTAMP,
     tags VARCHAR(30) ARRAY[22],
     materials VARCHAR(20) ARRAY[30],
     frame BOOLEAN,
     publish_date TIMESTAMP,
     views INT
);

CREATE TABLE art_photo(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    art_id INT REFERENCES art(id),
    photo_url VARCHAR(500),
    default_photo BOOLEAN
);

CREATE TABLE public_subscription(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    artist_id UUID REFERENCES artist(id),
    customer_id UUID REFERENCES customer(id)
);

CREATE TABLE private_subscription(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    artist_id UUID REFERENCES artist(id),
    price INT,
    create_date TIMESTAMP
);


CREATE TABLE art_private_subscription(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    art_id INT REFERENCES art(id),
    subscription_id INT REFERENCES private_subscription(id)
);

CREATE TABLE post(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    artist_id UUID REFERENCES artist(id),
    title VARCHAR(200),
    body VARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE post_photo(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    post_id INT REFERENCES post(id),
    photo_url VARCHAR(500),
    default_photo BOOLEAN
);

CREATE TABLE cart(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    customer_id UUID REFERENCES customer(id),
    art_id INT REFERENCES art(id)
);

CREATE TABLE card(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    customer_id UUID REFERENCES customer(id),
    type VARCHAR(20),
    number VARCHAR(20),
    date TIMESTAMP,
    cvv INT,
    is_default BOOLEAN
);

CREATE TABLE customer_private_subscription(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    private_subscription_id INT REFERENCES private_subscription(id),
    customer_id UUID REFERENCES customer(id),
    card_id INT REFERENCES card(id),
    payment_date TIMESTAMP,
    create_date TIMESTAMP
);

CREATE TABLE address(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    customer_id UUID REFERENCES customer(id),
    name VARCHAR(200),
    country VARCHAR(200),
    region VARCHAR(200),
    city VARCHAR(200),
    location VARCHAR(200),
    index INT,
    is_default BOOLEAN
);

CREATE TABLE order_(
    id INT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY NOT NULL,
    customer_id UUID REFERENCES customer(id),
    artist_id UUID REFERENCES artist(id),
    art_id INT REFERENCES art(id),
    status VARCHAR(10),
    artist_comment VARCHAR(300),
    card_id INT REFERENCES card(id),
    address_id INT REFERENCES address(id)
);

INSERT INTO customer (id, customer_name, gender, birth_date, avatar_url, description)
VALUES (
    '00000000-0000-0000-0000-000000000000', 'anonymous', 'MAN', '2024-04-28', ' ', 'anonymous'
);

CREATE USER keycloak_pg_user_secret WITH PASSWORD 'keycloak_pg_pass_secret';

CREATE DATABASE keycloak;

\c keycloak;