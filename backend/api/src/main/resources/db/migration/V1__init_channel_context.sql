-- Channel Bounded Context Migration
-- Create tables for channels, warehouses, and their mappings

-- Channels table
CREATE TABLE channels (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    additional_config TEXT,
    api_endpoint VARCHAR(500),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_channel_company ON channels(company_id);
CREATE INDEX idx_channel_type ON channels(type);
CREATE INDEX idx_channel_status ON channels(status);

-- Warehouses table
CREATE TABLE warehouses (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    zip_code VARCHAR(10),
    address1 VARCHAR(200) NOT NULL,
    address2 VARCHAR(200),
    city VARCHAR(100),
    country VARCHAR(50) NOT NULL,
    region VARCHAR(100),
    capacity INT NOT NULL,
    current_stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_warehouse_company ON warehouses(company_id);
CREATE INDEX idx_warehouse_code ON warehouses(code);
CREATE INDEX idx_warehouse_type ON warehouses(type);
CREATE INDEX idx_warehouse_status ON warehouses(status);
CREATE INDEX idx_warehouse_region ON warehouses(region);

-- Channel-Warehouse Mappings table
CREATE TABLE channel_warehouse_mappings (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    channel_id VARCHAR(36) NOT NULL,
    warehouse_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_channel_warehouse UNIQUE (channel_id, warehouse_id),
    CONSTRAINT fk_mapping_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_mapping_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE
);

CREATE INDEX idx_mapping_company ON channel_warehouse_mappings(company_id);
CREATE INDEX idx_mapping_channel ON channel_warehouse_mappings(channel_id);
CREATE INDEX idx_mapping_warehouse ON channel_warehouse_mappings(warehouse_id);
CREATE INDEX idx_mapping_role ON channel_warehouse_mappings(role);
