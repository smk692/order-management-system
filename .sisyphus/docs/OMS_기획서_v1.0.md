# Global OMS (Order Management System) 기획서

**버전**: 1.0
**작성일**: 2025-01-18
**문서 상태**: Draft

---

## 1. 개요

### 1.1 시스템 목적

**Global OMS**는 글로벌 멀티 채널 통합 주문 관리 시스템입니다.

- **다중 판매 채널 통합**: D2C(자사몰) + 마켓플레이스(네이버, 쿠팡) + 글로벌(아마존, 쇼피)
- **다중 물류 거점 연계**: 국내 자동화 센터 + 글로벌 FBA/해외 창고
- **AI 기반 최적화**: 자동 라우팅, 재고 최적화, 운영 시뮬레이션

### 1.2 핵심 차별점

| 기능 | 설명 |
|------|------|
| **Logistics Mesh** | 채널-창고 N:M 관계를 동적 토폴로지로 관리 |
| **채널별 가상 재고** | 물리 재고를 채널별로 할당하여 오버셀링 방지 |
| **Fulfillment Method** | WMS 자동 vs 직접 발송 혼용 지원 |
| **글로벌 통관 전략** | 국가별 HS Code, 인보이스, 규제 자동 대응 |
| **실시간 웹훅 디버거** | API 통신 장애 감지 및 즉시 대응 |

### 1.3 기술 스택 (백엔드)

| 항목 | 선택 |
|------|------|
| 언어 | Kotlin |
| 프레임워크 | Spring Boot 3.x |
| 아키텍처 | DDD 멀티모듈 (레이어 + 도메인) |
| ORM | Spring Data JPA |
| 데이터베이스 | MySQL (핵심 도메인) + MongoDB (로그/이벤트) |

---

## 2. 비즈니스 도메인 분석

### 2.1 도메인 관계도

```
┌─────────────────────────────────────────────────────────────┐
│                        Company (회사)                        │
│  - 시스템의 최상위 테넌트 단위                                │
└─────────────────────────────────────────────────────────────┘
           │                              │
           │ 1:N                          │ 1:N
           ▼                              ▼
┌─────────────────┐              ┌─────────────────┐
│   User (사용자)  │              │ Channel (채널)  │
│ - Admin/Editor/ │              │ - D2C/Market/   │
│   Viewer        │              │   Global        │
└─────────────────┘              └─────────────────┘
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    │                     │ N:M                 │
                    ▼                     ▼                     ▼
           ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
           │    Order      │     │   Warehouse   │     │    Product    │
           │   (주문)      │◄────│   (창고)      │     │   (상품)      │
           └───────────────┘     └───────────────┘     └───────────────┘
                    │                     │                     │
                    │                     │                     │
                    ▼                     ▼                     ▼
           ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
           │   Shipping    │     │   Inventory   │◄────│ProductMapping │
           │   (배송)      │     │   (재고)      │     │(상품-채널매핑)│
           └───────────────┘     └───────────────┘     └───────────────┘
                    │
                    ▼
           ┌───────────────┐
           │    Claim      │
           │  (클레임)     │
           └───────────────┘
```

### 2.2 핵심 비즈니스 규칙

#### 2.2.1 회사-채널-창고 관계

```
Company (1) ──── (N) User
Company (1) ──── (N) Channel
Channel (N) ──── (M) Warehouse   [ChannelWarehouseMapping]
```

**ChannelWarehouseMapping 속성:**
- `role`: PRIMARY | REGIONAL | BACKUP
- `priority`: 우선순위 (1이 최우선)
- `isActive`: 활성화 여부

#### 2.2.2 주문 풀필먼트 결정 로직

```kotlin
when (order.fulfillmentMethod) {
    WMS -> {
        // 1. 채널의 연결된 창고 목록 조회
        // 2. 라우팅 로직에 따라 최적 창고 선택
        //    - 재고 우선 배정
        //    - 권역별 최단거리
        //    - 부하 분산
        // 3. wmsNode 배정
    }
    DIRECT -> {
        // 매장/직접 발송 (라우팅 없음)
    }
}
```

#### 2.2.3 재고 관리 규칙

