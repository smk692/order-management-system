# API DTO 명세서

**버전**: 1.0
**작성일**: 2025-01-18

---

## 1. 개요

이 문서는 프론트엔드 타입(`frontend/types.ts`)과 백엔드 엔티티 간의 데이터 변환 규칙을 정의합니다.

### 1.1 변환 레이어 구조

```
Frontend (TypeScript)          API Layer              Backend (Kotlin)
─────────────────────────────────────────────────────────────────────
types.ts                       DTO                    Entity
  Product         ←─────→    ProductDTO      ←─────→   Product
  Inventory       ←─────→    StockDTO        ←─────→   Stock
  Order           ←─────→    OrderDTO        ←─────→   Order
```

### 1.2 명명 규칙

| 구분 | 프론트엔드 | API DTO | 백엔드 Entity |
|------|-----------|---------|---------------|
| 파일명 | `types.ts` | `*Request.kt`, `*Response.kt` | `*.kt` (도메인) |
| 케이스 | camelCase | camelCase (JSON) | camelCase (코드), snake_case (DB) |
| Enum | 문자열 리터럴 | Enum (JSON string) | Kotlin enum |

---

## 2. 도메인별 DTO 매핑

### 2.1 Product (상품)

#### 2.1.1 타입 비교

| Frontend (`Product`) | API DTO | Backend Entity | 비고 |
|---------------------|---------|----------------|------|
| `id: string` | `id: String` | `id: ProductId` | Value Object 래핑 |
| `sku: string` | `sku: String` | `sku: Sku` | Value Object 래핑 |
| `name: { [key: string]: string }` | `name: LocalizedStringDTO` | `name: LocalizedString` | 구조 변환 |
| `brand: string` | `brand: String?` | `brand: String` | nullable 차이 |
| `category: string` | `categoryPath: String` | `category: Category` | 필드명 변경 |
| `uom: 'PCS' \| 'SET' \| 'BOX' \| 'KG' \| '台' \| 'EA'` | `uom: UnitOfMeasure` | `uom: UnitOfMeasure` | **'台' → TAI 변환** |
| `color?: string` | `color: String?` | `color: String?` | 동일 |
| `ownerName?: string` | `ownerName: String?` | `ownerName: String?` | 동일 |
| `customerGoodsNo?: string` | `customerGoodsNo: String?` | `customerGoodsNo: String?` | 동일 |
| `barcodes: BarcodeInfo[]` | `barcodes: List<BarcodeDTO>` | `barcodes: List<Barcode>` | 배열 구조 동일 |
| `basePrice: number` | `basePrice: MoneyDTO` | `basePrice: Money` | Money VO 변환 |
| `totalStock: number` | `totalStock: Int` | (Inventory에서 조회) | **계산 필드** |
| `status: 'ACTIVE' \| 'INACTIVE' \| 'OUT_OF_STOCK'` | `status: ProductStatus` | `status: ProductStatus` | Enum 동일 |
| `dimensions: {...}` | `dimensions: DimensionsDTO?` | `dimensions: Dimensions?` | VO 구조 동일 |
| `netWeight: number` | `weight: WeightDTO?` | `weight: Weight?` | **구조 변환** |
| `grossWeight: number` | ↑ (weight에 포함) | ↑ | 통합 VO |
| `logisticsInfo: {...}` | `logisticsInfo: LogisticsInfoDTO` | `logisticsInfo: LogisticsInfo` | **Enum 케이스 변환** |
| `hsCode: string` | `hsCode: String?` | `hsCode: String?` | 동일 |
| `countryOfOrigin: string` | `countryOfOrigin: String?` | `countryOfOrigin: String?` | 동일 |
| `materialContent: string` | `material: String?` | `material: String?` | 필드명 변경 |
| `manufacturerDetails: {...}` | `manufacturer: ManufacturerDTO?` | embedded fields | 구조 평탄화 |
| `customsStrategies: CustomsStrategy[]` | `customsStrategies: List<CustomsStrategyDTO>` | `customsStrategies: List<CustomsStrategy>` | 동일 구조 |

#### 2.1.2 UOM 변환 규칙

```kotlin
// Frontend → Backend 변환
fun toBackendUom(frontendUom: String): UnitOfMeasure {
    return when (frontendUom) {
        "PCS" -> UnitOfMeasure.PCS
        "SET" -> UnitOfMeasure.SET
        "BOX" -> UnitOfMeasure.BOX
        "KG" -> UnitOfMeasure.KG
        "EA" -> UnitOfMeasure.EA
        "台" -> UnitOfMeasure.TAI  // 특수 케이스
        else -> throw IllegalArgumentException("Unknown UOM: $frontendUom")
    }
}

// Backend → Frontend 변환
fun toFrontendUom(backendUom: UnitOfMeasure): String {
    return when (backendUom) {
        UnitOfMeasure.TAI -> "台"  // 특수 케이스
        else -> backendUom.name
    }
}
```

#### 2.1.3 LogisticsInfo tempMgmt 변환

