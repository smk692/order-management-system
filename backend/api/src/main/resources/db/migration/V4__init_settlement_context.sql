-- Settlement Bounded Context Migration
-- Create tables for settlements and settlement items

-- Settlements table
CREATE TABLE settlements (
    id BINARY(16) PRIMARY KEY,
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_settlement_company (company_id),
    INDEX idx_settlement_channel (channel_id),
    INDEX idx_settlement_period (period_year, period_month),
    INDEX idx_settlement_status (status),
    UNIQUE KEY uk_settlement_channel_period (channel_id, period_year, period_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Settlement Items table
CREATE TABLE settlement_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_id BINARY(16) NOT NULL,
    order_id VARCHAR(50) NOT NULL,
    order_amount DECIMAL(15, 2) NOT NULL,
    commission_rate DECIMAL(5, 4) NOT NULL,
    commission_amount DECIMAL(15, 2) NOT NULL,
    settlement_amount DECIMAL(15, 2) NOT NULL,
    INDEX idx_settlement_item_settlement (settlement_id),
    INDEX idx_settlement_item_order (order_id),
    CONSTRAINT fk_settlement_item_settlement FOREIGN KEY (settlement_id) REFERENCES settlements(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
