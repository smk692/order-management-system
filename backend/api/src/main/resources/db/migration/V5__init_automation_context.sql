-- Automation Bounded Context Migration
-- Create tables for automation rules, conditions, and actions

-- Automation Rules table
CREATE TABLE automation_rules (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    config TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 0,
    execution_count BIGINT NOT NULL DEFAULT 0,
    last_executed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_rule_company (company_id),
    INDEX idx_rule_enabled (enabled),
    INDEX idx_rule_priority (priority),
    INDEX idx_rule_trigger_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Automation Rule Conditions table
CREATE TABLE automation_rule_conditions (
    rule_id VARCHAR(36) NOT NULL,
    field VARCHAR(100) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    value VARCHAR(500) NOT NULL,
    INDEX idx_condition_rule (rule_id),
    CONSTRAINT fk_condition_rule FOREIGN KEY (rule_id) REFERENCES automation_rules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Automation Rule Actions table
CREATE TABLE automation_rule_actions (
    rule_id VARCHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    config TEXT NOT NULL,
    INDEX idx_action_rule (rule_id),
    CONSTRAINT fk_action_rule FOREIGN KEY (rule_id) REFERENCES automation_rules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