```
물리 재고 (Physical) = 창고별 실제 재고
가상 재고 (Virtual) = 채널별 할당 재고

전체 재고 = available + reserved
available >= safetyStock 이면 NORMAL
available < safetyStock 이면 LOW
available == 0 이면 OUT_OF_STOCK
available > maxStock 이면 OVERSTOCK
```

---

## 3. 기능 명세

### 3.1 주문 관리 (Order Management)

#### 3.1.1 주문 목록 조회

| 항목 | 내용 |
|------|------|
| **기능 ID** | ORD-001 |
| **설명** | 주문 목록을 조회하고 필터링 |
| **입력** | 검색어, 상태 필터, 풀필먼트 필터, 페이지네이션 |
| **출력** | 주문 목록 (OrderListResponse) |

**필터 조건:**
- 상태: ALL / NEW / PAID / PREPARING / SHIPPED / CANCELLED
- 풀필먼트: ALL / WMS / DIRECT
- 검색: 주문번호, 고객명, 배정정보

**API:**
```
GET /api/orders
Query: status, fulfillmentMethod, search, page, size
```

#### 3.1.2 주문 상세 조회

| 항목 | 내용 |
|------|------|
| **기능 ID** | ORD-002 |
| **설명** | 단일 주문의 상세 정보 조회 |
| **입력** | 주문 ID |
| **출력** | OrderDetailResponse |

**API:**
```
GET /api/orders/{orderId}
```

#### 3.1.3 주문 상태 변경

| 항목 | 내용 |
|------|------|
| **기능 ID** | ORD-003 |
| **설명** | 주문 상태를 다음 단계로 변경 |
| **입력** | 주문 ID, 새 상태 |
| **비즈니스 규칙** | 상태 전이 규칙 준수 |

**주문 상태 전이 규칙:**
```
NEW → PAYMENT_PENDING → PAID → PREPARING → READY_TO_SHIP → SHIPPED → IN_DELIVERY → DELIVERED
                         ↓
                    CANCELLED

DELIVERED → EXCHANGE_REQUESTED
DELIVERED → RETURN_REQUESTED
```

**API:**
```
PATCH /api/orders/{orderId}/status
Body: { status: OrderStatus }
```

#### 3.1.4 일괄 주문 처리

| 항목 | 내용 |
|------|------|
| **기능 ID** | ORD-004 |
| **설명** | 여러 주문에 대한 일괄 작업 |
| **작업 종류** | 송장 출력, 출고 지시, 상태 변경 |

**API:**
```
POST /api/orders/bulk
Body: { orderIds: [], action: 'PRINT_INVOICE' | 'DISPATCH' | 'CHANGE_STATUS', params: {} }
```

---

### 3.2 상품 관리 (Product Management)

#### 3.2.1 상품 마스터 목록

| 항목 | 내용 |
|------|------|
| **기능 ID** | PRD-001 |
| **설명** | 상품 마스터 목록 조회 |
| **입력** | 검색어, 카테고리, 상태, 페이지네이션 |

**API:**
```
GET /api/products
Query: search, category, status, page, size
```

#### 3.2.2 상품 등록

| 항목 | 내용 |
|------|------|
| **기능 ID** | PRD-002 |
| **설명** | 신규 상품 마스터 등록 |
| **필수 필드** | SKU, 상품명(다국어), 카테고리, UOM |

**API:**
```
POST /api/products
Body: ProductCreateRequest
```

#### 3.2.3 상품 상세/수정

| 항목 | 내용 |
|------|------|
| **기능 ID** | PRD-003 |
| **설명** | 상품 상세 조회 및 수정 |
| **탭 구성** | 기본정보, 물류정보, 통관정보 |

**API:**
```
GET /api/products/{productId}
PUT /api/products/{productId}
```

#### 3.2.4 바코드 관리

| 항목 | 내용 |
|------|------|
| **기능 ID** | PRD-004 |
| **설명** | 상품별 다중 바코드 관리 |
| **규칙** | 메인 바코드는 1개만 가능 |

#### 3.2.5 통관 전략 관리

