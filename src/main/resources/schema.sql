-- Use the 'rest-api' database
\c rest-api;

-- Create schema if not exists
CREATE SCHEMA IF NOT EXISTS bank-cards-management;

-- Set the schema search path
SET search_path TO bank-cards-management;

-- Creating the 'users' table
CREATE TABLE "bank-cards-management".users (
	id uuid NOT NULL,
	full_name varchar NOT NULL,
	email varchar NOT NULL,
	"password" varchar NOT NULL,
	"role" varchar NOT NULL,
	CONSTRAINT email_unique UNIQUE (email),
	CONSTRAINT password_unique UNIQUE (password),
	CONSTRAINT users_pk PRIMARY KEY (id)
);


-- Creating the 'cards' table
CREATE TABLE "bank-cards-management".cards (
	id uuid NOT NULL,
	"number" varchar NOT NULL,
	owner_id uuid NOT NULL,
	expiration_date date NOT NULL,
	status varchar NOT NULL,
	balance numeric(10, 2) NOT NULL,
	transaction_limit_per_day numeric(10, 2) NULL,
	CONSTRAINT cards_number_unique UNIQUE (number),
	CONSTRAINT cards_pk PRIMARY KEY (id)
);
-- "bank-cards-management".cards foreign keys
ALTER TABLE "bank-cards-management".cards ADD CONSTRAINT cards_users_fk FOREIGN KEY (owner_id) REFERENCES "bank-cards-management".users(id);


-- Creating the 'transactions' table
CREATE TABLE "bank-cards-management".transactions (
	id uuid NOT NULL,
	card_id uuid NOT NULL,
	local_date_time timestamp NOT NULL,
	amount numeric(10, 2) NOT NULL,
	CONSTRAINT transactions_pk PRIMARY KEY (id)
);
-- "bank-cards-management".transactions foreign keys
ALTER TABLE "bank-cards-management".transactions ADD CONSTRAINT transactions_cards_fk FOREIGN KEY (card_id) REFERENCES "bank-cards-management".cards(id);





-- INSERT

INSERT INTO "bank-cards-management".users (id, full_name, email, "password", "role") VALUES
('a1d0c6e8-1f17-4f8c-9f36-0a8f84cdd1a1', 'Alice Johnson', 'alice.johnson@gmail.com', 'password123', 'ADMIN'),
('b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2', 'Bob Smith', 'bob.smith@gmail.com', 'password321', 'USER'),
('c3e2f8a0-3b39-4e1c-7c58-2c0fa6eff3c3', 'Clara Davis', 'clara.davis@gmail.com', 'password456', 'USER'),
('d4f3a9b1-4c4a-4f2d-6d69-3d1fb7ffe4d4', 'David Lee', 'david.lee@gmail.com', 'password654', 'USER');

INSERT INTO "bank-cards-management".cards (id, "number", owner_id, expiration_date, status, balance, transaction_limit_per_day) VALUES
('101aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa01', '1234-5678-9012-3456', 'a1d0c6e8-1f17-4f8c-9f36-0a8f84cdd1a1', '2028-04-30', 'активна', 2500.00, 1000.00),
('202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '2345-6789-0123-4567', 'b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2', '2027-12-31', 'активна', 120.50, 500.00),
('303ccc03-cccc-cccc-cccc-cccccccccc03', '3456-7890-1234-5678', 'b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2', '2026-08-15', 'активна', 7834.99, 2000.00),
('404ddd04-dddd-dddd-dddd-dddddddddd04', '4567-8901-2345-6789', 'c3e2f8a0-3b39-4e1c-7c58-2c0fa6eff3c3', '2029-01-01', 'заблокирована', 0.00, NULL),
('505eee05-eeee-eeee-eeee-eeeeeeeeee05', '5678-9012-3456-7890', 'd4f3a9b1-4c4a-4f2d-6d69-3d1fb7ffe4d4', '2025-01-01', 'истек срок действия', 134.75, 300.00);

INSERT INTO "bank-cards-management".transactions (id, card_id, local_date_time, amount) VALUES
('301aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa01', '101aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa01', '2025-04-13 14:30:00', 150.75),
('301aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa02', '202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '2025-04-12 10:15:00', 89.50),
('301aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa03', '303ccc03-cccc-cccc-cccc-cccccccccc03', '2025-04-11 18:45:00', 1020.00),
('301aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa04', '505eee05-eeee-eeee-eeee-eeeeeeeeee05', '2024-04-10 09:20:00', 300.00),
('301aaa01-aaaa-aaaa-aaaa-aaaaaaaaaa05', '404ddd04-dddd-dddd-dddd-dddddddddd04', '2025-04-09 20:00:00', 47.99);
