# ERD 설계

**버전**: 1.0
**작성일**: 2025-01-18

---

## 1. 데이터베이스 분리 전략

| 데이터베이스 | 용도 | 도메인 |
|-------------|------|--------|
| **MySQL** | 핵심 트랜잭션 데이터 | Identity, Catalog, Channel, Order, Inventory, Claim, Settlement, Automation |
| **MongoDB** | 로그/이벤트/비정형 데이터 | Integration (Webhook, API Log, Domain Event) |

---

## 2. MySQL 스키마 설계

### 2.1 ERD 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    IDENTITY CONTEXT                                      │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐          ┌──────────────────┐                                     │
│  │     company      │          │      user        │                                     │
│  ├──────────────────┤          ├──────────────────┤                                     │
│  │ PK id            │◄─────────│ FK company_id    │                                     │
│  │    name          │    1:N   │ PK id            │                                     │
│  │    business_no   │          │    email         │                                     │
│  │    status        │          │    name          │                                     │
│  │    created_at    │          │    role          │                                     │
│  └──────────────────┘          │    status        │                                     │
│                                │    created_at    │                                     │
│                                └──────────────────┘                                     │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    CATALOG CONTEXT                                       │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐    1:N   ┌──────────────────┐                                     │
│  │     product      │◄─────────│  product_barcode │                                     │
│  ├──────────────────┤          ├──────────────────┤                                     │
│  │ PK id            │          │ PK id            │                                     │
│  │ FK company_id    │          │ FK product_id    │                                     │
│  │    sku           │          │    code          │                                     │
│  │    name_ko       │          │    is_main       │                                     │
│  │    name_en       │          └──────────────────┘                                     │
│  │    brand         │                                                                   │
│  │    category_path │    1:N   ┌──────────────────────┐                                 │
│  │    uom           │◄─────────│ product_customs_strategy│                              │
│  │    base_price    │          ├──────────────────────┤                                 │
│  │    status        │          │ PK id                │                                 │
│  │    -- dimensions │          │ FK product_id        │                                 │
│  │    -- logistics  │          │    country_code      │                                 │
│  │    created_at    │          │    local_hs_code     │                                 │
│  └──────────────────┘          │    invoice_name      │                                 │
│                                │    duty_rate         │                                 │
│                                │    required_docs     │                                 │
│                                └──────────────────────┘                                 │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    CHANNEL CONTEXT                                       │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐                      ┌──────────────────┐                         │
│  │     channel      │                      │    warehouse     │                         │
│  ├──────────────────┤                      ├──────────────────┤                         │
│  │ PK id            │                      │ PK id            │                         │
│  │ FK company_id    │                      │ FK company_id    │                         │
│  │    name          │          N:M         │    name          │                         │
│  │    type          │◄────────────────────►│    region        │                         │
│  │    status        │                      │    type          │                         │
│  │    last_sync_at  │                      │    capacity      │                         │
│  │    credentials   │    ┌─────────────────────────┐          │                         │
│  │    created_at    │    │channel_warehouse_mapping│◄─────────│    status        │      │
│  └──────────────────┘    ├─────────────────────────┤          │    address_*     │      │
│          │               │ PK id                   │          │    created_at    │      │
│          └──────────────►│ FK channel_id           │          └──────────────────┘      │
│                          │ FK warehouse_id         │                                    │
│                          │    role                 │                                    │
│                          │    priority             │                                    │
│                          │    is_active            │                                    │
│                          │    load_balancing       │                                    │
│                          │    created_at           │                                    │
│                          └─────────────────────────┘                                    │
│                                                                                          │
│  ┌─────────────────────────┐                                                            │
│  │product_channel_mapping  │   (상품-채널 매핑)                                          │
│  ├─────────────────────────┤                                                            │
│  │ PK id                   │                                                            │
│  │ FK product_id           │                                                            │
│  │ FK channel_id           │                                                            │
│  │    is_active            │                                                            │
│  │    created_at           │                                                            │
│  └─────────────────────────┘                                                            │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                     ORDER CONTEXT                                        │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐    1:N   ┌──────────────────┐                                     │
│  │      order       │◄─────────│    order_item    │                                     │
│  ├──────────────────┤          ├──────────────────┤                                     │
│  │ PK id            │          │ PK id            │                                     │
│  │ FK company_id    │          │ FK order_id      │                                     │
│  │ FK channel_id    │          │ FK product_id    │                                     │
│  │    external_id   │          │    product_name  │                                     │
│  │    status        │          │    sku           │                                     │
│  │    order_date    │          │    quantity      │                                     │
│  │    -- customer   │          │    unit_price    │                                     │
│  │    -- address    │          │    total_price   │                                     │
│  │    fulfillment   │          └──────────────────┘                                     │
│  │ FK warehouse_id  │                                                                   │
│  │    routing_logic │    1:1   ┌──────────────────┐                                     │
│  │    total_amount  │◄─────────│     shipping     │                                     │
│  │    created_at    │          ├──────────────────┤                                     │
│  └──────────────────┘          │ PK id            │                                     │
│                                │ FK order_id      │    1:N   ┌──────────────────┐       │
│                                │    carrier       │◄─────────│  tracking_event  │       │
│                                │    tracking_no   │          ├──────────────────┤       │
│                                │    status        │          │ PK id            │       │
│                                │    shipped_at    │          │ FK shipping_id   │       │
│                                │    delivered_at  │          │    status        │       │
│                                └──────────────────┘          │    location      │       │
│                                                              │    description   │       │
│                                                              │    timestamp     │       │
│                                                              └──────────────────┘       │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   INVENTORY CONTEXT                                      │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐    1:N   ┌──────────────────────────┐                             │
│  │      stock       │◄─────────│ stock_channel_allocation │                             │
│  ├──────────────────┤          ├──────────────────────────┤                             │
│  │ PK id            │          │ PK id                    │                             │
│  │ FK company_id    │          │ FK stock_id              │                             │
│  │ FK product_id    │          │ FK channel_id            │                             │
│  │ FK warehouse_id  │          │    quantity              │                             │
│  │    total         │          └──────────────────────────┘                             │
│  │    available     │                                                                   │
│  │    reserved      │    1:N   ┌──────────────────┐                                     │
│  │    safety_stock  │◄─────────│  stock_movement  │                                     │
│  │    status        │          ├──────────────────┤                                     │
│  │    created_at    │          │ PK id            │                                     │
│  │    updated_at    │          │ FK stock_id      │                                     │
│  └──────────────────┘          │    type          │                                     │
│                                │    quantity      │                                     │
│                                │    before_qty    │                                     │
│                                │    after_qty     │                                     │
│                                │    reason        │                                     │
│                                │    reference_id  │                                     │
│                                │    created_at    │                                     │
│                                │ FK created_by    │                                     │
│                                └──────────────────┘                                     │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                     CLAIM CONTEXT                                        │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐    1:N   ┌──────────────────┐                                     │
│  │      claim       │◄─────────│    claim_item    │                                     │
│  ├──────────────────┤          ├──────────────────┤                                     │
│  │ PK id            │          │ PK id            │                                     │
│  │ FK company_id    │          │ FK claim_id      │                                     │
│  │ FK order_id      │          │ FK product_id    │                                     │
│  │    type          │          │    quantity      │                                     │
│  │    status        │          │    reason        │                                     │
│  │    reason        │          └──────────────────┘                                     │
│  │    memo          │                                                                   │
│  │    priority      │                                                                   │
│  │    refund_amount │                                                                   │
│  │    refunded_at   │                                                                   │
│  │    created_at    │                                                                   │
│  │    processed_at  │                                                                   │
│  └──────────────────┘                                                                   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   SETTLEMENT CONTEXT                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────┐    1:N   ┌──────────────────┐                                     │
│  │    settlement    │◄─────────│  settlement_item │                                     │
│  ├──────────────────┤          ├──────────────────┤                                     │
│  │ PK id            │          │ PK id            │                                     │
│  │ FK company_id    │          │ FK settlement_id │                                     │
│  │ FK channel_id    │          │ FK order_id      │                                     │
│  │    period_year   │          │    sales_amount  │                                     │
│  │    period_month  │          │    commission    │                                     │
│  │    total_sales   │          └──────────────────┘                                     │
│  │    total_comm    │                                                                   │
│  │    net_settle    │                                                                   │
│  │    status        │                                                                   │
│  │    created_at    │                                                                   │
│  │    confirmed_at  │                                                                   │
│  └──────────────────┘                                                                   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   AUTOMATION CONTEXT                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────────┐    1:N   ┌──────────────────┐                                 │
│  │   automation_rule    │◄─────────│  rule_condition  │                                 │
│  ├──────────────────────┤          ├──────────────────┤                                 │
│  │ PK id                │          │ PK id            │                                 │
│  │ FK company_id        │          │ FK rule_id       │                                 │
│  │    name              │          │    field         │                                 │
│  │    description       │          │    operator      │                                 │
│  │    trigger_type      │          │    value         │                                 │
│  │    trigger_config    │          │    order_num     │                                 │
│  │    is_active         │          └──────────────────┘                                 │
│  │    last_executed_at  │                                                               │
│  │    execution_count   │    1:N   ┌──────────────────┐                                 │
│  │    created_at        │◄─────────│   rule_action    │                                 │
│  └──────────────────────┘          ├──────────────────┤                                 │
│                                    │ PK id            │                                 │
│                                    │ FK rule_id       │                                 │
│                                    │    action_type   │                                 │
│                                    │    action_config │                                 │
│                                    │    order_num     │                                 │
│                                    └──────────────────┘                                 │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   STRATEGY CONTEXT                                       │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  ┌──────────────────────┐          ┌──────────────────────┐                             │
│  │  operations_strategy │          │   global_readiness   │                             │
│  ├──────────────────────┤          ├──────────────────────┤                             │
│  │ PK id                │          │ PK id                │                             │
│  │ FK company_id        │          │ FK company_id        │                             │
│  │    name              │          │    country_code      │                             │
│  │    weight_*          │          │    readiness_score   │                             │
│  │    result_*          │          │    hs_code_mapping   │                             │
│  │    is_active         │          │    certification     │                             │
│  │    deployed_at       │          │    logistics_ready   │                             │
│  └──────────────────────┘          └──────────────────────┘                             │
│          │                                                                               │
│          │ 1:N                                                                           │
│          ▼                                                                               │
│  ┌──────────────────────┐                                                               │
│  │  strategy_change_log │                                                               │
│  ├──────────────────────┤                                                               │
│  │ PK id                │                                                               │
│  │ FK strategy_id       │                                                               │
│  │ FK changed_by        │                                                               │
│  │    change_type       │                                                               │
│  │    previous_value    │                                                               │
│  │    new_value         │                                                               │
│  └──────────────────────┘                                                               │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 2.2 테이블 상세 명세