| 항목 | 내용 |
|------|------|
| **기능 ID** | PRD-005 |
| **설명** | 국가별 통관 전략 설정 |
| **필드** | countryCode, localHsCode, invoiceName, dutyRate, requiredDocs |

---

### 3.3 재고 관리 (Inventory Management)

#### 3.3.1 재고 현황 조회

| 항목 | 내용 |
|------|------|
| **기능 ID** | INV-001 |
| **설명** | 창고별/채널별 재고 현황 조회 |
| **필터** | 전체 창고 / 개별 채널 |

**핵심 표시 정보:**
- 물리 재고 (total, available, reserved)
- 채널별 할당량 (channelBreakdown)
- 상태 (NORMAL/LOW/OUT_OF_STOCK/OVERSTOCK)

**API:**
```
GET /api/inventory
Query: warehouseId, channelId, status
```

#### 3.3.2 재고 조정

| 항목 | 내용 |
|------|------|
| **기능 ID** | INV-002 |
| **설명** | 수동 재고 수량 조정 |
| **입력** | productId, warehouseId, adjustmentType, quantity, reason |

**API:**
```
POST /api/inventory/adjust
Body: { productId, warehouseId, type: 'ADD' | 'SUBTRACT' | 'SET', quantity, reason }
```

#### 3.3.3 채널별 재고 할당

| 항목 | 내용 |
|------|------|
| **기능 ID** | INV-003 |
| **설명** | 물리 재고를 채널별로 할당 |
| **규칙** | 할당 합계 ≤ 가용 재고 |

**API:**
```
PUT /api/inventory/{productId}/channel-allocation
Body: { allocations: { channelId: quantity } }
```

#### 3.3.4 재고 히스토리

| 항목 | 내용 |
|------|------|
| **기능 ID** | INV-004 |
| **설명** | 재고 변동 이력 조회 |

**API:**
```
GET /api/inventory/{productId}/history
Query: startDate, endDate, type
```

---

### 3.4 채널 관리 (Channel Management)

#### 3.4.1 채널 목록

| 항목 | 내용 |
|------|------|
| **기능 ID** | CHN-001 |
| **설명** | 연동된 판매 채널 목록 |
| **채널 타입** | D2C, Market, Global |

**API:**
```
GET /api/channels
```

#### 3.4.2 채널 연동 상태

| 항목 | 내용 |
|------|------|
| **기능 ID** | CHN-002 |
| **상태** | CONNECTED / DISCONNECTED / ERROR |
| **정보** | 마지막 동기화 시간, API 인증 상태 |

#### 3.4.3 채널 데이터 동기화

| 항목 | 내용 |
|------|------|
| **기능 ID** | CHN-003 |
| **설명** | 채널 API를 통한 데이터 갱신 |

**API:**
```
POST /api/channels/{channelId}/sync
```

---

### 3.5 창고 관리 (Warehouse Management)

#### 3.5.1 창고 목록

| 항목 | 내용 |
|------|------|
| **기능 ID** | WHS-001 |
| **설명** | 물류 거점 목록 |
| **타입** | AUTO(자동화), MEGA(메가), HUB(허브), AIR(항공) |

**API:**
```
GET /api/warehouses
```

#### 3.5.2 창고 가동률

| 항목 | 내용 |
|------|------|
| **기능 ID** | WHS-002 |
| **설명** | 창고별 가동률 및 처리 현황 |

---

### 3.6 채널-창고 매핑 (Logistics Mesh)

#### 3.6.1 매핑 토폴로지 조회

| 항목 | 내용 |
|------|------|
| **기능 ID** | MAP-001 |
| **설명** | 채널별 연결된 창고 토폴로지 조회 |

**API:**
```
GET /api/channel-warehouse-mappings
Query: channelId
```

#### 3.6.2 매핑 생성/수정

| 항목 | 내용 |
|------|------|
| **기능 ID** | MAP-002 |
| **설명** | 채널-창고 연결 및 역할 설정 |
| **역할** | PRIMARY(주), REGIONAL(권역), BACKUP(백업) |

**API:**
```
POST /api/channel-warehouse-mappings
Body: { channelId, warehouseId, role, priority }

PUT /api/channel-warehouse-mappings/{mappingId}
```

