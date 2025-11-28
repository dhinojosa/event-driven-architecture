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


CREATE TABLE product_outbox
(
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    timestamp      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (aggregate_id)
        REFERENCES product (productID)
);

CREATE INDEX idx_product_outbox_ts ON product_outbox (timestamp);
CREATE INDEX idx_product_outbox_aggregate_id ON product_outbox (aggregate_id);