#### 2.2.1 Identity Context

```sql
-- =============================================
-- 회사 테이블
-- =============================================
CREATE TABLE company (
    id              CHAR(36)        PRIMARY KEY COMMENT '회사 ID (UUID)',
    name            VARCHAR(100)    NOT NULL COMMENT '회사명',
    business_number VARCHAR(20)     NOT NULL UNIQUE COMMENT '사업자등록번호',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태: ACTIVE, SUSPENDED, DELETED',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_company_status (status),
    INDEX idx_company_business_number (business_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='회사 정보';

-- =============================================
-- 사용자 테이블
-- =============================================
CREATE TABLE user (
    id              CHAR(36)        PRIMARY KEY COMMENT '사용자 ID (UUID)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    email           VARCHAR(255)    NOT NULL COMMENT '이메일',
    name            VARCHAR(50)     NOT NULL COMMENT '이름',
    password_hash   VARCHAR(255)    NULL COMMENT '비밀번호 해시 (OAuth 사용 시 NULL)',
    role            VARCHAR(20)     NOT NULL DEFAULT 'VIEWER' COMMENT '역할: OWNER, EDITOR, VIEWER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태: ACTIVE, INACTIVE, INVITED',
    last_login_at   DATETIME(6)     NULL COMMENT '마지막 로그인',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(id),
    UNIQUE INDEX uk_user_company_email (company_id, email),
    INDEX idx_user_status (status),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='사용자 정보';
```

#### 2.2.2 Catalog Context