#### 3.6.3 동적 부하 분산 설정

| 항목 | 내용 |
|------|------|
| **기능 ID** | MAP-003 |
| **설명** | AI 기반 실시간 부하 분산 활성화 |

**API:**
```
PUT /api/channel-warehouse-mappings/load-balancing
Body: { channelId, enabled: boolean }
```

---

### 3.7 상품-채널 매핑 (Product Mapping)

#### 3.7.1 매핑 목록 조회

| 항목 | 내용 |
|------|------|
| **기능 ID** | PMM-001 |
| **설명** | 상품별 채널 매핑 상태 조회 |
| **대용량** | 24,500+ 상품 지원 |

**API:**
```
GET /api/product-channel-mappings
Query: search, page, size
```

#### 3.7.2 일괄 매핑

| 항목 | 내용 |
|------|------|
| **기능 ID** | PMM-002 |
| **설명** | 여러 상품을 특정 채널에 일괄 매핑 |

**API:**
```
POST /api/product-channel-mappings/bulk
Body: { productIds: [], channelId, action: 'MAP' | 'UNMAP' }
```

---

### 3.8 배송 관리 (Shipping Management)

#### 3.8.1 배송 목록

| 항목 | 내용 |
|------|------|
| **기능 ID** | SHP-001 |
| **설명** | 배송 현황 목록 |
| **상태** | 출고대기, 배송중, 배송완료 |

**API:**
```
GET /api/shipments
Query: status, startDate, endDate
```

#### 3.8.2 송장 발행

| 항목 | 내용 |
|------|------|
| **기능 ID** | SHP-002 |
| **설명** | 택배사 연동 송장 발행 |

**API:**
```
POST /api/shipments/{shipmentId}/invoice
```

#### 3.8.3 배송 추적

| 항목 | 내용 |
|------|------|
| **기능 ID** | SHP-003 |
| **설명** | 택배사 API 연동 실시간 추적 |

**API:**
```
GET /api/shipments/{shipmentId}/tracking
```

---

### 3.9 클레임 관리 (Claims Management)

#### 3.9.1 클레임 목록

| 항목 | 내용 |
|------|------|
| **기능 ID** | CLM-001 |
| **타입** | 취소, 반품, 교환 |
| **우선순위** | URGENT, NORMAL |

**API:**
```
GET /api/claims
Query: type, status, priority
```

#### 3.9.2 클레임 처리

| 항목 | 내용 |
|------|------|
| **기능 ID** | CLM-002 |
| **상태** | 접수대기 → 처리중 → 완료 |

**API:**
```
PATCH /api/claims/{claimId}/status
```

---

### 3.10 정산 관리 (Settlement)

#### 3.10.1 정산 현황

| 항목 | 내용 |
|------|------|
| **기능 ID** | STL-001 |
| **항목** | 총 판매금액, 채널 수수료, 실 정산액 |

**API:**
```
GET /api/settlements
Query: period (YYYY-MM)
```

#### 3.10.2 채널별 정산 상세

| 항목 | 내용 |
|------|------|
| **기능 ID** | STL-002 |
| **설명** | 채널별 매출/수수료/정산액 |

**API:**
```
GET /api/settlements/channels/{channelId}
Query: period
```

---

### 3.11 자동화 (Automation)

#### 3.11.1 자동화 규칙 목록

| 항목 | 내용 |
|------|------|
| **기능 ID** | AUT-001 |
| **트리거** | 신규주문, 재고변경, 배송시작, 결제실패 |
| **액션** | Slack알림, 이메일, 상태변경, 재고조정 |

**API:**
```
GET /api/automation/rules
```

#### 3.11.2 규칙 생성/수정

| 항목 | 내용 |
|------|------|
| **기능 ID** | AUT-002 |
| **구조** | IF (trigger + condition) THEN (action) |

**API:**
```
POST /api/automation/rules
PUT /api/automation/rules/{ruleId}
```

#### 3.11.3 규칙 활성화/비활성화

| 항목 | 내용 |
|------|------|
| **기능 ID** | AUT-003 |

**API:**
```
PATCH /api/automation/rules/{ruleId}/toggle
```

