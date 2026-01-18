-- Inventory Bounded Context Migration
-- Create tables for stocks and stock movements

-- Stocks table
CREATE TABLE stocks (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(100) NOT NULL,
    warehouse_id VARCHAR(100) NOT NULL,
    total INT NOT NULL DEFAULT 0,
    available INT NOT NULL DEFAULT 0,
    reserved INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_stock_company (company_id),
    INDEX idx_stock_product_warehouse (product_id, warehouse_id),
    INDEX idx_stock_status (status),
    UNIQUE KEY uk_stock_product_warehouse (product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Stock channel allocations table
CREATE TABLE stock_channel_allocations (
    stock_id VARCHAR(36) NOT NULL,
    channel_id VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (stock_id, channel_id),
    CONSTRAINT fk_allocation_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Stock movements table (audit trail)
CREATE TABLE stock_movements (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    stock_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    before_total INT NOT NULL,
    after_total INT NOT NULL,
    reference_id VARCHAR(100),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_movement_company (company_id),
    INDEX idx_movement_stock (stock_id),
    INDEX idx_movement_reference (reference_id),
    INDEX idx_movement_type (type),
    CONSTRAINT fk_movement_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