```kotlin
// Frontend: 'Normal' | 'Temperature Control' | 'Cold' | 'Freezing' | 'Cryogenic'
// Backend: NORMAL | TEMPERATURE_CONTROL | COLD | FREEZING | CRYOGENIC

fun toBackendTempType(frontendTemp: String): TemperatureType {
    return when (frontendTemp) {
        "Normal" -> TemperatureType.NORMAL
        "Temperature Control" -> TemperatureType.TEMPERATURE_CONTROL
        "Cold" -> TemperatureType.COLD
        "Freezing" -> TemperatureType.FREEZING
        "Cryogenic" -> TemperatureType.CRYOGENIC
        else -> TemperatureType.NORMAL
    }
}

fun toFrontendTempType(backendTemp: TemperatureType): String {
    return when (backendTemp) {
        TemperatureType.NORMAL -> "Normal"
        TemperatureType.TEMPERATURE_CONTROL -> "Temperature Control"
        TemperatureType.COLD -> "Cold"
        TemperatureType.FREEZING -> "Freezing"
        TemperatureType.CRYOGENIC -> "Cryogenic"
    }
}
```

#### 2.1.4 Product DTO 정의

```kotlin
// === Request DTO ===
data class ProductCreateRequest(
    val sku: String,
    val name: LocalizedStringDTO,
    val brand: String?,
    val categoryPath: String,
    val uom: String,                      // "PCS", "台" 등
    val basePrice: MoneyDTO,
    val color: String?,
    val ownerName: String?,
    val customerGoodsNo: String?,
    val barcodes: List<BarcodeDTO> = emptyList(),
    val dimensions: DimensionsDTO?,
    val weight: WeightDTO?,
    val logisticsInfo: LogisticsInfoDTO,
    val hsCode: String?,
    val countryOfOrigin: String?,
    val material: String?,
    val manufacturerName: String?,
    val manufacturerAddress: String?,
    val customsStrategies: List<CustomsStrategyDTO> = emptyList()
)

// === Response DTO ===
data class ProductResponse(
    val id: String,
    val sku: String,
    val name: Map<String, String>,        // { "ko": "...", "en": "..." }
    val brand: String?,
    val category: String,                 // categoryPath
    val uom: String,                      // "PCS", "台" 등
    val color: String?,
    val ownerName: String?,
    val customerGoodsNo: String?,
    val barcodes: List<BarcodeDTO>,
    val basePrice: Double,
    val totalStock: Int,                  // 계산 필드 (Inventory 합계)
    val status: String,                   // "ACTIVE" 등
    val dimensions: DimensionsDTO?,
    val netWeight: Double?,
    val grossWeight: Double?,
    val logisticsInfo: LogisticsInfoDTO,
    val hsCode: String?,
    val countryOfOrigin: String?,
    val materialContent: String?,
    val manufacturerDetails: ManufacturerDTO?,
    val customsStrategies: List<CustomsStrategyDTO>
)

// === Sub DTOs ===
data class LocalizedStringDTO(
    val ko: String,
    val en: String? = null
)

data class BarcodeDTO(
    val code: String,
    val isMain: Boolean
)

data class DimensionsDTO(
    val width: Double,
    val length: Double,
    val height: Double,
    val unit: String   // "cm" | "mm"
)

data class WeightDTO(
    val net: Double,
    val gross: Double
)

data class LogisticsInfoDTO(
    val tempMgmt: String,           // "Normal" | "Temperature Control" 등
    val shelfLifeMgmt: Boolean,
    val snMgmt: Boolean,
    val isDangerous: Boolean,
    val isFragile: Boolean,
    val isHighValue: Boolean,
    val isNonStandard: Boolean
)

data class ManufacturerDTO(
    val name: String,
    val address: String
)

data class CustomsStrategyDTO(
    val countryCode: String,
    val localHsCode: String,
    val invoiceName: String,
    val dutyRate: String,
    val requiredDocs: List<String>,
    val complianceAlert: String?
)

data class MoneyDTO(
    val amount: Double,
    val currency: String = "KRW"
)
```

---

### 2.2 Inventory (재고) → Stock

#### 2.2.1 **이름 변경 주의**

| Frontend | Backend | 설명 |
|----------|---------|------|
| `Inventory` | `Stock` | **도메인 명칭 차이** |

프론트엔드에서는 `Inventory`라는 용어를 사용하지만, 백엔드 DDD 모델에서는 `Stock`으로 명명합니다.
API 엔드포인트에서는 `/api/inventory`를 유지하되, 내부적으로 `Stock` 엔티티를 사용합니다.

#### 2.2.2 타입 비교

| Frontend (`Inventory`) | API DTO | Backend (`Stock`) | 비고 |
|-----------------------|---------|-------------------|------|
| `productId: string` | `productId: String` | `productId: ProductId` | VO 래핑 |
| `productName: string` | `productName: String` | (Product에서 조회) | **조인 필드** |
| `warehouse: string` | `warehouseId: String` | `warehouseId: WarehouseId` | ID로 변경 |
| - | `warehouseName: String` | (Warehouse에서 조회) | **조인 필드** |
| `total: number` | `total: Int` | `total: Int` | 동일 |
| `available: number` | `available: Int` | `available: Int` | 동일 |
| `reserved: number` | `reserved: Int` | `reserved: Int` | 동일 |
| `safetyStock: number` | `safetyStock: Int` | `safetyStock: Int` | 동일 |
| `status: StockStatus` | `status: String` | `status: StockStatus` | Enum 동일 |
| `channelBreakdown: Record<string, number>` | `channelAllocations: Map<String, Int>` | `channelAllocations: Map<ChannelId, Int>` | 구조 동일 |

