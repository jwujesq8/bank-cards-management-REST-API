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
	expiration_date timestamp(6) NOT NULL,
	status varchar(20) NOT NULL,
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
	source_card_id uuid NOT NULL,
	destination_card_id uuid NOT NULL,
	local_date_time timestamp NOT NULL,
	amount numeric(10, 2) NOT NULL,
	CONSTRAINT transactions_pk PRIMARY KEY (id),
	CONSTRAINT transactions_sorce_cards_fk FOREIGN KEY (source_card_id) REFERENCES "bank-cards-management".cards(id),
	CONSTRAINT transactions_destination_cards_fk FOREIGN KEY (destination_card_id) REFERENCES "bank-cards-management".cards(id)
);



-- INSERT

INSERT INTO "bank-cards-management".users (id, full_name, email, "password", "role") VALUES
('a1d0c6e8-1f17-4f8c-9f36-0a8f84cdd1a1', 'Alice Johnson', 'alice.johnson@gmail.com', 'password123', 'ADMIN'),
('b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2', 'Bob Smith', 'bob.smith@gmail.com', 'password321', 'USER'),
('c3e2f8a0-3b39-4e1c-7c58-2c0fa6eff3c3', 'Clara Davis', 'clara.davis@gmail.com', 'password456', 'USER'),
('d4f3a9b1-4c4a-4f2d-6d69-3d1fb7ffe4d4', 'David Lee', 'david.lee@gmail.com', 'password654', 'USER');

INSERT INTO "bank-cards-management".cards (id, "number", owner_id, expiration_date, status, balance, transaction_limit_per_day) VALUES
('fd07bc88-92c7-43c1-9012-36290897cb1f', 'Bi46GCPYSXfufJtTx3yOvJv5lsmFRfa//KhrV+t2L0I=', 'a1d0c6e8-1f17-4f8c-9f36-0a8f84cdd1a1', '2029-04-30', 'active', 500.00, 1000.00),
('202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'EDaIHHvy1LNvgtuLbOWHzeEMzausKjrdd9+OgsyA6jI=', 'b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2', '2027-12-31', 'active', 120.50, 500.00),
('303ccc03-cccc-cccc-cccc-cccccccccc03', '0wvjx0AcggS94kd10eHgL2E0XZzul08spK1VjJLF0O4=', 'b2c1d7f9-2a28-4d0b-8a47-1b9f95dee2b2', '2026-08-15', 'active', 7834.99, 2000.00),
('404ddd04-dddd-dddd-dddd-dddddddddd04', 'qau0ngS5Y8iQtKzhD0eRoMq03z9E0nkQab2yVqSkfyk=', 'c3e2f8a0-3b39-4e1c-7c58-2c0fa6eff3c3', '2029-01-01', 'blocked', 0.00, NULL),
('505eee05-eeee-eeee-eeee-eeeeeeeeee05', 'bZV0UQS2RMzWWdI6D6WtpIwj0m2+HmfTfzkwwtAxsNg=', 'd4f3a9b1-4c4a-4f2d-6d69-3d1fb7ffe4d4', '2025-01-01', 'expired', 134.75, 300.00);

INSERT INTO "bank-cards-management".transactions (id, source_card_id, destination_card_id, local_date_time, amount) VALUES
('11111111-1111-1111-1111-111111111111', '202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '303ccc03-cccc-cccc-cccc-cccccccccc03', '2024-04-30 00:00:00.000', 1000.00),
('22222222-2222-2222-2222-222222222222', '202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '303ccc03-cccc-cccc-cccc-cccccccccc03', '2023-12-31 00:00:00.000', 500.00),
('33333333-3333-3333-3333-333333333333', '303ccc03-cccc-cccc-cccc-cccccccccc03', '202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '2024-08-15 00:00:00.000', 100.00),
('44444444-4444-4444-4444-444444444444', '202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '303ccc03-cccc-cccc-cccc-cccccccccc03', '2025-01-01 00:00:00.000', 500.00),
('55555555-5555-5555-5555-555555555555', '303ccc03-cccc-cccc-cccc-cccccccccc03', '202bbb02-bbbb-bbbb-bbbb-bbbbbbbbbbb2', '2025-01-01 00:00:00.000', 5.00);

