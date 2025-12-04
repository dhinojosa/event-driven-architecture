-- Create tables for order system

-- Customer table
CREATE TABLE customer
(
    customerID UUID PRIMARY KEY,
    fullName VARCHAR(100) NOT NULL
);

-- Product table
CREATE TABLE product
(
    productID UUID PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description TEXT,
    stock       INTEGER        NOT NULL DEFAULT 0,
    price       DECIMAL(10, 2) NOT NULL
);

-- Order table
CREATE TABLE "order"
(
    orderID UUID PRIMARY KEY,
    customerID UUID NOT NULL,
    FOREIGN KEY (customerID) REFERENCES Customer (customerID)
);

-- OrderItem table
CREATE TABLE orderItem
(
    id       SERIAL PRIMARY KEY,
    orderID UUID NOT NULL,
    productID UUID NOT NULL,
    quantity INTEGER        NOT NULL,
    price    DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (orderID) REFERENCES "order" (orderID),
    FOREIGN KEY (productID) REFERENCES product (productID)
);

CREATE TABLE order_outbox (
                                id UUID PRIMARY KEY,
                                aggregatetype VARCHAR(255) NOT NULL,
                                aggregateid VARCHAR(255) NOT NULL,
                                type VARCHAR(255) NOT NULL,
                                payload BYTEA
);

CREATE INDEX idx_order_outbox_aggregate_id ON order_outbox (aggregateid);