#### 2.2.3 Inventory DTO 정의

```kotlin
// === Response DTO (조회) ===
data class InventoryResponse(
    val id: String,
    val productId: String,
    val productName: String,             // 조인된 상품명
    val warehouse: String,               // warehouseName (프론트 호환)
    val warehouseId: String,             // 추가 정보
    val total: Int,
    val available: Int,
    val reserved: Int,
    val safetyStock: Int,
    val status: String,                  // "NORMAL", "LOW", "OUT_OF_STOCK", "OVERSTOCK"
    val channelBreakdown: Map<String, Int>  // { "CH-001": 100, "CH-002": 50 }
)

// === Request DTO (재고 조정) ===
data class StockAdjustRequest(
    val productId: String,
    val warehouseId: String,
    val newTotal: Int,
    val reason: String
)

// === Request DTO (채널 할당) ===
data class ChannelAllocationRequest(
    val channelId: String,
    val quantity: Int
)
```

---

### 2.3 Order (주문)

#### 2.3.1 타입 비교

| Frontend (`Order`) | API DTO | Backend (`Order`) | 비고 |
|-------------------|---------|-------------------|------|
| `id: string` | `id: String` | `id: OrderId` | VO 래핑 |
| `channel: string` | `channelId: String` | `channelId: ChannelId` | ID 필드 |
| - | `channelName: String` | (Channel에서 조회) | **조인 필드** |
| `orderDate: string` | `orderDate: String` (ISO 8601) | `orderDate: Instant` | **포맷 변환** |
| `customerName: string` | `customer: CustomerDTO` | `customer: Customer` | **구조 확장** |
| `totalAmount: number` | `totalAmount: MoneyDTO` | `totalAmount: Money` | Money VO |
| `status: OrderStatus` | `status: String` | `status: OrderStatus` | Enum 동일 |
| `fulfillmentMethod?: 'WMS' \| 'DIRECT'` | `fulfillmentMethod: String` | `fulfillmentMethod: FulfillmentMethod` | Enum 변환 |
| `wmsNode?: string` | `warehouseId: String?` | `assignedWarehouseId: WarehouseId?` | **필드명 변경** |
| `routingLogic?: string` | `routingLogic: String?` | `routingLogic: String?` | 동일 |
| `items: Array<...>` | `items: List<OrderItemDTO>` | `items: List<OrderItem>` | 구조 변환 |

#### 2.3.2 OrderItem 변환 (중요)

| Frontend (`items[].price`) | API DTO | Backend | 비고 |
|---------------------------|---------|---------|------|
| `price: number` | `unitPrice: Double` | `unitPrice: Money` | **단가로 해석** |
| - | `totalPrice: Double` | `totalPrice: Money` | **계산 필드 추가** |

**변환 규칙**: Frontend의 `price`는 단가(`unitPrice`)로 해석하며, `totalPrice = unitPrice * quantity`로 계산합니다.

#### 2.3.3 Order DTO 정의

```kotlin
// === Response DTO ===
data class OrderResponse(
    val id: String,
    val channel: String,                  // channelName (프론트 호환)
    val channelId: String,
    val orderDate: String,                // ISO 8601: "2025-01-18T10:30:00Z"
    val customerName: String,             // customer.name
    val totalAmount: Double,
    val currency: String,
    val status: String,                   // "NEW", "PAID" 등
    val fulfillmentMethod: String?,       // "WMS" | "DIRECT"
    val wmsNode: String?,                 // warehouseName (프론트 호환)
    val warehouseId: String?,
    val routingLogic: String?,
    val items: List<OrderItemDTO>
)

data class OrderItemDTO(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,                    // unitPrice (프론트 호환)
    val unitPrice: Double,
    val totalPrice: Double
)

// === Request DTO ===
data class OrderCreateRequest(
    val channelId: String,
    val externalOrderId: String?,
    val customer: CustomerDTO,
    val shippingAddress: AddressDTO,
    val fulfillmentMethod: String = "WMS",
    val items: List<OrderItemCreateDTO>
)

data class CustomerDTO(
    val name: String,
    val phone: String,
    val email: String?
)

data class AddressDTO(
    val zipCode: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val country: String = "KR"
)

data class OrderItemCreateDTO(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double
)
```

---

### 2.4 Channel (채널)

#### 2.4.1 타입 비교

| Frontend (`Channel`) | API DTO | Backend (`Channel`) | 비고 |
|---------------------|---------|---------------------|------|
| `id: string` | `id: String` | `id: ChannelId` | VO 래핑 |
| `name: string` | `name: String` | `name: String` | 동일 |
| `type: string` | `type: String` | `type: ChannelType` | **Enum 타입** |
| `status: 'CONNECTED' \| 'DISCONNECTED' \| 'ERROR'` | `status: String` | `status: ChannelStatus` | Enum 동일 |
| `lastSync: string` | `lastSync: String?` | `lastSyncAt: Instant?` | **포맷 변환** |
| `logo: string` | `logo: String?` | `logoEmoji: String?` | 필드명 차이 |

