CREATE TABLE customer
(
    customerID UUID PRIMARY KEY,
    firstName VARCHAR(50)  NOT NULL,
    lastName  VARCHAR(50)  NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    state     CHAR(2)
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
    total     NUMERIC(19, 4) NOT NULL,
    timestamp TIMESTAMP      NOT NULL,
    FOREIGN KEY (customerID) REFERENCES customer (customerID)
);

-- Create index on customerID for faster joins and lookups
CREATE INDEX idx_orderhistory_customerid ON orderHistory (customerID);

-- Create index on timestamp for time-based queries
CREATE INDEX idx_orderhistory_timestamp ON orderHistory (timestamp);

-- Create the stateReport table
CREATE OR REPLACE VIEW statereport AS
SELECT
    c.state,
    SUM(oh.total) as amount,
    COUNT(oh.orderid) as count
FROM orderHistory oh
         JOIN customer c ON oh.customerID = c.customerID
GROUP BY c.state;

CREATE TABLE customer_outbox
(
    id UUID PRIMARY KEY,
    aggregatetype VARCHAR(255) NOT NULL,
    aggregateid   VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NOT NULL,
    payload BYTEA
);