---

### 3.12 인터페이스/웹훅 (Interface)

#### 3.12.1 웹훅 로그 조회

| 항목 | 내용 |
|------|------|
| **기능 ID** | INT-001 |
| **정보** | 시간, 상태코드, 이벤트타입, 소스, 지연시간 |

**API:**
```
GET /api/webhooks/logs
Query: status, eventType, startDate, endDate
```

#### 3.12.2 웹훅 재시도

| 항목 | 내용 |
|------|------|
| **기능 ID** | INT-002 |
| **설명** | 실패한 웹훅 재전송 |

**API:**
```
POST /api/webhooks/logs/{logId}/retry
```

#### 3.12.3 웹훅 시뮬레이터

| 항목 | 내용 |
|------|------|
| **기능 ID** | INT-003 |
| **설명** | 테스트 페이로드 전송 |

**API:**
```
POST /api/webhooks/simulate
Body: { eventType, payload }
```

---

### 3.13 전략 센터 (Strategy Intelligence)

#### 3.13.1 운영 시뮬레이터

| 항목 | 내용 |
|------|------|
| **기능 ID** | STR-001 |
| **가중치** | 물류비용, 리드타임, 재고분산, 탄소배출 |
| **출력** | 효율성 점수, 비용 절감액, 평균 리드타임 |

**API:**
```
POST /api/strategy/simulate
Body: { weights: { costReduction, leadTime, stockBalance, carbonEmission } }
```

#### 3.13.2 전략 배포

| 항목 | 내용 |
|------|------|
| **기능 ID** | STR-002 |
| **설명** | 시뮬레이션 결과를 실제 시스템에 적용 |

**API:**
```
POST /api/strategy/deploy
```

#### 3.13.3 글로벌 진출 준비도

| 항목 | 내용 |
|------|------|
| **기능 ID** | STR-003 |
| **항목** | HS Code 매핑율, 인증 현황, 물류 노드 |

**API:**
```
GET /api/strategy/global-readiness
Query: countryCode
```

---

### 3.14 사용자 및 설정 (Settings)

#### 3.14.1 사용자 관리

| 항목 | 내용 |
|------|------|
| **기능 ID** | SET-001 |
| **역할** | Owner, Editor, Viewer |

**API:**
```
GET /api/users
POST /api/users/invite
```

#### 3.14.2 알림 설정

| 항목 | 내용 |
|------|------|
| **기능 ID** | SET-002 |
| **채널** | 이메일, Slack, 모바일 푸시 |

**API:**
```
GET /api/settings/notifications
PUT /api/settings/notifications
```

---

## 4. 사용자 시나리오

### 4.1 주문 처리 플로우

```
1. [웹훅 수신] 네이버에서 신규 주문 수신
   → POST /webhooks/orders (외부 → OMS)

2. [주문 생성] 주문 데이터 파싱 및 저장
   → Order 엔티티 생성 (status: NEW)

3. [풀필먼트 결정] fulfillmentMethod 판단
   → 채널 설정 기반 WMS/DIRECT 결정

4. [WMS 라우팅] (WMS인 경우)
   → MappingView 규칙에 따라 최적 창고 선택
   → wmsNode = "용인 메가 허브 (CJ)"

5. [상태 진행]
   → NEW → PAID → PREPARING → READY_TO_SHIP

6. [송장 발행]
   → 택배사 API 연동, 송장번호 발급

7. [출고]
   → SHIPPED 상태 변경
   → 재고 차감 (reserved → 0)

8. [배송 추적]
   → IN_DELIVERY → DELIVERED

9. [정산 반영]
   → 채널별 정산 데이터 집계
```

### 4.2 재고 관리 플로우

```
1. [상품 등록]
   → ProductView에서 신규 상품 생성

2. [입고 처리]
   → InventoryView에서 창고별 입고 처리
   → total += 입고수량, available += 입고수량

3. [채널 할당]
   → channelBreakdown 설정
   → 자사몰: 400, 네이버: 250, 쿠팡: 150

4. [주문 발생 시]
   → available -= 주문수량
   → reserved += 주문수량

5. [재고 부족 감지]
   → available < safetyStock
   → status = LOW
   → 자동화 규칙 실행 (Slack 알림)

6. [출고 완료 시]
   → reserved -= 출고수량
```