#### 2.4.2 Channel.type 변환

```kotlin
// Frontend: 임의의 문자열 (예: "마켓플레이스", "D2C", "글로벌")
// Backend: D2C | MARKET | GLOBAL

fun toBackendChannelType(frontendType: String): ChannelType {
    return when (frontendType.uppercase()) {
        "D2C", "자사몰" -> ChannelType.D2C
        "MARKET", "마켓", "마켓플레이스" -> ChannelType.MARKET
        "GLOBAL", "글로벌", "해외" -> ChannelType.GLOBAL
        else -> ChannelType.MARKET  // 기본값
    }
}
```

#### 2.4.3 Channel DTO 정의

```kotlin
data class ChannelResponse(
    val id: String,
    val name: String,
    val type: String,                     // "D2C", "MARKET", "GLOBAL"
    val status: String,                   // "CONNECTED", "DISCONNECTED", "ERROR"
    val lastSync: String?,                // ISO 8601
    val logo: String?                     // 이모지 또는 URL
)

data class ChannelCreateRequest(
    val name: String,
    val type: String,
    val apiKey: String?,
    val secretKey: String?
)
```

---

### 2.5 Warehouse (창고)

#### 2.5.1 타입 비교

| 필드 | API DTO | Backend Entity | 비고 |
|------|---------|----------------|------|
| `id` | `String` | `WarehouseId` | VO 래핑 |
| `name` | `String` | `String` | 동일 |
| `region` | `String` | `String` | 동일 |
| `type` | `String` | `WarehouseType` | Enum: AUTO, MEGA, HUB, AIR |
| `capacity` | `Int` | `Int` | 가동률 (%) |
| `status` | `String` | `WarehouseStatus` | Enum: ACTIVE, MAINTENANCE, INACTIVE |
| `address` | `AddressDTO` | `Address` | VO 구조 동일 |

#### 2.5.2 Warehouse DTO 정의

```kotlin
data class WarehouseResponse(
    val id: String,
    val name: String,
    val region: String,                   // "수도권", "영남권", "Global"
    val type: String,                     // "AUTO", "MEGA", "HUB", "AIR"
    val capacity: Int,                    // 가동률 (%)
    val status: String,                   // "ACTIVE", "MAINTENANCE", "INACTIVE"
    val address: AddressDTO?
)

data class WarehouseCreateRequest(
    val name: String,
    val region: String,
    val type: String,
    val capacity: Int = 0,
    val address: AddressDTO?
)

data class ChannelWarehouseMappingDTO(
    val id: String,
    val channelId: String,
    val channelName: String,
    val warehouseId: String,
    val warehouseName: String,
    val role: String,                     // "PRIMARY", "REGIONAL", "BACKUP"
    val priority: Int,
    val isActive: Boolean,
    val loadBalancingEnabled: Boolean
)
```

---

### 2.6 Claim (클레임)

#### 2.6.1 타입 비교

| 필드 | API DTO | Backend Entity | 비고 |
|------|---------|----------------|------|
| `id` | `String` | `ClaimId` | VO 래핑 |
| `orderId` | `String` | `OrderId` | 주문 참조 |
| `type` | `String` | `ClaimType` | Enum: CANCEL, RETURN, EXCHANGE |
| `status` | `String` | `ClaimStatus` | Enum: PENDING, PROCESSING, COMPLETED, REJECTED |
| `reason` | `String` | `String` | 클레임 사유 |
| `priority` | `String` | `ClaimPriority` | Enum: URGENT, NORMAL |
| `refundAmount` | `MoneyDTO?` | `Money?` | 환불 금액 |
| `items` | `List<ClaimItemDTO>` | `List<ClaimItem>` | 클레임 상품 |

#### 2.6.2 Claim DTO 정의

```kotlin
data class ClaimResponse(
    val id: String,
    val orderId: String,
    val orderNumber: String,              // 주문번호 (조인)
    val type: String,                     // "CANCEL", "RETURN", "EXCHANGE"
    val status: String,                   // "PENDING", "PROCESSING", "COMPLETED", "REJECTED"
    val reason: String,
    val memo: String?,
    val priority: String,                 // "URGENT", "NORMAL"
    val refundAmount: Double?,
    val currency: String?,
    val refundedAt: String?,              // ISO 8601
    val items: List<ClaimItemDTO>,
    val createdAt: String,
    val processedAt: String?
)

data class ClaimItemDTO(
    val productId: String,
    val productName: String,              // 조인 필드
    val quantity: Int,
    val reason: String?
)

data class ClaimCreateRequest(
    val orderId: String,
    val type: String,                     // "CANCEL", "RETURN", "EXCHANGE"
    val reason: String,
    val priority: String = "NORMAL",
    val items: List<ClaimItemCreateDTO>
)

data class ClaimItemCreateDTO(
    val productId: String,
    val quantity: Int,
    val reason: String?
)

data class ClaimProcessRequest(
    val action: String,                   // "APPROVE", "REJECT"
    val refundAmount: Double?,
    val memo: String?
)
```

