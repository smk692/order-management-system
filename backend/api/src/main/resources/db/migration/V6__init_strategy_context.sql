-- Strategy Bounded Context Migration
-- Create tables for operations strategies, global readiness, and strategy deployments

-- Operations Strategies table
CREATE TABLE operations_strategies (
    id CHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    -- SimulationWeights (embedded)
    cost_reduction INT NOT NULL,
    lead_time INT NOT NULL,
    stock_balance INT NOT NULL,
    carbon_emission INT NOT NULL,
    -- SimulationResult (embedded, nullable)
    efficiency_score INT,
    cost_saving DECIMAL(15, 2),
    avg_lead_time INT,
    recommendation TEXT,
    calculated_at TIMESTAMP,
    -- Status
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_strategy_company ON operations_strategies(company_id);
CREATE INDEX idx_strategy_status ON operations_strategies(status);
CREATE INDEX idx_strategy_name ON operations_strategies(name);

-- Operations Strategy Countries (ElementCollection)
CREATE TABLE operations_strategy_countries (
    strategy_id CHAR(36) NOT NULL,
    country VARCHAR(3) NOT NULL,
    PRIMARY KEY (strategy_id, country),
    CONSTRAINT fk_strategy_countries_strategy FOREIGN KEY (strategy_id) REFERENCES operations_strategies(id) ON DELETE CASCADE
);

-- Global Readiness table
CREATE TABLE global_readiness (
    id CHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    country VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    launched_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_readiness_company_country UNIQUE (company_id, country)
);

CREATE INDEX idx_readiness_company ON global_readiness(company_id);
CREATE INDEX idx_readiness_country ON global_readiness(country);
CREATE INDEX idx_readiness_status ON global_readiness(status);

-- Global Readiness Checklist (ElementCollection)
CREATE TABLE global_readiness_checklist (
    readiness_id CHAR(36) NOT NULL,
    item_id VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (readiness_id, item_id),
    CONSTRAINT fk_checklist_readiness FOREIGN KEY (readiness_id) REFERENCES global_readiness(id) ON DELETE CASCADE
);

CREATE INDEX idx_checklist_category ON global_readiness_checklist(category);

-- Strategy Deployments table
CREATE TABLE strategy_deployments (
    id CHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    strategy_id CHAR(36) NOT NULL,
    country VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    deployed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rollback_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_deployment_strategy FOREIGN KEY (strategy_id) REFERENCES operations_strategies(id) ON DELETE CASCADE
);

CREATE INDEX idx_deployment_company ON strategy_deployments(company_id);
CREATE INDEX idx_deployment_strategy ON strategy_deployments(strategy_id);
CREATE INDEX idx_deployment_country ON strategy_deployments(country);
CREATE INDEX idx_deployment_status ON strategy_deployments(status);
