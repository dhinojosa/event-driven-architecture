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

CREATE TABLE order_outbox
(
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    timestamp      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (aggregate_id)
        REFERENCES "order" (orderID)
);

CREATE INDEX idx_order_outbox_ts ON order_outbox (timestamp);
CREATE INDEX idx_order_outbox_aggregate_id ON order_outbox (aggregate_id);