---

### 2.7 Settlement (정산)

#### 2.7.1 타입 비교

| 필드 | API DTO | Backend Entity | 비고 |
|------|---------|----------------|------|
| `id` | `String` | `SettlementId` | VO 래핑 |
| `channelId` | `String` | `ChannelId` | 채널 참조 |
| `period` | `PeriodDTO` | `SettlementPeriod` | 정산 기간 |
| `totalSales` | `MoneyDTO` | `Money` | 총 판매금액 |
| `totalCommission` | `MoneyDTO` | `Money` | 총 수수료 |
| `netSettlement` | `MoneyDTO` | `Money` | 실 정산액 |
| `status` | `String` | `SettlementStatus` | Enum: DRAFT, CONFIRMED, PAID |
| `items` | `List<SettlementItemDTO>` | `List<SettlementItem>` | 정산 상세 |

#### 2.7.2 Settlement DTO 정의

```kotlin
data class SettlementResponse(
    val id: String,
    val channelId: String,
    val channelName: String,              // 조인 필드
    val periodYear: Int,
    val periodMonth: Int,
    val totalSales: Double,
    val totalCommission: Double,
    val netSettlement: Double,
    val currency: String,
    val status: String,                   // "DRAFT", "CONFIRMED", "PAID"
    val itemCount: Int,                   // 상세 건수
    val createdAt: String,
    val confirmedAt: String?
)

data class SettlementDetailResponse(
    val id: String,
    val channelId: String,
    val channelName: String,
    val periodYear: Int,
    val periodMonth: Int,
    val totalSales: Double,
    val totalCommission: Double,
    val netSettlement: Double,
    val currency: String,
    val status: String,
    val items: List<SettlementItemDTO>,
    val createdAt: String,
    val confirmedAt: String?
)

data class SettlementItemDTO(
    val orderId: String,
    val orderNumber: String,              // 조인 필드
    val salesAmount: Double,
    val commission: Double,
    val currency: String
)

data class SettlementConfirmRequest(
    val confirm: Boolean = true
)
```

---

### 2.8 Automation (자동화)

#### 2.8.1 타입 비교

| 필드 | API DTO | Backend Entity | 비고 |
|------|---------|----------------|------|
| `id` | `String` | `RuleId` | VO 래핑 |
| `name` | `String` | `String` | 규칙명 |
| `trigger` | `TriggerDTO` | `Trigger` | 트리거 정보 |
| `conditions` | `List<ConditionDTO>` | `List<Condition>` | 조건 목록 |
| `actions` | `List<ActionDTO>` | `List<Action>` | 액션 목록 |
| `isActive` | `Boolean` | `Boolean` | 활성화 여부 |

#### 2.8.2 Automation DTO 정의

```kotlin
data class AutomationRuleResponse(
    val id: String,
    val name: String,
    val description: String?,
    val trigger: TriggerDTO,
    val conditions: List<ConditionDTO>,
    val actions: List<ActionDTO>,
    val isActive: Boolean,
    val lastExecutedAt: String?,          // ISO 8601
    val executionCount: Int,
    val createdAt: String
)

data class TriggerDTO(
    val type: String,                     // "ORDER_CREATED", "STOCK_LOW" 등
    val config: Map<String, Any>?         // 트리거 설정
)

data class ConditionDTO(
    val field: String,
    val operator: String,                 // "EQUALS", "GREATER_THAN" 등
    val value: String,
    val orderNum: Int = 0
)

data class ActionDTO(
    val type: String,                     // "SEND_SLACK", "CHANGE_ORDER_STATUS" 등
    val config: Map<String, Any>,         // 액션 설정
    val orderNum: Int = 0
)

data class AutomationRuleCreateRequest(
    val name: String,
    val description: String?,
    val trigger: TriggerDTO,
    val conditions: List<ConditionDTO> = emptyList(),
    val actions: List<ActionDTO>,
    val isActive: Boolean = true
)

data class AutomationRuleUpdateRequest(
    val name: String?,
    val description: String?,
    val conditions: List<ConditionDTO>?,
    val actions: List<ActionDTO>?,
    val isActive: Boolean?
)
```

---

### 2.9 Strategy (전략)

#### 2.9.1 Strategy DTO 정의

```kotlin
data class OperationsStrategyResponse(
    val id: String,
    val name: String,
    val weights: SimulationWeightsDTO,
    val result: SimulationResultDTO?,
    val isActive: Boolean,
    val deployedAt: String?,
    val createdAt: String,
    val updatedAt: String
)

data class SimulationWeightsDTO(
    val costReduction: Int,               // 물류비용 절감 (%)
    val leadTime: Int,                    // 리드타임 최단화 (%)
    val stockBalance: Int,                // 재고 분산 (%)
    val carbonEmission: Int               // 탄소 배출 저감 (%)
)

data class SimulationResultDTO(
    val efficiencyScore: Double,          // 운영 효율성 점수
    val costSaving: Double,               // 예상 비용 절감액
    val avgLeadTime: Double,              // 예상 평균 리드타임 (일)
    val recommendation: String
)

data class StrategyCreateRequest(
    val name: String,
    val weights: SimulationWeightsDTO
)

data class GlobalReadinessResponse(
    val id: String,
    val countryCode: String,
    val countryName: String,
    val readinessScore: Int,              // 전체 준비도 (%)
    val hsCodeMappingRate: Int,           // HS Code 매핑률 (%)
    val certificationStatus: String,      // "NONE", "PARTIAL", "COMPLETE"
    val logisticsNodeReady: Boolean,
    val taxIdRegistered: Boolean,
    val requiredCertifications: List<String>?,
    val blockers: List<String>?,
    val notes: String?,
    val updatedAt: String
)
```

