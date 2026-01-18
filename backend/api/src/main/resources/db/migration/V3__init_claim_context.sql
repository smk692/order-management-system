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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_claim_company (company_id),
    INDEX idx_claim_number (claim_number),
    INDEX idx_claim_order (order_id),
    INDEX idx_claim_type (type),
    INDEX idx_claim_status (status),
    INDEX idx_claim_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Claim Items table
CREATE TABLE claim_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    reason TEXT,
    INDEX idx_claim_item_claim (claim_id),
    INDEX idx_claim_item_product (product_id),
    CONSTRAINT fk_claim_item_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