### 4.3 글로벌 진출 플로우

```
1. [국가 선택]
   → I18nView에서 "USA" 선택

2. [준비도 확인]
   → HS Code 매핑률, 인증 현황, 물류 노드 확인

3. [상품 통관 전략 설정]
   → ProductDetailModal에서 customsStrategies 추가

4. [채널 매핑]
   → ProductMappingView에서 "아마존 US"에 상품 매핑

5. [물류 연결]
   → MappingView에서 "아마존 US" → "인천 항공 센터" 연결

6. [주문 처리]
   → 미국 주문 수신 시 자동 통관 서류 생성
```

---

## 5. 비기능 요구사항

### 5.1 성능

| 항목 | 요구사항 |
|------|---------|
| API 응답시간 | p95 < 200ms |
| 동시 사용자 | 1,000명 |
| 일일 주문 처리량 | 100,000건 |
| 상품 SKU 수 | 100,000+ |

### 5.2 가용성

| 항목 | 요구사항 |
|------|---------|
| 서비스 가용률 | 99.9% |
| RTO (복구 목표 시간) | 1시간 |
| RPO (복구 목표 시점) | 15분 |

### 5.3 보안

| 항목 | 요구사항 |
|------|---------|
| 인증 | JWT 기반 토큰 인증 |
| 권한 | RBAC (역할 기반 접근 제어) |
| 데이터 암호화 | TLS 1.3, AES-256 |
| 감사 로그 | 모든 CUD 작업 기록 |

---

## 6. 다음 단계

### Phase 1: 도메인 모델 설계
- [ ] DDD Bounded Context 정의
- [ ] Aggregate Root 식별
- [ ] 도메인 이벤트 정의

### Phase 2: ERD 설계
- [ ] MySQL 스키마 (핵심 도메인)
- [ ] MongoDB 컬렉션 (로그/이벤트)
- [ ] 인덱스 전략

### Phase 3: API 설계
- [ ] OpenAPI 3.0 스펙 작성
- [ ] 에러 코드 정의
- [ ] 페이지네이션/정렬 규칙

---

## 부록

### A. 용어 정의

| 용어 | 정의 |
|------|------|
| SKU | Stock Keeping Unit, 재고 관리 단위 |
| WMS | Warehouse Management System |
| FBA | Fulfillment by Amazon |
| HS Code | Harmonized System Code, 국제 통일 상품 분류 코드 |
| D2C | Direct to Consumer |

### B. 주문 상태 코드

| 코드 | 한글명 | 설명 |
|------|--------|------|
| NEW | 신규주문 | 주문 접수됨 |
| PAYMENT_PENDING | 결제대기 | 결제 진행 중 |
| PAID | 결제완료 | 결제 확인됨 |
| PREPARING | 상품준비중 | 피킹/패킹 진행 |
| READY_TO_SHIP | 출고대기 | 출고 준비 완료 |
| SHIPPED | 출고완료 | 택배사 인계 |
| IN_DELIVERY | 배송중 | 배송 진행 중 |
| DELIVERED | 배송완료 | 고객 수령 |
| CANCELLED | 주문취소 | 주문 취소됨 |
| EXCHANGE_REQUESTED | 교환요청 | 교환 접수 |
| RETURN_REQUESTED | 반품요청 | 반품 접수 |

### C. 채널 타입

| 타입 | 설명 | 예시 |
|------|------|------|
| D2C | 자사 직영 채널 | 자사몰 |
| Market | 국내 마켓플레이스 | 네이버, 쿠팡, 11번가 |
| Global | 해외 마켓플레이스 | 아마존, 쇼피, 라자다 |

### D. 창고 타입

| 타입 | 설명 | 예시 |
|------|------|------|
| AUTO | 자동화 센터 | 김포 자동화 센터 |
| MEGA | 메가 허브 | 용인 메가 허브 |
| HUB | 권역 허브 | 칠곡 허브 |
| AIR | 항공 물류 | 인천 항공 센터 |

