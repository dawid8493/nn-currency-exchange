CREATE TABLE IF NOT EXISTS owner
(
    id varchar(30) NOT NULL PRIMARY KEY,
    first_name varchar(50) NOT NULL,
    last_name varchar(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS currency_balance
(
    id varchar(30) NOT NULL PRIMARY KEY,
    currency varchar(3) NOT NULL,
    amount numeric NOT NULL,
    account_id varchar(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS account
(
    id varchar(30) NOT NULL PRIMARY KEY,
    owner_id varchar(30) NOT NULL
);

ALTER TABLE currency_balance ADD CONSTRAINT fk_currency_balance_to_account FOREIGN KEY (account_id) REFERENCES account;