---

## 3. 공통 변환 규칙

### 3.1 날짜/시간 변환

| 방향 | 소스 | 대상 | 포맷 |
|------|------|------|------|
| Frontend → Backend | `string` | `Instant` | ISO 8601 파싱 |
| Backend → Frontend | `Instant` | `string` | ISO 8601 문자열 |

```kotlin
// 변환 유틸리티
object DateTimeConverter {
    private val formatter = DateTimeFormatter.ISO_INSTANT

    fun toInstant(isoString: String): Instant = Instant.parse(isoString)

    fun toIsoString(instant: Instant): String = formatter.format(instant)

    // 프론트엔드 로컬 타임 표시용 (KST)
    fun toKoreanTime(instant: Instant): String {
        val kst = ZoneId.of("Asia/Seoul")
        return instant.atZone(kst).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}
```

### 3.2 금액(Money) 변환

```kotlin
data class MoneyDTO(
    val amount: Double,
    val currency: String = "KRW"
)

// Entity → DTO
fun Money.toDTO(): MoneyDTO = MoneyDTO(
    amount = this.amount.toDouble(),
    currency = this.currency.name
)

// DTO → Entity
fun MoneyDTO.toEntity(): Money = Money(
    amount = BigDecimal.valueOf(this.amount),
    currency = Currency.valueOf(this.currency)
)
```

### 3.3 페이지네이션

```kotlin
// 요청
data class PageRequest(
    val page: Int = 0,          // 0-based
    val size: Int = 20,
    val sort: String? = null,   // "createdAt,desc"
    val filter: Map<String, String>? = null
)

// 응답
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
```

### 3.4 에러 응답

```kotlin
data class ApiErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail
)

data class ErrorDetail(
    val code: String,           // "PRODUCT_NOT_FOUND"
    val message: String,        // "상품을 찾을 수 없습니다"
    val details: Map<String, Any>? = null
)

// 프론트엔드 types.ts의 ApiResponse와 호환
// export interface ApiResponse<T> {
//   success: boolean;
//   data: T;
//   error?: {
//     code: string;
//     message: string;
//   };
// }
```

---

## 4. Enum 매핑 테이블

### 4.1 OrderStatus

| Frontend | Backend | DB |
|----------|---------|-----|
| `'NEW'` | `OrderStatus.NEW` | `'NEW'` |
| `'PAYMENT_PENDING'` | `OrderStatus.PAYMENT_PENDING` | `'PAYMENT_PENDING'` |
| `'PAID'` | `OrderStatus.PAID` | `'PAID'` |
| `'PREPARING'` | `OrderStatus.PREPARING` | `'PREPARING'` |
| `'READY_TO_SHIP'` | `OrderStatus.READY_TO_SHIP` | `'READY_TO_SHIP'` |
| `'SHIPPED'` | `OrderStatus.SHIPPED` | `'SHIPPED'` |
| `'IN_DELIVERY'` | `OrderStatus.IN_DELIVERY` | `'IN_DELIVERY'` |
| `'DELIVERED'` | `OrderStatus.DELIVERED` | `'DELIVERED'` |
| `'CANCELLED'` | `OrderStatus.CANCELLED` | `'CANCELLED'` |
| `'EXCHANGE_REQUESTED'` | `OrderStatus.EXCHANGE_REQUESTED` | `'EXCHANGE_REQUESTED'` |
| `'RETURN_REQUESTED'` | `OrderStatus.RETURN_REQUESTED` | `'RETURN_REQUESTED'` |

### 4.2 StockStatus

| Frontend | Backend | DB |
|----------|---------|-----|
| `'NORMAL'` | `StockStatus.NORMAL` | `'NORMAL'` |
| `'LOW'` | `StockStatus.LOW` | `'LOW'` |
| `'OUT_OF_STOCK'` | `StockStatus.OUT_OF_STOCK` | `'OUT_OF_STOCK'` |
| `'OVERSTOCK'` | `StockStatus.OVERSTOCK` | `'OVERSTOCK'` |

### 4.3 ProductStatus

| Frontend | Backend | DB |
|----------|---------|-----|
| `'ACTIVE'` | `ProductStatus.ACTIVE` | `'ACTIVE'` |
| `'INACTIVE'` | `ProductStatus.INACTIVE` | `'INACTIVE'` |
| `'OUT_OF_STOCK'` | `ProductStatus.OUT_OF_STOCK` | `'OUT_OF_STOCK'` |