```sql
-- =============================================
-- 상품 테이블
-- =============================================
CREATE TABLE product (
    id              VARCHAR(50)     PRIMARY KEY COMMENT '상품 ID (OMS-FGXXXXXXXXXX)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    sku             VARCHAR(50)     NOT NULL COMMENT 'SKU',
    name_ko         VARCHAR(200)    NOT NULL COMMENT '상품명 (한국어)',
    name_en         VARCHAR(200)    NULL COMMENT '상품명 (영어)',
    brand           VARCHAR(100)    NULL COMMENT '브랜드',
    category_path   VARCHAR(500)    NOT NULL COMMENT '카테고리 경로 (> 구분자)',
    uom             VARCHAR(10)     NOT NULL DEFAULT 'PCS' COMMENT '단위: PCS, SET, BOX, KG, EA, TAI(台)',
    base_price      DECIMAL(15,2)   NOT NULL DEFAULT 0 COMMENT '기준 판매가',
    currency        VARCHAR(3)      NOT NULL DEFAULT 'KRW' COMMENT '통화',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태: ACTIVE, INACTIVE, OUT_OF_STOCK',

    -- Dimensions (Embedded)
    dim_width       DECIMAL(10,2)   NULL COMMENT '가로 (cm)',
    dim_length      DECIMAL(10,2)   NULL COMMENT '세로 (cm)',
    dim_height      DECIMAL(10,2)   NULL COMMENT '높이 (cm)',
    dim_unit        VARCHAR(5)      NULL DEFAULT 'CM' COMMENT '치수 단위: CM, MM',

    -- Weight (Embedded)
    net_weight      DECIMAL(10,3)   NULL COMMENT '순중량 (kg)',
    gross_weight    DECIMAL(10,3)   NULL COMMENT '총중량 (kg)',

    -- Logistics Info (Embedded)
    temp_management VARCHAR(30)     NOT NULL DEFAULT 'NORMAL' COMMENT '온도 관리: NORMAL, TEMPERATURE_CONTROL, COLD, FREEZING, CRYOGENIC',
    shelf_life_mgmt BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '유통기한 관리 여부',
    sn_mgmt         BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '시리얼 넘버 관리 여부',
    is_dangerous    BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '위험물 여부',
    is_fragile      BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '파손 주의 여부',
    is_high_value   BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '고가 상품 여부',
    is_non_standard BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '비표준 사이즈 여부',

    -- Customs Base Info
    hs_code         VARCHAR(20)     NULL COMMENT 'HS Code (기본)',
    country_origin  VARCHAR(50)     NULL COMMENT '원산지',
    material        VARCHAR(500)    NULL COMMENT '소재/성분',
    manufacturer    VARCHAR(200)    NULL COMMENT '제조사',
    mfr_address     VARCHAR(500)    NULL COMMENT '제조사 주소',

    -- 추가 필드 (프론트엔드 호환)
    color           VARCHAR(50)     NULL COMMENT '상품 색상',
    owner_name      VARCHAR(100)    NULL COMMENT '상품 소유자명',
    customer_goods_no VARCHAR(100)  NULL COMMENT '채널별 상품코드 (고객사 상품번호)',

    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_product_company FOREIGN KEY (company_id) REFERENCES company(id),
    UNIQUE INDEX uk_product_company_sku (company_id, sku),
    INDEX idx_product_status (status),
    INDEX idx_product_category (category_path(100)),
    INDEX idx_product_brand (brand),
    FULLTEXT INDEX ft_product_name (name_ko, name_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='상품 마스터';

-- =============================================
-- 상품 바코드 테이블
-- =============================================
CREATE TABLE product_barcode (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_id      VARCHAR(50)     NOT NULL COMMENT '상품 ID',
    code            VARCHAR(50)     NOT NULL COMMENT '바코드',
    is_main         BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '메인 바코드 여부',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_barcode_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE INDEX uk_barcode_code (code),
    INDEX idx_barcode_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='상품 바코드';

-- =============================================
-- 상품 통관 전략 테이블
-- =============================================
CREATE TABLE product_customs_strategy (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_id      VARCHAR(50)     NOT NULL COMMENT '상품 ID',
    country_code    CHAR(2)         NOT NULL COMMENT '국가 코드 (ISO 3166-1 alpha-2)',
    local_hs_code   VARCHAR(20)     NOT NULL COMMENT '현지 HS Code',
    invoice_name    VARCHAR(200)    NOT NULL COMMENT '통관용 영문 상품명',
    duty_rate       VARCHAR(20)     NULL COMMENT '관세율',
    required_docs   JSON            NULL COMMENT '필요 서류 목록',
    compliance_alert VARCHAR(500)   NULL COMMENT '규제 경고',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_customs_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    UNIQUE INDEX uk_customs_product_country (product_id, country_code),
    INDEX idx_customs_country (country_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='상품 통관 전략';
```

#### 2.2.3 Channel Context

