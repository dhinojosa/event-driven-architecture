CREATE TABLE product
(
    productID UUID PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

-- Create index on commonly queried fields
CREATE INDEX idx_product_name ON product (name);


CREATE TABLE product_outbox (
                                id UUID PRIMARY KEY,
                                aggregatetype VARCHAR(255) NOT NULL,
                                aggregateid VARCHAR(255) NOT NULL,
                                type VARCHAR(255) NOT NULL,
                                payload BYTEA
);
