SET FOREIGN_KEY_CHECKS=0;
SET IGNORECASE TRUE;

-- Create tables

CREATE TABLE IF NOT EXISTS User (
id bigint PRIMARY KEY AUTO_INCREMENT,
name varchar(256),
email varchar(256) UNIQUE,
password varchar(256),
date_created timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Session (
session_id varchar(256) PRIMARY KEY,
user_id bigint,
FOREIGN KEY (user_id) REFERENCES User (id)
);

CREATE TABLE IF NOT EXISTS Product (
id bigint PRIMARY KEY AUTO_INCREMENT,
name varchar(256),
description clob,
price_cents int,
qty int DEFAULT 0,
date_created timestamp DEFAULT CURRENT_TIMESTAMP,
date_updated timestamp AS CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Vendor (
id bigint PRIMARY KEY AUTO_INCREMENT,
name varchar(256),
token varchar(256)
);

CREATE TABLE IF NOT EXISTS Transaction (
id bigint PRIMARY KEY AUTO_INCREMENT,
product_id bigint,
vendor_id bigint,
FOREIGN KEY (product_id) REFERENCES Product (id),
FOREIGN KEY (vendor_id) REFERENCES Vendor (id)
);

-- Insert seed data

MERGE INTO User (id, name, email, password) VALUES (1, 'Admin', 'admin@email.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'); -- password: 'admin123'
MERGE INTO Vendor (id, name, token) VALUES (1, 'PismoStore', '2289edc82ff62d5d8a82ad2ef7079871aef71713');
MERGE INTO Product (id, name, description, price_cents, qty) VALUES (1, 'Chapéu de cowboy', 'Sucesso em Barretos!', 10000, 10);
MERGE INTO Product (id, name, description, price_cents, qty) VALUES (2, 'Máscara de cavalo', 'Pra ficar bem na foto, quando o carro do Google passar pela sua rua!', 12000, 5);

SET FOREIGN_KEY_CHECKS=1;