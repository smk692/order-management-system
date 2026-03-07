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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_rule_company ON automation_rules(company_id);
CREATE INDEX idx_rule_enabled ON automation_rules(enabled);
CREATE INDEX idx_rule_priority ON automation_rules(priority);
CREATE INDEX idx_rule_trigger_type ON automation_rules(type);

-- Automation Rule Conditions table
CREATE TABLE automation_rule_conditions (
    rule_id VARCHAR(36) NOT NULL,
    field VARCHAR(100) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    value VARCHAR(500) NOT NULL,
    CONSTRAINT fk_condition_rule FOREIGN KEY (rule_id) REFERENCES automation_rules(id) ON DELETE CASCADE
);

CREATE INDEX idx_condition_rule ON automation_rule_conditions(rule_id);

-- Automation Rule Actions table
CREATE TABLE automation_rule_actions (
    rule_id VARCHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    config TEXT NOT NULL,
    CONSTRAINT fk_action_rule FOREIGN KEY (rule_id) REFERENCES automation_rules(id) ON DELETE CASCADE
);

CREATE INDEX idx_action_rule ON automation_rule_actions(rule_id);