```sql
-- =============================================
-- 채널 테이블
-- =============================================
CREATE TABLE channel (
    id              VARCHAR(20)     PRIMARY KEY COMMENT '채널 ID (CH-XXX)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    name            VARCHAR(100)    NOT NULL COMMENT '채널명',
    type            VARCHAR(20)     NOT NULL COMMENT '타입: D2C, MARKET, GLOBAL',
    status          VARCHAR(20)     NOT NULL DEFAULT 'DISCONNECTED' COMMENT '상태: CONNECTED, DISCONNECTED, ERROR',
    last_sync_at    DATETIME(6)     NULL COMMENT '마지막 동기화',

    -- 인증 정보 (암호화)
    api_key         VARCHAR(500)    NULL COMMENT 'API Key (암호화)',
    secret_key      VARCHAR(500)    NULL COMMENT 'Secret Key (암호화)',
    additional_cfg  JSON            NULL COMMENT '추가 설정',

    logo_emoji      VARCHAR(10)     NULL COMMENT '로고 이모지',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_channel_company FOREIGN KEY (company_id) REFERENCES company(id),
    INDEX idx_channel_company (company_id),
    INDEX idx_channel_type (type),
    INDEX idx_channel_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='판매 채널';

-- =============================================
-- 창고 테이블
-- =============================================
CREATE TABLE warehouse (
    id              VARCHAR(20)     PRIMARY KEY COMMENT '창고 ID (WH-XXX)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    name            VARCHAR(100)    NOT NULL COMMENT '창고명',
    region          VARCHAR(50)     NOT NULL COMMENT '권역: 수도권, 중부권, 영남권, Global',
    type            VARCHAR(20)     NOT NULL COMMENT '타입: AUTO, MEGA, HUB, AIR',
    capacity        INT             NOT NULL DEFAULT 0 COMMENT '가동률 (%)',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '상태: ACTIVE, MAINTENANCE, INACTIVE',

    -- 주소
    zip_code        VARCHAR(10)     NULL,
    address1        VARCHAR(200)    NULL,
    address2        VARCHAR(200)    NULL,
    city            VARCHAR(50)     NULL,
    country         VARCHAR(50)     NOT NULL DEFAULT 'KR',

    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_warehouse_company FOREIGN KEY (company_id) REFERENCES company(id),
    INDEX idx_warehouse_company (company_id),
    INDEX idx_warehouse_region (region),
    INDEX idx_warehouse_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='물류 창고';

-- =============================================
-- 채널-창고 매핑 테이블 (N:M)
-- =============================================
CREATE TABLE channel_warehouse_mapping (
    id                  CHAR(36)        PRIMARY KEY COMMENT '매핑 ID (UUID)',
    channel_id          VARCHAR(20)     NOT NULL COMMENT '채널 ID',
    warehouse_id        VARCHAR(20)     NOT NULL COMMENT '창고 ID',
    role                VARCHAR(20)     NOT NULL COMMENT '역할: PRIMARY, REGIONAL, BACKUP',
    priority            INT             NOT NULL DEFAULT 1 COMMENT '우선순위 (1이 최우선)',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    load_balancing      BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '부하 분산 활성화',
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_mapping_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    CONSTRAINT fk_mapping_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    UNIQUE INDEX uk_mapping_channel_warehouse (channel_id, warehouse_id),
    INDEX idx_mapping_channel (channel_id),
    INDEX idx_mapping_warehouse (warehouse_id),
    INDEX idx_mapping_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='채널-창고 매핑 (Logistics Mesh)';

-- =============================================
-- 상품-채널 매핑 테이블
-- =============================================
CREATE TABLE product_channel_mapping (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    product_id      VARCHAR(50)     NOT NULL COMMENT '상품 ID',
    channel_id      VARCHAR(20)     NOT NULL COMMENT '채널 ID',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_pcm_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_pcm_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    UNIQUE INDEX uk_pcm_product_channel (product_id, channel_id),
    INDEX idx_pcm_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='상품-채널 매핑';
```

#### 2.2.4 Order Context

```sql
-- =============================================
-- 주문 테이블
-- =============================================
CREATE TABLE `order` (
    id                  VARCHAR(30)     PRIMARY KEY COMMENT '주문 ID (ORD-YYYYMMDD-XXX)',
    company_id          CHAR(36)        NOT NULL COMMENT '회사 ID',
    channel_id          VARCHAR(20)     NOT NULL COMMENT '채널 ID',
    external_order_id   VARCHAR(100)    NULL COMMENT '채널 주문번호',
    status              VARCHAR(30)     NOT NULL DEFAULT 'NEW' COMMENT '주문 상태',
    order_date          DATETIME(6)     NOT NULL COMMENT '주문일시',

    -- 고객 정보
    customer_name       VARCHAR(50)     NOT NULL COMMENT '고객명',
    customer_phone      VARCHAR(20)     NOT NULL COMMENT '고객 전화번호',
    customer_email      VARCHAR(255)    NULL COMMENT '고객 이메일',

    -- 배송지
    ship_zip_code       VARCHAR(10)     NOT NULL,
    ship_address1       VARCHAR(200)    NOT NULL,
    ship_address2       VARCHAR(200)    NULL,
    ship_city           VARCHAR(50)     NOT NULL,
    ship_country        VARCHAR(50)     NOT NULL DEFAULT 'KR',

    -- 풀필먼트
    fulfillment_method  VARCHAR(10)     NOT NULL DEFAULT 'WMS' COMMENT 'WMS, DIRECT',
    warehouse_id        VARCHAR(20)     NULL COMMENT '배정된 창고 ID',
    routing_logic       VARCHAR(50)     NULL COMMENT '라우팅 로직',

    -- 금액
    total_amount        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'KRW',

    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_order_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_order_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    CONSTRAINT fk_order_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    INDEX idx_order_company (company_id),
    INDEX idx_order_channel (channel_id),
    INDEX idx_order_status (status),
    INDEX idx_order_date (order_date),
    INDEX idx_order_external (external_order_id),
    INDEX idx_order_fulfillment (fulfillment_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='주문';

-- =============================================
-- 주문 상품 테이블
-- =============================================
CREATE TABLE order_item (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        VARCHAR(30)     NOT NULL COMMENT '주문 ID',
    product_id      VARCHAR(50)     NOT NULL COMMENT '상품 ID',
    product_name    VARCHAR(200)    NOT NULL COMMENT '상품명 (스냅샷)',
    sku             VARCHAR(50)     NOT NULL COMMENT 'SKU (스냅샷)',
    quantity        INT             NOT NULL COMMENT '수량',
    unit_price      DECIMAL(15,2)   NOT NULL COMMENT '단가',
    total_price     DECIMAL(15,2)   NOT NULL COMMENT '합계',
    currency        VARCHAR(3)      NOT NULL DEFAULT 'KRW',

    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product(id),
    INDEX idx_item_order (order_id),
    INDEX idx_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='주문 상품';

-- =============================================
-- 배송 테이블
-- =============================================
CREATE TABLE shipping (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    order_id        VARCHAR(30)     NOT NULL UNIQUE COMMENT '주문 ID',
    carrier         VARCHAR(20)     NOT NULL COMMENT '택배사: CJ, HANJIN, LOGEN, POST, FEDEX, DHL, UPS',
    tracking_number VARCHAR(50)     NOT NULL COMMENT '송장번호',
    status          VARCHAR(30)     NOT NULL DEFAULT 'PICKED_UP' COMMENT '상태',
    shipped_at      DATETIME(6)     NULL COMMENT '출고일시',
    delivered_at    DATETIME(6)     NULL COMMENT '배송완료일시',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_shipping_order FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    INDEX idx_shipping_carrier (carrier),
    INDEX idx_shipping_tracking (tracking_number),
    INDEX idx_shipping_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='배송 정보';

-- =============================================
-- 배송 추적 이벤트 테이블
-- =============================================
CREATE TABLE tracking_event (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    shipping_id     BIGINT          NOT NULL COMMENT '배송 ID',
    status          VARCHAR(30)     NOT NULL COMMENT '상태',
    location        VARCHAR(200)    NULL COMMENT '위치',
    description     VARCHAR(500)    NULL COMMENT '설명',
    event_time      DATETIME(6)     NOT NULL COMMENT '이벤트 발생 시간',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_event_shipping FOREIGN KEY (shipping_id) REFERENCES shipping(id) ON DELETE CASCADE,
    INDEX idx_event_shipping (shipping_id),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='배송 추적 이벤트';
```

