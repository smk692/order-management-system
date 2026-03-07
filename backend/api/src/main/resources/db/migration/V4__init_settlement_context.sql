-- Settlement Bounded Context Migration
-- Create tables for settlements and settlement items

-- Settlements table
CREATE TABLE settlements (
    id BYTEA PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    channel_id VARCHAR(50) NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_sales DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_commission DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    net_settlement DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    confirmed_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_settlement_channel_period UNIQUE (channel_id, period_year, period_month)
);

CREATE INDEX idx_settlement_company ON settlements(company_id);
CREATE INDEX idx_settlement_channel ON settlements(channel_id);
CREATE INDEX idx_settlement_period ON settlements(period_year, period_month);
CREATE INDEX idx_settlement_status ON settlements(status);

-- Settlement Items table
CREATE TABLE settlement_items (
    id BIGSERIAL PRIMARY KEY,
    settlement_id BYTEA NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    order_amount DECIMAL(15, 2) NOT NULL,
    commission_rate DECIMAL(5, 4) NOT NULL,
    commission_amount DECIMAL(15, 2) NOT NULL,
    settlement_amount DECIMAL(15, 2) NOT NULL,
    CONSTRAINT fk_settlement_item_settlement FOREIGN KEY (settlement_id) REFERENCES settlements(id) ON DELETE CASCADE
);

CREATE INDEX idx_settlement_item_settlement ON settlement_items(settlement_id);
CREATE INDEX idx_settlement_item_order ON settlement_items(order_id);
