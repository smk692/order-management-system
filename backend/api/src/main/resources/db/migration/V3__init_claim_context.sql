-- Claim Bounded Context Migration
-- Create tables for claims and claim items

-- Claims table
CREATE TABLE claims (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    claim_number VARCHAR(20) NOT NULL UNIQUE,
    order_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason TEXT NOT NULL,
    memo TEXT,
    priority VARCHAR(20) NOT NULL,
    refund_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    refunded_at TIMESTAMP,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_claim_company ON claims(company_id);
CREATE INDEX idx_claim_number ON claims(claim_number);
CREATE INDEX idx_claim_order ON claims(order_id);
CREATE INDEX idx_claim_type ON claims(type);
CREATE INDEX idx_claim_status ON claims(status);
CREATE INDEX idx_claim_priority ON claims(priority);

-- Claim Items table
CREATE TABLE claim_items (
    id BIGSERIAL PRIMARY KEY,
    claim_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    reason TEXT,
    CONSTRAINT fk_claim_item_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE
);

CREATE INDEX idx_claim_item_claim ON claim_items(claim_id);
CREATE INDEX idx_claim_item_product ON claim_items(product_id);