#### 2.2.5 Inventory Context

```sql
-- =============================================
-- 재고 테이블
-- =============================================
CREATE TABLE stock (
    id              CHAR(36)        PRIMARY KEY COMMENT '재고 ID (UUID)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    product_id      VARCHAR(50)     NOT NULL COMMENT '상품 ID',
    warehouse_id    VARCHAR(20)     NOT NULL COMMENT '창고 ID',
    total           INT             NOT NULL DEFAULT 0 COMMENT '전체 재고',
    available       INT             NOT NULL DEFAULT 0 COMMENT '가용 재고',
    reserved        INT             NOT NULL DEFAULT 0 COMMENT '예약 재고',
    safety_stock    INT             NOT NULL DEFAULT 0 COMMENT '안전 재고',
    status          VARCHAR(20)     NOT NULL DEFAULT 'NORMAL' COMMENT '상태: NORMAL, LOW, OUT_OF_STOCK, OVERSTOCK',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_stock_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_stock_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id),
    UNIQUE INDEX uk_stock_product_warehouse (product_id, warehouse_id),
    INDEX idx_stock_company (company_id),
    INDEX idx_stock_status (status),
    INDEX idx_stock_available (available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='재고';

-- =============================================
-- 재고 채널 할당 테이블
-- =============================================
CREATE TABLE stock_channel_allocation (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    stock_id        CHAR(36)        NOT NULL COMMENT '재고 ID',
    channel_id      VARCHAR(20)     NOT NULL COMMENT '채널 ID',
    quantity        INT             NOT NULL DEFAULT 0 COMMENT '할당 수량',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_alloc_stock FOREIGN KEY (stock_id) REFERENCES stock(id) ON DELETE CASCADE,
    CONSTRAINT fk_alloc_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    UNIQUE INDEX uk_alloc_stock_channel (stock_id, channel_id),
    INDEX idx_alloc_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='재고 채널 할당';

-- =============================================
-- 재고 이동 이력 테이블
-- =============================================
CREATE TABLE stock_movement (
    id              CHAR(36)        PRIMARY KEY COMMENT '이동 ID (UUID)',
    stock_id        CHAR(36)        NOT NULL COMMENT '재고 ID',
    type            VARCHAR(20)     NOT NULL COMMENT '타입: RECEIVE, SHIP, RESERVE, RELEASE, ADJUST, TRANSFER_IN, TRANSFER_OUT',
    quantity        INT             NOT NULL COMMENT '수량',
    before_qty      INT             NOT NULL COMMENT '변경 전 수량',
    after_qty       INT             NOT NULL COMMENT '변경 후 수량',
    reason          VARCHAR(500)    NULL COMMENT '사유',
    reference_id    VARCHAR(50)     NULL COMMENT '참조 ID (주문ID 등)',
    created_by      CHAR(36)        NULL COMMENT '작업자 ID',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_movement_stock FOREIGN KEY (stock_id) REFERENCES stock(id),
    CONSTRAINT fk_movement_user FOREIGN KEY (created_by) REFERENCES user(id),
    INDEX idx_movement_stock (stock_id),
    INDEX idx_movement_type (type),
    INDEX idx_movement_date (created_at),
    INDEX idx_movement_reference (reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='재고 이동 이력';
```

#### 2.2.6 Claim Context

```sql
-- =============================================
-- 클레임 테이블
-- =============================================
CREATE TABLE claim (
    id              VARCHAR(30)     PRIMARY KEY COMMENT '클레임 ID (CLM-YYYYMMDD-XXX)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    order_id        VARCHAR(30)     NOT NULL COMMENT '주문 ID',
    type            VARCHAR(20)     NOT NULL COMMENT '타입: CANCEL, RETURN, EXCHANGE',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '상태: PENDING, PROCESSING, COMPLETED, REJECTED',
    reason          VARCHAR(500)    NOT NULL COMMENT '사유',
    memo            TEXT            NULL COMMENT '메모',
    priority        VARCHAR(10)     NOT NULL DEFAULT 'NORMAL' COMMENT '우선순위: URGENT, NORMAL',
    refund_amount   DECIMAL(15,2)   NULL COMMENT '환불 금액',
    currency        VARCHAR(3)      NULL DEFAULT 'KRW',
    refunded_at     DATETIME(6)     NULL COMMENT '환불 처리일',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at    DATETIME(6)     NULL COMMENT '처리 완료일',

    CONSTRAINT fk_claim_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_claim_order FOREIGN KEY (order_id) REFERENCES `order`(id),
    INDEX idx_claim_company (company_id),
    INDEX idx_claim_order (order_id),
    INDEX idx_claim_type (type),
    INDEX idx_claim_status (status),
    INDEX idx_claim_priority (priority),
    INDEX idx_claim_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='클레임';

-- =============================================
-- 클레임 상품 테이블
-- =============================================
CREATE TABLE claim_item (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    claim_id        VARCHAR(30)     NOT NULL COMMENT '클레임 ID',
    product_id      VARCHAR(50)     NOT NULL COMMENT '상품 ID',
    quantity        INT             NOT NULL COMMENT '수량',
    reason          VARCHAR(500)    NULL COMMENT '개별 사유',

    CONSTRAINT fk_ci_claim FOREIGN KEY (claim_id) REFERENCES claim(id) ON DELETE CASCADE,
    CONSTRAINT fk_ci_product FOREIGN KEY (product_id) REFERENCES product(id),
    INDEX idx_ci_claim (claim_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='클레임 상품';
```

#### 2.2.7 Settlement Context