### 4.4 UnitOfMeasure (특수)

| Frontend | Backend | DB | 비고 |
|----------|---------|-----|------|
| `'PCS'` | `UnitOfMeasure.PCS` | `'PCS'` | |
| `'SET'` | `UnitOfMeasure.SET` | `'SET'` | |
| `'BOX'` | `UnitOfMeasure.BOX` | `'BOX'` | |
| `'KG'` | `UnitOfMeasure.KG` | `'KG'` | |
| `'EA'` | `UnitOfMeasure.EA` | `'EA'` | |
| **`'台'`** | **`UnitOfMeasure.TAI`** | `'TAI'` | **대만 단위 - 특수 변환 필요** |

### 4.5 TemperatureType (케이스 변환 필요)

| Frontend | Backend | DB |
|----------|---------|-----|
| `'Normal'` | `TemperatureType.NORMAL` | `'NORMAL'` |
| `'Temperature Control'` | `TemperatureType.TEMPERATURE_CONTROL` | `'TEMPERATURE_CONTROL'` |
| `'Cold'` | `TemperatureType.COLD` | `'COLD'` |
| `'Freezing'` | `TemperatureType.FREEZING` | `'FREEZING'` |
| `'Cryogenic'` | `TemperatureType.CRYOGENIC` | `'CRYOGENIC'` |

### 4.6 ChannelStatus

| Frontend | Backend | DB |
|----------|---------|-----|
| `'CONNECTED'` | `ChannelStatus.CONNECTED` | `'CONNECTED'` |
| `'DISCONNECTED'` | `ChannelStatus.DISCONNECTED` | `'DISCONNECTED'` |
| `'ERROR'` | `ChannelStatus.ERROR` | `'ERROR'` |

### 4.7 DimensionUnit (대소문자 변환 필요)

| Frontend | Backend | DB | 비고 |
|----------|---------|-----|------|
| `'cm'` | `DimensionUnit.CM` | `'CM'` | **소문자 → 대문자** |
| `'mm'` | `DimensionUnit.MM` | `'MM'` | **소문자 → 대문자** |

```kotlin
// DimensionUnit 변환 규칙
object DimensionUnitConverter {
    fun toBackend(frontend: String): DimensionUnit = when (frontend.lowercase()) {
        "cm" -> DimensionUnit.CM
        "mm" -> DimensionUnit.MM
        else -> DimensionUnit.CM  // 기본값
    }

    fun toFrontend(backend: DimensionUnit): String = backend.name.lowercase()
    // CM → "cm", MM → "mm"
}
```

---

## 5. API 엔드포인트 설계

### 5.1 RESTful 엔드포인트 (요약)

| 도메인 | 메서드 | 엔드포인트 | 설명 |
|--------|--------|------------|------|
| **Product** | GET | `/api/products` | 상품 목록 조회 |
| | GET | `/api/products/{id}` | 상품 상세 조회 |
| | POST | `/api/products` | 상품 생성 |
| | PUT | `/api/products/{id}` | 상품 수정 |
| | DELETE | `/api/products/{id}` | 상품 삭제 |
| **Inventory** | GET | `/api/inventory` | 재고 목록 조회 |
| | GET | `/api/inventory/{productId}/warehouses` | 창고별 재고 |
| | POST | `/api/inventory/adjust` | 재고 조정 |
| | POST | `/api/inventory/{stockId}/allocate` | 채널 할당 |
| **Order** | GET | `/api/orders` | 주문 목록 |
| | GET | `/api/orders/{id}` | 주문 상세 |
| | POST | `/api/orders` | 주문 생성 |
| | PATCH | `/api/orders/{id}/status` | 상태 변경 |
| **Channel** | GET | `/api/channels` | 채널 목록 |
| | POST | `/api/channels/{id}/sync` | 동기화 트리거 |
| **Claim** | GET | `/api/claims` | 클레임 목록 |
| | POST | `/api/claims` | 클레임 생성 |
| | PATCH | `/api/claims/{id}` | 클레임 처리 |
| **Settlement** | GET | `/api/settlements` | 정산 목록 |
| | GET | `/api/settlements/{id}` | 정산 상세 |

### 5.2 응답 포맷 표준

```kotlin
// 성공 응답
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T,
    val meta: ResponseMeta? = null
)

data class ResponseMeta(
    val requestId: String,
    val timestamp: String
)

// 목록 응답 (페이지네이션)
data class ApiListResponse<T>(
    val success: Boolean = true,
    val data: List<T>,
    val page: PageInfo
)

data class PageInfo(
    val current: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int
)
```

---

## 6. 변환 서비스 구현 가이드

### 6.1 Mapper 인터페이스 패턴

