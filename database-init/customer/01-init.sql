CREATE TABLE customer
(
    customerID UUID PRIMARY KEY,
    firstName VARCHAR(50)  NOT NULL,
    lastName  VARCHAR(50)  NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    state     CHAR(2),
    numOrders INTEGER DEFAULT 0
);

-- Create an index on email for faster lookups
CREATE INDEX idx_customer_email ON customer (email);

-- Create an index on lastName for potential name-based searches
CREATE INDEX idx_customer_lastname ON customer (lastName);


-- Create the orderHistory table
CREATE TABLE orderHistory
(
    orderID UUID PRIMARY KEY,
    customerID UUID NOT NULL,
    total NUMERIC(19, 4) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (customerID) REFERENCES customer (customerID)
);

-- Create index on customerID for faster joins and lookups
CREATE INDEX idx_orderhistory_customerid ON orderHistory (customerID);

-- Create index on timestamp for time-based queries
CREATE INDEX idx_orderhistory_timestamp ON orderHistory (timestamp);

-- Create the stateReport table
CREATE TABLE stateReport
(
    state CHAR(2) PRIMARY KEY,
    amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    count INTEGER NOT NULL DEFAULT 0
);

-- Create index on amount for aggregate queries
CREATE INDEX idx_statereport_amount ON stateReport (amount);


-- Create customer creation outbox table for CDC
CREATE TABLE customer_outbox
(
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id   UUID NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    payload        JSONB NOT NULL,
    timestamp      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (aggregate_id)
        REFERENCES customer (customerID)
);

CREATE INDEX idx_customer_outbox_ts ON customer_outbox (timestamp);
CREATE INDEX idx_customer_outbox_aggregate_id ON customer_outbox (aggregate_id);