```sql
-- =============================================
-- 정산 테이블
-- =============================================
CREATE TABLE settlement (
    id              CHAR(36)        PRIMARY KEY COMMENT '정산 ID (UUID)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    channel_id      VARCHAR(20)     NOT NULL COMMENT '채널 ID',
    period_year     INT             NOT NULL COMMENT '정산 연도',
    period_month    INT             NOT NULL COMMENT '정산 월',
    total_sales     DECIMAL(15,2)   NOT NULL DEFAULT 0 COMMENT '총 판매금액',
    total_commission DECIMAL(15,2)  NOT NULL DEFAULT 0 COMMENT '총 수수료',
    net_settlement  DECIMAL(15,2)   NOT NULL DEFAULT 0 COMMENT '실 정산액',
    currency        VARCHAR(3)      NOT NULL DEFAULT 'KRW',
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT' COMMENT '상태: DRAFT, CONFIRMED, PAID',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    confirmed_at    DATETIME(6)     NULL COMMENT '확정일',

    CONSTRAINT fk_settlement_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_settlement_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    UNIQUE INDEX uk_settlement_period (company_id, channel_id, period_year, period_month),
    INDEX idx_settlement_channel (channel_id),
    INDEX idx_settlement_period (period_year, period_month),
    INDEX idx_settlement_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='정산';

-- =============================================
-- 정산 상세 테이블
-- =============================================
CREATE TABLE settlement_item (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    settlement_id   CHAR(36)        NOT NULL COMMENT '정산 ID',
    order_id        VARCHAR(30)     NOT NULL COMMENT '주문 ID',
    sales_amount    DECIMAL(15,2)   NOT NULL COMMENT '판매금액',
    commission      DECIMAL(15,2)   NOT NULL COMMENT '수수료',
    currency        VARCHAR(3)      NOT NULL DEFAULT 'KRW',

    CONSTRAINT fk_si_settlement FOREIGN KEY (settlement_id) REFERENCES settlement(id) ON DELETE CASCADE,
    CONSTRAINT fk_si_order FOREIGN KEY (order_id) REFERENCES `order`(id),
    INDEX idx_si_settlement (settlement_id),
    INDEX idx_si_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='정산 상세';
```

#### 2.2.8 Automation Context

```sql
-- =============================================
-- 자동화 규칙 테이블
-- =============================================
CREATE TABLE automation_rule (
    id                  CHAR(36)        PRIMARY KEY COMMENT '규칙 ID (UUID)',
    company_id          CHAR(36)        NOT NULL COMMENT '회사 ID',
    name                VARCHAR(100)    NOT NULL COMMENT '규칙명',
    description         VARCHAR(500)    NULL COMMENT '설명',
    trigger_type        VARCHAR(30)     NOT NULL COMMENT '트리거: ORDER_CREATED, ORDER_STATUS_CHANGED, STOCK_LOW, STOCK_CHANGED, PAYMENT_FAILED, SHIPPING_STARTED',
    trigger_config      JSON            NULL COMMENT '트리거 설정',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    last_executed_at    DATETIME(6)     NULL COMMENT '마지막 실행일',
    execution_count     INT             NOT NULL DEFAULT 0 COMMENT '실행 횟수',
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_rule_company FOREIGN KEY (company_id) REFERENCES company(id),
    INDEX idx_rule_company (company_id),
    INDEX idx_rule_trigger (trigger_type),
    INDEX idx_rule_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='자동화 규칙';

-- =============================================
-- 규칙 조건 테이블
-- =============================================
CREATE TABLE rule_condition (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    rule_id         CHAR(36)        NOT NULL COMMENT '규칙 ID',
    field           VARCHAR(100)    NOT NULL COMMENT '필드명',
    operator        VARCHAR(20)     NOT NULL COMMENT '연산자: EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, CONTAINS',
    value           VARCHAR(500)    NOT NULL COMMENT '비교값',
    order_num       INT             NOT NULL DEFAULT 0 COMMENT '순서',

    CONSTRAINT fk_condition_rule FOREIGN KEY (rule_id) REFERENCES automation_rule(id) ON DELETE CASCADE,
    INDEX idx_condition_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='규칙 조건';

-- =============================================
-- 규칙 액션 테이블
-- =============================================
CREATE TABLE rule_action (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    rule_id         CHAR(36)        NOT NULL COMMENT '규칙 ID',
    action_type     VARCHAR(30)     NOT NULL COMMENT '액션: SEND_SLACK, SEND_EMAIL, CHANGE_ORDER_STATUS, ADJUST_STOCK',
    action_config   JSON            NOT NULL COMMENT '액션 설정',
    order_num       INT             NOT NULL DEFAULT 0 COMMENT '순서',

    CONSTRAINT fk_action_rule FOREIGN KEY (rule_id) REFERENCES automation_rule(id) ON DELETE CASCADE,
    INDEX idx_action_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='규칙 액션';
```

#### 2.2.9 Strategy Context