```kotlin
// application/product/mapper/ProductMapper.kt

interface ProductMapper {
    fun toResponse(entity: Product, totalStock: Int): ProductResponse
    fun toEntity(request: ProductCreateRequest, companyId: CompanyId): Product
    fun toLocalizedString(dto: LocalizedStringDTO): LocalizedString
    fun toLogisticsInfo(dto: LogisticsInfoDTO): LogisticsInfo
}

@Component
class ProductMapperImpl : ProductMapper {

    override fun toResponse(entity: Product, totalStock: Int): ProductResponse {
        return ProductResponse(
            id = entity.id.value,
            sku = entity.sku.value,
            name = mapOf(
                "ko" to entity.name.ko,
                "en" to (entity.name.en ?: "")
            ),
            brand = entity.brand,
            category = entity.category.path,
            uom = UomConverter.toFrontend(entity.uom),  // TAI → "台"
            color = entity.color,
            ownerName = entity.ownerName,
            customerGoodsNo = entity.customerGoodsNo,
            barcodes = entity.barcodes.map { BarcodeDTO(it.code, it.isMain) },
            basePrice = entity.basePrice.amount.toDouble(),
            totalStock = totalStock,
            status = entity.status.name,
            dimensions = entity.dimensions?.let {
                DimensionsDTO(
                    width = it.width.toDouble(),
                    length = it.length.toDouble(),
                    height = it.height.toDouble(),
                    unit = it.unit.name.lowercase()
                )
            },
            netWeight = entity.weight?.net?.toDouble(),
            grossWeight = entity.weight?.gross?.toDouble(),
            logisticsInfo = LogisticsInfoDTO(
                tempMgmt = TempConverter.toFrontend(entity.logisticsInfo.tempManagement),
                shelfLifeMgmt = entity.logisticsInfo.shelfLifeManagement,
                snMgmt = entity.logisticsInfo.serialNumberManagement,
                isDangerous = entity.logisticsInfo.isDangerous,
                isFragile = entity.logisticsInfo.isFragile,
                isHighValue = entity.logisticsInfo.isHighValue,
                isNonStandard = entity.logisticsInfo.isNonStandard
            ),
            hsCode = entity.hsCode,
            countryOfOrigin = entity.countryOfOrigin,
            materialContent = entity.material,
            manufacturerDetails = entity.manufacturerName?.let {
                ManufacturerDTO(
                    name = it,
                    address = entity.manufacturerAddress ?: ""
                )
            },
            customsStrategies = entity.customsStrategies.map { cs ->
                CustomsStrategyDTO(
                    countryCode = cs.countryCode,
                    localHsCode = cs.localHsCode,
                    invoiceName = cs.invoiceName,
                    dutyRate = cs.dutyRate,
                    requiredDocs = cs.requiredDocs,
                    complianceAlert = cs.complianceAlert
                )
            }
        )
    }

    // ... 기타 변환 메서드
}
```

### 6.2 컨버터 유틸리티

```kotlin
// core/converter/UomConverter.kt
object UomConverter {
    fun toBackend(frontend: String): UnitOfMeasure = when (frontend) {
        "台" -> UnitOfMeasure.TAI
        else -> UnitOfMeasure.valueOf(frontend)
    }

    fun toFrontend(backend: UnitOfMeasure): String = when (backend) {
        UnitOfMeasure.TAI -> "台"
        else -> backend.name
    }
}

// core/converter/TempConverter.kt
object TempConverter {
    private val frontendToBackend = mapOf(
        "Normal" to TemperatureType.NORMAL,
        "Temperature Control" to TemperatureType.TEMPERATURE_CONTROL,
        "Cold" to TemperatureType.COLD,
        "Freezing" to TemperatureType.FREEZING,
        "Cryogenic" to TemperatureType.CRYOGENIC
    )

    private val backendToFrontend = frontendToBackend.entries.associate { it.value to it.key }

    fun toBackend(frontend: String): TemperatureType =
        frontendToBackend[frontend] ?: TemperatureType.NORMAL

    fun toFrontend(backend: TemperatureType): String =
        backendToFrontend[backend] ?: "Normal"
}
```

---

## 7. 요약: 주요 변환 포인트

| # | 항목 | 프론트엔드 | 백엔드 | 변환 필요 |
|---|------|-----------|--------|----------|
| 1 | 도메인명 | `Inventory` | `Stock` | API 경로 유지, 내부 명칭 차이 |
| 2 | UOM 단위 | `'台'` | `TAI` | 양방향 문자열 변환 |
| 3 | 온도 타입 | `'Normal'` | `NORMAL` | 케이스 + 공백 변환 |
| 4 | 주문 상품 가격 | `price` (단가) | `unitPrice`, `totalPrice` | 구조 확장 |
| 5 | 채널 타입 | `string` (자유) | `ChannelType enum` | 매핑 변환 |
| 6 | 날짜 | `string` | `Instant` | ISO 8601 파싱 |
| 7 | 금액 | `number` | `Money` (VO) | BigDecimal + Currency 래핑 |
| 8 | 창고 필드명 | `wmsNode` | `assignedWarehouseId` | 호환성 유지 필드 추가 |

---

## 8. 다음 단계

### Phase 4: API 구현 준비
- [ ] OpenAPI 3.0 스펙 작성
- [ ] 에러 코드 상세 정의
- [ ] 인증/인가 스펙 (JWT)
- [ ] Rate Limiting 정책

### Phase 5: 코드 구현
- [ ] Kotlin 멀티모듈 프로젝트 생성
- [ ] Mapper 클래스 구현
- [ ] Controller 구현
- [ ] 통합 테스트 작성