```sql
-- =============================================
-- 운영 전략 테이블
-- =============================================
CREATE TABLE operations_strategy (
    id              CHAR(36)        PRIMARY KEY COMMENT '전략 ID (UUID)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    name            VARCHAR(100)    NOT NULL COMMENT '전략명',

    -- 가중치 설정
    weight_cost_reduction   INT     NOT NULL DEFAULT 50 COMMENT '물류비용 절감 가중치 (%)',
    weight_lead_time        INT     NOT NULL DEFAULT 50 COMMENT '리드타임 최단화 가중치 (%)',
    weight_stock_balance    INT     NOT NULL DEFAULT 50 COMMENT '재고 분산 가중치 (%)',
    weight_carbon_emission  INT     NOT NULL DEFAULT 20 COMMENT '탄소 배출 저감 가중치 (%)',

    -- 시뮬레이션 결과
    result_efficiency_score DECIMAL(5,2)  NULL COMMENT '운영 효율성 점수',
    result_cost_saving      DECIMAL(15,2) NULL COMMENT '예상 비용 절감액',
    result_avg_lead_time    DECIMAL(5,2)  NULL COMMENT '예상 평균 리드타임 (일)',

    is_active       BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '활성화 여부',
    deployed_at     DATETIME(6)     NULL COMMENT '배포일',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_strategy_company FOREIGN KEY (company_id) REFERENCES company(id),
    INDEX idx_strategy_company (company_id),
    INDEX idx_strategy_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='운영 전략';

-- =============================================
-- 글로벌 진출 준비도 테이블
-- =============================================
CREATE TABLE global_readiness (
    id              CHAR(36)        PRIMARY KEY COMMENT '준비도 ID (UUID)',
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    country_code    CHAR(2)         NOT NULL COMMENT '국가 코드 (ISO 3166-1)',
    country_name    VARCHAR(50)     NOT NULL COMMENT '국가명',

    -- 준비도 점수 (0-100)
    readiness_score         INT     NOT NULL DEFAULT 0 COMMENT '전체 준비도 (%)',
    hs_code_mapping_rate    INT     NOT NULL DEFAULT 0 COMMENT 'HS Code 매핑률 (%)',
    certification_status    VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '인증 상태: NONE, PARTIAL, COMPLETE',
    logistics_node_ready    BOOLEAN NOT NULL DEFAULT FALSE COMMENT '물류 노드 준비 여부',
    tax_id_registered       BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Tax ID 등록 여부',

    -- 상세 정보
    required_certifications JSON    NULL COMMENT '필요 인증 목록',
    blockers                JSON    NULL COMMENT '진입 장벽 목록',
    notes                   TEXT    NULL COMMENT '비고',

    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_readiness_company FOREIGN KEY (company_id) REFERENCES company(id),
    UNIQUE INDEX uk_readiness_company_country (company_id, country_code),
    INDEX idx_readiness_score (readiness_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='글로벌 진출 준비도';

-- =============================================
-- 전략 변경 로그 테이블
-- =============================================
CREATE TABLE strategy_change_log (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    company_id      CHAR(36)        NOT NULL COMMENT '회사 ID',
    strategy_id     CHAR(36)        NOT NULL COMMENT '전략 ID',
    change_type     VARCHAR(20)     NOT NULL COMMENT '변경 유형: CREATE, UPDATE, DEPLOY, DEACTIVATE',
    changed_by      CHAR(36)        NOT NULL COMMENT '변경자 ID',
    previous_value  JSON            NULL COMMENT '이전 값',
    new_value       JSON            NULL COMMENT '새 값',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_changelog_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_changelog_strategy FOREIGN KEY (strategy_id) REFERENCES operations_strategy(id),
    CONSTRAINT fk_changelog_user FOREIGN KEY (changed_by) REFERENCES user(id),
    INDEX idx_changelog_strategy (strategy_id),
    INDEX idx_changelog_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='전략 변경 로그';
```

---

## 3. MongoDB 컬렉션 설계

### 3.1 Integration Context

```javascript
// =============================================
// 웹훅 로그 컬렉션
// =============================================
// Collection: webhook_logs

{
    _id: ObjectId,
    companyId: "uuid-string",
    source: "NAVER",                    // 채널명
    eventType: "ORDER.PAID",            // 이벤트 타입
    httpStatus: 200,
    latencyMs: 124,
    requestPayload: {                   // JSON 객체
        orderId: "EXT-123",
        // ... 원본 페이로드
    },
    responsePayload: {                  // JSON 객체 (nullable)
        success: true
    },
    errorMessage: null,
    retryCount: 0,
    createdAt: ISODate("2025-01-18T10:30:00Z")
}

// 인덱스
db.webhook_logs.createIndex({ companyId: 1, createdAt: -1 })
db.webhook_logs.createIndex({ source: 1, eventType: 1 })
db.webhook_logs.createIndex({ httpStatus: 1 })
db.webhook_logs.createIndex({ createdAt: 1 }, { expireAfterSeconds: 7776000 })  // 90일 TTL


// =============================================
// API 로그 컬렉션
// =============================================
// Collection: api_logs

{
    _id: ObjectId,
    companyId: "uuid-string",
    channelId: "CH-001",
    direction: "OUTBOUND",              // INBOUND / OUTBOUND
    endpoint: "https://api.naver.com/orders",
    method: "POST",
    httpStatus: 200,
    latencyMs: 342,
    requestHeaders: {
        "Content-Type": "application/json",
        "Authorization": "Bearer ***"   // 마스킹
    },
    requestBody: "{ ... }",             // 문자열 (큰 경우 압축)
    responseBody: "{ ... }",
    createdAt: ISODate("2025-01-18T10:30:00Z")
}

// 인덱스
db.api_logs.createIndex({ companyId: 1, channelId: 1, createdAt: -1 })
db.api_logs.createIndex({ direction: 1, httpStatus: 1 })
db.api_logs.createIndex({ createdAt: 1 }, { expireAfterSeconds: 2592000 })  // 30일 TTL


// =============================================
// 도메인 이벤트 컬렉션 (Event Sourcing)
// =============================================
// Collection: domain_events

{
    _id: ObjectId,
    aggregateId: "ORD-20250118-001",    // Aggregate ID
    aggregateType: "Order",              // Aggregate 타입
    eventType: "OrderCreatedEvent",
    eventData: {
        orderId: "ORD-20250118-001",
        channelId: "CH-001",
        customerId: "CUST-001",
        totalAmount: 150000,
        items: [
            { productId: "OMS-FG001", quantity: 2 }
        ]
    },
    version: 1,                          // 이벤트 순서
    createdAt: ISODate("2025-01-18T10:30:00Z"),
    createdBy: "user-uuid"
}

// 인덱스
db.domain_events.createIndex({ aggregateId: 1, version: 1 }, { unique: true })
db.domain_events.createIndex({ aggregateType: 1, eventType: 1 })
db.domain_events.createIndex({ createdAt: -1 })


// =============================================
// AI 채팅 로그 컬렉션
// =============================================
// Collection: ai_chat_logs

{
    _id: ObjectId,
    companyId: "uuid-string",
    userId: "uuid-string",
    sessionId: "session-uuid",
    message: "재고 부족 상품 알려줘",
    context: {
        currentPage: "inventory"
    },
    response: "현재 재고 부족 상품은 5개입니다...",
    latencyMs: 1200,
    createdAt: ISODate("2025-01-18T10:30:00Z")
}

// 인덱스
db.ai_chat_logs.createIndex({ companyId: 1, userId: 1, createdAt: -1 })
db.ai_chat_logs.createIndex({ sessionId: 1 })
db.ai_chat_logs.createIndex({ createdAt: 1 }, { expireAfterSeconds: 7776000 })  // 90일 TTL


// =============================================
// 운영 시뮬레이션 로그 컬렉션
// =============================================
// Collection: simulation_logs

{
    _id: ObjectId,
    companyId: "uuid-string",
    userId: "uuid-string",
    weights: {
        costReduction: 75,
        leadTime: 40,
        stockBalance: 60,
        carbonEmission: 20
    },
    results: {
        efficiencyScore: 84.2,
        costSaving: 1200000,
        avgLeadTime: 1.8
    },
    deployed: true,
    deployedAt: ISODate("2025-01-18T11:00:00Z"),
    createdAt: ISODate("2025-01-18T10:30:00Z")
}

// 인덱스
db.simulation_logs.createIndex({ companyId: 1, createdAt: -1 })
db.simulation_logs.createIndex({ deployed: 1 })
```

---

## 4. 인덱스 전략

### 4.1 MySQL 인덱스 요약

| 테이블 | 인덱스 | 타입 | 용도 |
|--------|--------|------|------|
| `company` | `business_number` | UNIQUE | 사업자번호 조회 |
| `user` | `(company_id, email)` | UNIQUE | 회사 내 이메일 중복 방지 |
| `product` | `(company_id, sku)` | UNIQUE | SKU 유일성 |
| `product` | `(name_ko, name_en)` | FULLTEXT | 상품명 검색 |
| `order` | `(company_id, status, order_date)` | COMPOSITE | 주문 목록 조회 |
| `order` | `external_order_id` | INDEX | 채널 주문번호 조회 |
| `stock` | `(product_id, warehouse_id)` | UNIQUE | 재고 유일성 |
| `channel_warehouse_mapping` | `(channel_id, warehouse_id)` | UNIQUE | 매핑 유일성 |
| `operations_strategy` | `company_id` | INDEX | 회사별 전략 조회 |
| `global_readiness` | `(company_id, country_code)` | UNIQUE | 국가별 준비도 유일성 |

### 4.2 MongoDB 인덱스 요약

| 컬렉션 | 인덱스 | TTL | 용도 |
|--------|--------|-----|------|
| `webhook_logs` | `companyId + createdAt` | 90일 | 웹훅 조회 |
| `api_logs` | `companyId + channelId + createdAt` | 30일 | API 로그 조회 |
| `domain_events` | `aggregateId + version` | - | 이벤트 순서 보장 |

---

## 5. 마이그레이션 전략

### 5.1 Flyway 마이그레이션 파일 구조

```
resources/db/migration/
├── V1__init_identity_context.sql
├── V2__init_catalog_context.sql
├── V3__init_channel_context.sql
├── V4__init_order_context.sql
├── V5__init_inventory_context.sql
├── V6__init_claim_context.sql
├── V7__init_settlement_context.sql
├── V8__init_automation_context.sql
├── V9__add_indexes.sql
└── V10__init_seed_data.sql
```

### 5.2 초기 시드 데이터

```sql
-- V10__init_seed_data.sql

-- 테스트 회사
INSERT INTO company (id, name, business_number, status, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Global OMS Demo', '123-45-67890', 'ACTIVE', NOW());

-- 기본 채널
INSERT INTO channel (id, company_id, name, type, status, logo_emoji, created_at)
VALUES
    ('CH-001', '00000000-0000-0000-0000-000000000001', '자사몰', 'D2C', 'CONNECTED', '🛍️', NOW()),
    ('CH-002', '00000000-0000-0000-0000-000000000001', '네이버 스마트스토어', 'MARKET', 'CONNECTED', '🟢', NOW()),
    ('CH-003', '00000000-0000-0000-0000-000000000001', '쿠팡', 'MARKET', 'CONNECTED', '🟤', NOW()),
    ('CH-004', '00000000-0000-0000-0000-000000000001', '아마존 US', 'GLOBAL', 'CONNECTED', '📦', NOW()),
    ('CH-005', '00000000-0000-0000-0000-000000000001', '쇼피 SG', 'GLOBAL', 'CONNECTED', '🧡', NOW());

-- 기본 창고
INSERT INTO warehouse (id, company_id, name, region, type, capacity, status, country, created_at)
VALUES
    ('WH-001', '00000000-0000-0000-0000-000000000001', '김포 자동화 센터', '수도권', 'AUTO', 85, 'ACTIVE', 'KR', NOW()),
    ('WH-002', '00000000-0000-0000-0000-000000000001', '용인 메가 허브', '수도권', 'MEGA', 72, 'ACTIVE', 'KR', NOW()),
    ('WH-003', '00000000-0000-0000-0000-000000000001', '칠곡 허브', '영남권', 'HUB', 64, 'ACTIVE', 'KR', NOW()),
    ('WH-004', '00000000-0000-0000-0000-000000000001', '인천 항공 센터', '수도권', 'AIR', 45, 'ACTIVE', 'KR', NOW());

-- 기본 채널-창고 매핑
INSERT INTO channel_warehouse_mapping (id, channel_id, warehouse_id, role, priority, is_active, load_balancing, created_at)
VALUES
    (UUID(), 'CH-001', 'WH-001', 'PRIMARY', 1, TRUE, FALSE, NOW()),
    (UUID(), 'CH-001', 'WH-002', 'BACKUP', 2, TRUE, FALSE, NOW()),
    (UUID(), 'CH-002', 'WH-002', 'PRIMARY', 1, TRUE, FALSE, NOW()),
    (UUID(), 'CH-003', 'WH-002', 'PRIMARY', 1, TRUE, TRUE, NOW()),
    (UUID(), 'CH-003', 'WH-003', 'REGIONAL', 2, TRUE, TRUE, NOW()),
    (UUID(), 'CH-004', 'WH-004', 'PRIMARY', 1, TRUE, FALSE, NOW()),
    (UUID(), 'CH-005', 'WH-004', 'PRIMARY', 1, TRUE, FALSE, NOW());
```

---

## 6. 다음 단계

### Phase 4: API 설계
- [ ] OpenAPI 3.0 스펙 작성
- [ ] 에러 코드 정의
- [ ] 페이지네이션/정렬 규칙

### Phase 5: 구현
- [ ] Kotlin 멀티모듈 프로젝트 생성
- [ ] 도메인 엔티티 구현
- [ ] Repository 구현
- [ ] Application Service 구현
- [ ] REST API Controller 구현

