# DDD 도메인 모델 설계

**버전**: 1.0
**작성일**: 2025-01-18

---

## 1. Bounded Context 정의

### 1.1 컨텍스트 맵

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Global OMS Domain                               │
└─────────────────────────────────────────────────────────────────────────┘

  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │   Identity   │     │   Catalog    │     │   Order      │
  │   Context    │     │   Context    │     │   Context    │
  │              │     │              │     │              │
  │ - Company    │     │ - Product    │     │ - Order      │
  │ - User       │     │ - Category   │     │ - OrderItem  │
  │ - Role       │     │ - Barcode    │     │ - Shipping   │
  └──────┬───────┘     └──────┬───────┘     └──────┬───────┘
         │                    │                    │
         │ ACL               │ ACL               │
         ▼                    ▼                    ▼
  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │   Channel    │     │  Inventory   │     │   Claim      │
  │   Context    │     │   Context    │     │   Context    │
  │              │     │              │     │              │
  │ - Channel    │────►│ - Stock      │◄────│ - Claim      │
  │ - Warehouse  │     │ - Allocation │     │ - Return     │
  │ - Mapping    │     │ - Movement   │     │ - Exchange   │
  └──────────────┘     └──────────────┘     └──────────────┘
         │
         │
         ▼
  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │  Settlement  │     │  Automation  │     │  Integration │
  │   Context    │     │   Context    │     │   Context    │
  │              │     │              │     │              │
  │ - Settlement │     │ - Rule       │     │ - Webhook    │
  │ - Commission │     │ - Trigger    │     │ - ApiLog     │
  │ - Period     │     │ - Action     │     │ - Event      │
  └──────────────┘     └──────────────┘     └──────────────┘
```

### 1.2 Context 관계 유형

| 관계 | 설명 |
|------|------|
| **Shared Kernel** | Identity Context ↔ 모든 Context (Company, User 참조) |
| **Customer-Supplier** | Order Context → Inventory Context (재고 차감 요청) |
| **Conformist** | Integration Context → 외부 채널 API |
| **ACL (Anti-Corruption Layer)** | 외부 시스템 데이터 변환 계층 |

---

## 2. Bounded Context 상세

### 2.1 Identity Context (신원)

**책임**: 회사, 사용자, 권한 관리

```kotlin
// ========================
// Aggregate: Company
// ========================
@Entity
class Company(
    @Id val id: CompanyId,
    var name: String,
    var businessNumber: String,
    var status: CompanyStatus,
    val createdAt: Instant
) {
    // Invariants
    // - businessNumber는 고유해야 함
    // - status가 SUSPENDED면 사용자 로그인 불가
}

@Embeddable
data class CompanyId(val value: UUID)

enum class CompanyStatus { ACTIVE, SUSPENDED, DELETED }

// ========================
// Aggregate: User
// ========================
@Entity
class User(
    @Id val id: UserId,
    val companyId: CompanyId,  // FK
    var email: Email,
    var name: String,
    var role: UserRole,
    var status: UserStatus,
    val createdAt: Instant
) {
    // Invariants
    // - email은 회사 내 고유
    // - OWNER 역할은 회사당 1명만 가능
}

@Embeddable
data class UserId(val value: UUID)

@Embeddable
data class Email(val value: String) {
    init { require(value.contains("@")) }
}

enum class UserRole { OWNER, EDITOR, VIEWER }
enum class UserStatus { ACTIVE, INACTIVE, INVITED }
```

---

### 2.2 Catalog Context (상품 카탈로그)

**책임**: 상품 마스터, 바코드, 통관 전략 관리

```kotlin
// ========================
// Aggregate: Product
// ========================
@Entity
class Product(
    @Id val id: ProductId,
    val companyId: CompanyId,
    val sku: Sku,
    var name: LocalizedString,
    var brand: String,
    var category: Category,
    var uom: UnitOfMeasure,
    var basePrice: Money,
    var status: ProductStatus,

    // Value Objects (Embedded)
    var dimensions: Dimensions?,
    var weight: Weight?,
    var logisticsInfo: LogisticsInfo,

    // Additional Attributes
    var color: String?,                    // 상품 색상
    var ownerName: String?,                // 상품 소유자명
    var customerGoodsNo: String?,          // 채널별 상품코드 (고객사 상품번호)

    // Customs & Compliance Info
    var hsCode: String?,                   // HS Code (기본)
    var countryOfOrigin: String?,          // 원산지
    var material: String?,                 // 소재/성분
    var manufacturer: String?,             // 제조사
    var manufacturerAddress: String?,      // 제조사 주소

    // Child Entities
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    val barcodes: MutableList<Barcode> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    val customsStrategies: MutableList<CustomsStrategy> = mutableListOf(),

    val createdAt: Instant
) {
    // Invariants
    // - SKU는 회사 내 고유
    // - 메인 바코드는 1개만 가능
    // - 상태가 ACTIVE가 아니면 주문 불가

    fun addBarcode(code: String, isMain: Boolean) {
        if (isMain) {
            barcodes.forEach { it.isMain = false }
        }
        barcodes.add(Barcode(code = code, isMain = isMain))
    }

    fun setMainBarcode(code: String) {
        barcodes.forEach { it.isMain = (it.code == code) }
    }

    fun addCustomsStrategy(strategy: CustomsStrategy) {
        // 동일 국가 전략이 있으면 교체
        customsStrategies.removeIf { it.countryCode == strategy.countryCode }
        customsStrategies.add(strategy)
    }
}

@Embeddable
data class ProductId(val value: String)  // "OMS-FG0015687674" 형식

@Embeddable
data class Sku(val value: String)

@Embeddable
data class LocalizedString(
    val ko: String,
    val en: String? = null
)

@Embeddable
data class Category(
    val path: String  // "반려동물 > 강아지 사료"
) {
    val depth: Int get() = path.split(" > ").size
    val leaf: String get() = path.split(" > ").last()
}

enum class UnitOfMeasure { PCS, SET, BOX, KG, EA, TAI }  // TAI = '台' (대만 단위)
enum class ProductStatus { ACTIVE, INACTIVE, OUT_OF_STOCK }

// ========================
// Child Entity: Barcode
// ========================
@Entity
class Barcode(
    @Id @GeneratedValue val id: Long = 0,
    val code: String,
    var isMain: Boolean
)

// ========================
// Child Entity: CustomsStrategy
// ========================
@Entity
class CustomsStrategy(
    @Id @GeneratedValue val id: Long = 0,
    val countryCode: String,           // "US", "JP"
    val localHsCode: String,           // 해당 국가 HS Code
    val invoiceName: String,           // 통관용 영문 상품명
    val dutyRate: String,              // "16.5%"
    @ElementCollection
    val requiredDocs: List<String>,    // ["FCC Declaration"]
    val complianceAlert: String?       // 규제 경고
)

// ========================
// Value Objects
// ========================
@Embeddable
data class Dimensions(
    val width: BigDecimal,
    val length: BigDecimal,
    val height: BigDecimal,
    val unit: DimensionUnit
)

enum class DimensionUnit { CM, MM }

@Embeddable
data class Weight(
    val net: BigDecimal,
    val gross: BigDecimal,
    val unit: WeightUnit
)

enum class WeightUnit { KG, G }

@Embeddable
data class LogisticsInfo(
    val tempManagement: TemperatureType,
    val shelfLifeManagement: Boolean,
    val serialNumberManagement: Boolean,
    val isDangerous: Boolean,
    val isFragile: Boolean,
    val isHighValue: Boolean,
    val isNonStandard: Boolean
)

enum class TemperatureType { NORMAL, TEMPERATURE_CONTROL, COLD, FREEZING, CRYOGENIC }

@Embeddable
data class Money(
    val amount: BigDecimal,
    val currency: Currency
)

enum class Currency { KRW, USD, JPY }
```

---

### 2.3 Channel Context (채널/물류)

**책임**: 판매 채널, 물류 거점, 채널-창고 매핑 관리

```kotlin
// ========================
// Aggregate: Channel
// ========================
@Entity
class Channel(
    @Id val id: ChannelId,
    val companyId: CompanyId,
    var name: String,
    val type: ChannelType,
    var status: ChannelStatus,
    var lastSyncAt: Instant?,

    // 채널 API 인증 정보 (암호화)
    var credentials: ChannelCredentials?,

    val createdAt: Instant
) {
    // Invariants
    // - status가 ERROR면 동기화 불가
    // - credentials 없으면 DISCONNECTED

    fun markSynced() {
        lastSyncAt = Instant.now()
    }

    fun markError(reason: String) {
        status = ChannelStatus.ERROR
    }
}

@Embeddable
data class ChannelId(val value: String)  // "CH-001"

enum class ChannelType { D2C, MARKET, GLOBAL }
enum class ChannelStatus { CONNECTED, DISCONNECTED, ERROR }

@Embeddable
data class ChannelCredentials(
    val apiKey: String,      // 암호화 저장
    val secretKey: String?,
    val additionalConfig: String?  // JSON
)

// ========================
// Aggregate: Warehouse
// ========================
@Entity
class Warehouse(
    @Id val id: WarehouseId,
    val companyId: CompanyId,
    var name: String,
    val region: String,           // "수도권", "영남권"
    val type: WarehouseType,
    var capacity: Int,            // 가동률 (%)
    var status: WarehouseStatus,

    val address: Address,

    val createdAt: Instant
)

@Embeddable
data class WarehouseId(val value: String)  // "WH-001"

enum class WarehouseType { AUTO, MEGA, HUB, AIR }
enum class WarehouseStatus { ACTIVE, MAINTENANCE, INACTIVE }

@Embeddable
data class Address(
    val zipCode: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val country: String
)

// ========================
// Aggregate: ChannelWarehouseMapping (N:M 관계)
// ========================
@Entity
class ChannelWarehouseMapping(
    @Id val id: MappingId,
    val channelId: ChannelId,
    val warehouseId: WarehouseId,
    var role: MappingRole,
    var priority: Int,              // 1이 최우선
    var isActive: Boolean,
    var loadBalancingEnabled: Boolean,

    val createdAt: Instant
) {
    // Invariants
    // - 동일 채널-창고 조합은 1개만 존재
    // - PRIMARY 역할은 채널당 1개만 가능
    // - priority는 1부터 시작
}

@Embeddable
data class MappingId(val value: UUID)

enum class MappingRole { PRIMARY, REGIONAL, BACKUP }
```

---

### 2.4 Order Context (주문)

**책임**: 주문 라이프사이클, 배송 관리

```kotlin
// ========================
// Aggregate: Order
// ========================
@Entity
class Order(
    @Id val id: OrderId,
    val companyId: CompanyId,
    val channelId: ChannelId,
    val externalOrderId: String?,      // 채널 주문번호

    var status: OrderStatus,
    val orderDate: Instant,

    // 고객 정보 (Value Object)
    val customer: Customer,
    val shippingAddress: Address,

    // 풀필먼트
    val fulfillmentMethod: FulfillmentMethod,
    var assignedWarehouseId: WarehouseId?,
    var routingLogic: String?,          // "재고 우선 배정"

    // 금액
    var totalAmount: Money,

    // 주문 상품
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),

    // 배송 정보
    @OneToOne(cascade = [CascadeType.ALL])
    var shipping: Shipping? = null,

    val createdAt: Instant
) {
    // ========================
    // Domain Logic
    // ========================

    fun canTransitionTo(newStatus: OrderStatus): Boolean {
        return OrderStatusMachine.canTransition(status, newStatus)
    }

    fun transitionTo(newStatus: OrderStatus) {
        require(canTransitionTo(newStatus)) {
            "Cannot transition from $status to $newStatus"
        }
        status = newStatus
        // 도메인 이벤트 발행
        registerEvent(OrderStatusChangedEvent(id, status, newStatus))
    }

    fun assignToWarehouse(warehouseId: WarehouseId, logic: String) {
        require(fulfillmentMethod == FulfillmentMethod.WMS)
        assignedWarehouseId = warehouseId
        routingLogic = logic
    }

    fun createShipping(carrier: Carrier, trackingNumber: String) {
        require(status == OrderStatus.READY_TO_SHIP)
        shipping = Shipping(
            carrier = carrier,
            trackingNumber = trackingNumber,
            status = ShippingStatus.PICKED_UP
        )
        transitionTo(OrderStatus.SHIPPED)
    }

    // 금액 재계산
    fun recalculateTotal() {
        totalAmount = Money(
            amount = items.sumOf { it.totalPrice.amount },
            currency = totalAmount.currency
        )
    }
}

@Embeddable
data class OrderId(val value: String)  // "ORD-20250118-001"

enum class OrderStatus {
    NEW,
    PAYMENT_PENDING,
    PAID,
    PREPARING,
    READY_TO_SHIP,
    SHIPPED,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED,
    EXCHANGE_REQUESTED,
    RETURN_REQUESTED
}

enum class FulfillmentMethod { WMS, DIRECT }

// ========================
// 상태 전이 규칙
// ========================
object OrderStatusMachine {
    private val transitions = mapOf(
        OrderStatus.NEW to setOf(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),
        OrderStatus.PAYMENT_PENDING to setOf(OrderStatus.PAID, OrderStatus.CANCELLED),
        OrderStatus.PAID to setOf(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING to setOf(OrderStatus.READY_TO_SHIP, OrderStatus.CANCELLED),
        OrderStatus.READY_TO_SHIP to setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED to setOf(OrderStatus.IN_DELIVERY),
        OrderStatus.IN_DELIVERY to setOf(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED to setOf(OrderStatus.EXCHANGE_REQUESTED, OrderStatus.RETURN_REQUESTED)
    )

    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean {
        return transitions[from]?.contains(to) ?: false
    }
}

// ========================
// Value Object: Customer
// ========================
@Embeddable
data class Customer(
    val name: String,
    val phone: String,
    val email: String?
)

// ========================
// Child Entity: OrderItem
// ========================
@Entity
class OrderItem(
    @Id @GeneratedValue val id: Long = 0,
    val productId: ProductId,
    val productName: String,      // 주문 시점 상품명 (스냅샷)
    val sku: Sku,
    val quantity: Int,
    val unitPrice: Money,
    val totalPrice: Money
) {
    init {
        require(quantity > 0)
    }
}

// ========================
// Child Entity: Shipping
// ========================
@Entity
class Shipping(
    @Id @GeneratedValue val id: Long = 0,
    val carrier: Carrier,
    val trackingNumber: String,
    var status: ShippingStatus,
    var shippedAt: Instant? = null,
    var deliveredAt: Instant? = null,

    @OneToMany(cascade = [CascadeType.ALL])
    val trackingHistory: MutableList<TrackingEvent> = mutableListOf()
) {
    fun addTrackingEvent(event: TrackingEvent) {
        trackingHistory.add(event)
        status = event.status
        if (event.status == ShippingStatus.DELIVERED) {
            deliveredAt = event.timestamp
        }
    }
}

enum class Carrier { CJ, HANJIN, LOGEN, POST, FEDEX, DHL, UPS }
enum class ShippingStatus { PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION }

@Entity
class TrackingEvent(
    @Id @GeneratedValue val id: Long = 0,
    val status: ShippingStatus,
    val location: String,
    val description: String,
    val timestamp: Instant
)
```

---

### 2.5 Inventory Context (재고)

**책임**: 재고 수량 관리, 채널별 할당

```kotlin
// ========================
// Aggregate: Stock
// ========================
@Entity
class Stock(
    @Id val id: StockId,
    val companyId: CompanyId,
    val productId: ProductId,
    val warehouseId: WarehouseId,

    var total: Int,              // 전체 재고
    var available: Int,          // 가용 재고
    var reserved: Int,           // 예약 재고
    var safetyStock: Int,        // 안전 재고

    var status: StockStatus,

    // 채널별 가상 재고 할당
    @ElementCollection
    val channelAllocations: MutableMap<ChannelId, Int> = mutableMapOf(),

    val createdAt: Instant,
    var updatedAt: Instant
) {
    // ========================
    // Domain Logic
    // ========================

    // Invariant: total = available + reserved
    init {
        require(total == available + reserved)
    }

    fun receive(quantity: Int) {
        require(quantity > 0)
        total += quantity
        available += quantity
        updateStatus()
        registerEvent(StockReceivedEvent(id, quantity))
    }

    fun reserve(quantity: Int) {
        require(quantity > 0)
        require(available >= quantity) { "Insufficient available stock" }
        available -= quantity
        reserved += quantity
        updateStatus()
        registerEvent(StockReservedEvent(id, quantity))
    }

    fun release(quantity: Int) {
        require(quantity > 0)
        require(reserved >= quantity)
        reserved -= quantity
        available += quantity
        updateStatus()
    }

    fun ship(quantity: Int) {
        require(quantity > 0)
        require(reserved >= quantity)
        reserved -= quantity
        total -= quantity
        updateStatus()
        registerEvent(StockShippedEvent(id, quantity))
    }

    fun adjust(newTotal: Int, reason: String) {
        val diff = newTotal - total
        total = newTotal
        available = maxOf(0, total - reserved)
        updateStatus()
        registerEvent(StockAdjustedEvent(id, diff, reason))
    }

    fun allocateToChannel(channelId: ChannelId, quantity: Int) {
        val currentTotal = channelAllocations.values.sum()
        require(currentTotal + quantity <= available) {
            "Total channel allocation cannot exceed available stock"
        }
        channelAllocations[channelId] = quantity
    }

    private fun updateStatus() {
        status = when {
            available == 0 -> StockStatus.OUT_OF_STOCK
            available < safetyStock -> StockStatus.LOW
            available > total * 2 -> StockStatus.OVERSTOCK
            else -> StockStatus.NORMAL
        }
        updatedAt = Instant.now()
    }
}

@Embeddable
data class StockId(val value: UUID)

enum class StockStatus { NORMAL, LOW, OUT_OF_STOCK, OVERSTOCK }

// ========================
// Entity: StockMovement (재고 이동 이력)
// ========================
@Entity
class StockMovement(
    @Id val id: MovementId,
    val stockId: StockId,
    val type: MovementType,
    val quantity: Int,
    val beforeQuantity: Int,
    val afterQuantity: Int,
    val reason: String?,
    val referenceId: String?,      // 주문ID, 입고ID 등
    val createdAt: Instant,
    val createdBy: UserId
)

@Embeddable
data class MovementId(val value: UUID)

enum class MovementType {
    RECEIVE,         // 입고
    SHIP,            // 출고
    RESERVE,         // 예약
    RELEASE,         // 예약 해제
    ADJUST,          // 조정
    TRANSFER_IN,     // 이동 입고
    TRANSFER_OUT     // 이동 출고
}
```

---

### 2.6 Claim Context (클레임)

**책임**: 취소, 반품, 교환 처리

```kotlin
// ========================
// Aggregate: Claim
// ========================
@Entity
class Claim(
    @Id val id: ClaimId,
    val companyId: CompanyId,
    val orderId: OrderId,
    val type: ClaimType,
    var status: ClaimStatus,
    val reason: String,
    var memo: String?,
    val priority: ClaimPriority,

    // 반품/교환 상품
    @OneToMany(cascade = [CascadeType.ALL])
    val items: MutableList<ClaimItem> = mutableListOf(),

    // 환불 정보
    var refundAmount: Money?,
    var refundedAt: Instant?,

    val createdAt: Instant,
    var processedAt: Instant?
) {
    fun startProcessing() {
        require(status == ClaimStatus.PENDING)
        status = ClaimStatus.PROCESSING
    }

    fun complete(refundAmount: Money?) {
        require(status == ClaimStatus.PROCESSING)
        status = ClaimStatus.COMPLETED
        this.refundAmount = refundAmount
        this.refundedAt = if (refundAmount != null) Instant.now() else null
        processedAt = Instant.now()
        registerEvent(ClaimCompletedEvent(id, type, refundAmount))
    }

    fun reject(reason: String) {
        require(status == ClaimStatus.PROCESSING)
        status = ClaimStatus.REJECTED
        memo = reason
        processedAt = Instant.now()
    }
}

@Embeddable
data class ClaimId(val value: String)  // "CLM-20250118-001"

enum class ClaimType { CANCEL, RETURN, EXCHANGE }
enum class ClaimStatus { PENDING, PROCESSING, COMPLETED, REJECTED }
enum class ClaimPriority { URGENT, NORMAL }

@Entity
class ClaimItem(
    @Id @GeneratedValue val id: Long = 0,
    val productId: ProductId,
    val quantity: Int,
    val reason: String?
)
```

---

### 2.7 Settlement Context (정산)

**책임**: 채널별 정산, 수수료 계산

```kotlin
// ========================
// Aggregate: Settlement
// ========================
@Entity
class Settlement(
    @Id val id: SettlementId,
    val companyId: CompanyId,
    val channelId: ChannelId,
    val period: SettlementPeriod,

    var totalSales: Money,
    var totalCommission: Money,
    var netSettlement: Money,

    var status: SettlementStatus,

    @OneToMany(cascade = [CascadeType.ALL])
    val items: MutableList<SettlementItem> = mutableListOf(),

    val createdAt: Instant,
    var confirmedAt: Instant?
) {
    fun addItem(orderId: OrderId, salesAmount: Money, commissionRate: BigDecimal) {
        val commission = Money(
            amount = salesAmount.amount * commissionRate,
            currency = salesAmount.currency
        )
        items.add(SettlementItem(
            orderId = orderId,
            salesAmount = salesAmount,
            commissionAmount = commission
        ))
        recalculate()
    }

    private fun recalculate() {
        totalSales = Money(
            amount = items.sumOf { it.salesAmount.amount },
            currency = totalSales.currency
        )
        totalCommission = Money(
            amount = items.sumOf { it.commissionAmount.amount },
            currency = totalSales.currency
        )
        netSettlement = Money(
            amount = totalSales.amount - totalCommission.amount,
            currency = totalSales.currency
        )
    }

    fun confirm() {
        require(status == SettlementStatus.DRAFT)
        status = SettlementStatus.CONFIRMED
        confirmedAt = Instant.now()
    }
}

@Embeddable
data class SettlementId(val value: UUID)

@Embeddable
data class SettlementPeriod(
    val year: Int,
    val month: Int
)

enum class SettlementStatus { DRAFT, CONFIRMED, PAID }

@Entity
class SettlementItem(
    @Id @GeneratedValue val id: Long = 0,
    val orderId: OrderId,
    val salesAmount: Money,
    val commissionAmount: Money
)
```

---

### 2.8 Automation Context (자동화)

**책임**: 자동화 규칙 정의 및 실행

```kotlin
// ========================
// Aggregate: AutomationRule
// ========================
@Entity
class AutomationRule(
    @Id val id: RuleId,
    val companyId: CompanyId,
    var name: String,
    var description: String?,

    val trigger: Trigger,
    val conditions: List<Condition>,
    val actions: List<Action>,

    var isActive: Boolean,
    var lastExecutedAt: Instant?,
    var executionCount: Int = 0,

    val createdAt: Instant
) {
    fun execute(context: ExecutionContext): Boolean {
        if (!isActive) return false

        // 조건 평가
        if (!conditions.all { it.evaluate(context) }) {
            return false
        }

        // 액션 실행
        actions.forEach { it.execute(context) }

        lastExecutedAt = Instant.now()
        executionCount++

        registerEvent(RuleExecutedEvent(id, context))
        return true
    }

    fun toggle() {
        isActive = !isActive
    }
}

@Embeddable
data class RuleId(val value: UUID)

// ========================
// Value Objects
// ========================
@Embeddable
data class Trigger(
    val type: TriggerType,
    val config: String  // JSON
)

enum class TriggerType {
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
    STOCK_LOW,
    STOCK_CHANGED,
    PAYMENT_FAILED,
    SHIPPING_STARTED
}

@Embeddable
data class Condition(
    val field: String,
    val operator: ConditionOperator,
    val value: String
) {
    fun evaluate(context: ExecutionContext): Boolean {
        val fieldValue = context.getValue(field)
        return when (operator) {
            ConditionOperator.EQUALS -> fieldValue == value
            ConditionOperator.NOT_EQUALS -> fieldValue != value
            ConditionOperator.GREATER_THAN -> (fieldValue?.toIntOrNull() ?: 0) > (value.toIntOrNull() ?: 0)
            ConditionOperator.LESS_THAN -> (fieldValue?.toIntOrNull() ?: 0) < (value.toIntOrNull() ?: 0)
            ConditionOperator.CONTAINS -> fieldValue?.contains(value) == true
        }
    }
}

enum class ConditionOperator { EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, CONTAINS }

@Embeddable
data class Action(
    val type: ActionType,
    val config: String  // JSON
) {
    fun execute(context: ExecutionContext) {
        when (type) {
            ActionType.SEND_SLACK -> context.sendSlack(config)
            ActionType.SEND_EMAIL -> context.sendEmail(config)
            ActionType.CHANGE_ORDER_STATUS -> context.changeOrderStatus(config)
            ActionType.ADJUST_STOCK -> context.adjustStock(config)
        }
    }
}

enum class ActionType {
    SEND_SLACK,
    SEND_EMAIL,
    CHANGE_ORDER_STATUS,
    ADJUST_STOCK
}
```

---

### 2.9 Integration Context (통합/인터페이스)

**책임**: 웹훅 수신, API 로그, 외부 연동

```kotlin
// ========================
// Aggregate: WebhookLog (MongoDB)
// ========================
@Document(collection = "webhook_logs")
class WebhookLog(
    @Id val id: String,
    val companyId: String,
    val source: String,           // 채널명
    val eventType: String,        // "ORDER.PAID"
    val httpStatus: Int,
    val latencyMs: Long,
    val requestPayload: String,   // JSON
    val responsePayload: String?,
    val errorMessage: String?,
    var retryCount: Int = 0,
    val createdAt: Instant
) {
    fun needsRetry(): Boolean {
        return httpStatus >= 400 && retryCount < 3
    }

    fun incrementRetry() {
        retryCount++
    }
}

// ========================
// Aggregate: ApiLog (MongoDB)
// ========================
@Document(collection = "api_logs")
class ApiLog(
    @Id val id: String,
    val companyId: String,
    val channelId: String,
    val direction: ApiDirection,  // INBOUND / OUTBOUND
    val endpoint: String,
    val method: String,
    val httpStatus: Int,
    val latencyMs: Long,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val responseBody: String?,
    val createdAt: Instant
)

enum class ApiDirection { INBOUND, OUTBOUND }

// ========================
// Aggregate: DomainEvent (MongoDB - Event Sourcing용)
// ========================
@Document(collection = "domain_events")
class DomainEventLog(
    @Id val id: String,
    val aggregateId: String,
    val aggregateType: String,
    val eventType: String,
    val eventData: String,        // JSON
    val version: Long,
    val createdAt: Instant,
    val createdBy: String
)
```

---

### 2.10 Strategy Context (전략 인텔리전스)

**책임**: 운영 전략 시뮬레이션, 국가별 진출 준비도 분석

```kotlin
// ========================
// Aggregate: OperationsStrategy
// ========================
@Entity
class OperationsStrategy(
    @Id val id: StrategyId,
    val companyId: CompanyId,
    var name: String,
    var description: String?,

    // 시뮬레이션 가중치
    var weights: SimulationWeights,

    // 시뮬레이션 결과
    @Embedded
    var simulationResult: SimulationResult?,

    var status: StrategyStatus,
    val createdAt: Instant,
    var updatedAt: Instant
) {
    fun runSimulation(context: SimulationContext): SimulationResult {
        // 가중치 기반 시뮬레이션 실행
        val result = context.calculate(weights)

        simulationResult = result
        updatedAt = Instant.now()

        registerEvent(StrategySimulatedEvent(id, result))
        return result
    }

    fun activate() {
        require(simulationResult != null) { "Cannot activate without simulation" }
        status = StrategyStatus.ACTIVE
    }
}

@Embeddable
data class StrategyId(val value: UUID)

@Embeddable
data class SimulationWeights(
    val costReduction: Int,               // 물류비용 절감 가중치 (%)
    val leadTime: Int,                    // 리드타임 최단화 가중치 (%)
    val stockBalance: Int,                // 재고 분산 가중치 (%)
    val carbonEmission: Int               // 탄소 배출 저감 가중치 (%)
) {
    init {
        // 가중치 합계는 100-400% 범위 (각 항목 0-100)
        require(costReduction in 0..100 && leadTime in 0..100 &&
                stockBalance in 0..100 && carbonEmission in 0..100) {
            "Each weight must be between 0 and 100"
        }
    }
}

@Embeddable
data class SimulationResult(
    val efficiencyScore: BigDecimal,      // 운영 효율성 점수
    val costSaving: BigDecimal,           // 예상 비용 절감액
    val avgLeadTime: BigDecimal,          // 예상 평균 리드타임 (일)
    val recommendation: String,           // 권장 액션
    val calculatedAt: Instant
)

enum class StrategyStatus { DRAFT, SIMULATED, ACTIVE, ARCHIVED }

// ========================
// Aggregate: GlobalReadiness (국가별 진출 준비도)
// ========================
@Entity
class GlobalReadiness(
    @Id val id: ReadinessId,
    val companyId: CompanyId,
    val countryCode: String,              // "US", "JP", "CN"
    var countryName: String,

    // 준비도 점수
    var regulatoryScore: Int,             // 규제 준비도 (0-100)
    var logisticsScore: Int,              // 물류 준비도 (0-100)
    var marketScore: Int,                 // 시장 준비도 (0-100)
    var financialScore: Int,              // 재무 준비도 (0-100)

    var overallReadiness: Int,            // 종합 준비도 (0-100)
    var status: ReadinessStatus,

    // 준비 항목 체크리스트
    @ElementCollection
    val checklist: MutableList<ReadinessItem> = mutableListOf(),

    val createdAt: Instant,
    var updatedAt: Instant
) {
    fun recalculateReadiness() {
        overallReadiness = (regulatoryScore + logisticsScore + marketScore + financialScore) / 4

        status = when {
            overallReadiness >= 80 -> ReadinessStatus.READY
            overallReadiness >= 50 -> ReadinessStatus.PREPARING
            else -> ReadinessStatus.NOT_READY
        }

        updatedAt = Instant.now()
    }

    fun completeChecklistItem(itemId: String) {
        checklist.find { it.id == itemId }?.completed = true
        recalculateReadiness()
    }
}

@Embeddable
data class ReadinessId(val value: UUID)

@Embeddable
data class ReadinessItem(
    val id: String,
    val category: ReadinessCategory,
    val description: String,
    var completed: Boolean = false
)

enum class ReadinessCategory {
    REGULATORY,      // 규제
    LOGISTICS,       // 물류
    MARKET,          // 시장
    FINANCIAL        // 재무
}

enum class ReadinessStatus { NOT_READY, PREPARING, READY, LAUNCHED }

// ========================
// Entity: StrategyDeployment (전략 배포 이력)
// ========================
@Entity
class StrategyDeployment(
    @Id val id: DeploymentId,
    val strategyId: StrategyId,
    val companyId: CompanyId,
    val deployedAt: Instant,
    val deployedBy: UserId,
    var status: DeploymentStatus,
    val targetRegion: String?,            // 배포 대상 지역
    val notes: String?
)

@Embeddable
data class DeploymentId(val value: UUID)

enum class DeploymentStatus { DEPLOYED, ROLLED_BACK, FAILED }
```

---

## 3. 멀티모듈 구조

### 3.1 프로젝트 구조 (옵션 B - 레이어 + 도메인)

```
oms-backend/
├── build.gradle.kts
├── settings.gradle.kts
│
├── core/                          # 공통 모듈
│   ├── core-domain/               # 공통 도메인 (Value Objects, Exceptions)
│   │   └── src/main/kotlin/
│   │       └── com/oms/core/
│   │           ├── domain/
│   │           │   ├── Money.kt
│   │           │   ├── Address.kt
│   │           │   └── AuditableEntity.kt
│   │           ├── exception/
│   │           │   ├── DomainException.kt
│   │           │   └── ErrorCode.kt
│   │           └── event/
│   │               └── DomainEvent.kt
│   │
│   └── core-infra/                # 공통 인프라 (Config, Utils)
│       └── src/main/kotlin/
│           └── com/oms/core/
│               ├── config/
│               │   ├── JpaConfig.kt
│               │   ├── MongoConfig.kt
│               │   └── SecurityConfig.kt
│               └── util/
│
├── domain/                        # 도메인 모듈들
│   ├── domain-identity/
│   │   └── src/main/kotlin/
│   │       └── com/oms/identity/
│   │           ├── domain/
│   │           │   ├── Company.kt
│   │           │   ├── User.kt
│   │           │   └── repository/
│   │           │       ├── CompanyRepository.kt
│   │           │       └── UserRepository.kt
│   │           └── service/
│   │               └── UserDomainService.kt
│   │
│   ├── domain-catalog/
│   │   └── src/main/kotlin/
│   │       └── com/oms/catalog/
│   │           ├── domain/
│   │           │   ├── Product.kt
│   │           │   ├── Barcode.kt
│   │           │   ├── CustomsStrategy.kt
│   │           │   └── repository/
│   │           └── service/
│   │
│   ├── domain-channel/
│   │   └── src/main/kotlin/
│   │       └── com/oms/channel/
│   │           ├── domain/
│   │           │   ├── Channel.kt
│   │           │   ├── Warehouse.kt
│   │           │   ├── ChannelWarehouseMapping.kt
│   │           │   └── repository/
│   │           └── service/
│   │
│   ├── domain-order/
│   │   └── src/main/kotlin/
│   │       └── com/oms/order/
│   │           ├── domain/
│   │           │   ├── Order.kt
│   │           │   ├── OrderItem.kt
│   │           │   ├── Shipping.kt
│   │           │   ├── OrderStatusMachine.kt
│   │           │   └── repository/
│   │           └── service/
│   │               └── OrderRoutingService.kt
│   │
│   ├── domain-inventory/
│   │   └── src/main/kotlin/
│   │       └── com/oms/inventory/
│   │           ├── domain/
│   │           │   ├── Stock.kt
│   │           │   ├── StockMovement.kt
│   │           │   └── repository/
│   │           └── service/
│   │               └── StockAllocationService.kt
│   │
│   ├── domain-claim/
│   │   └── src/main/kotlin/
│   │       └── com/oms/claim/
│   │
│   ├── domain-settlement/
│   │   └── src/main/kotlin/
│   │       └── com/oms/settlement/
│   │
│   └── domain-automation/
│       └── src/main/kotlin/
│           └── com/oms/automation/
│
├── infrastructure/                # 인프라 모듈
│   ├── infra-mysql/               # MySQL 구현체
│   │   └── src/main/kotlin/
│   │       └── com/oms/infra/mysql/
│   │           ├── entity/        # JPA Entity
│   │           ├── repository/    # JPA Repository 구현
│   │           └── mapper/        # Domain ↔ Entity 변환
│   │
│   ├── infra-mongo/               # MongoDB 구현체
│   │   └── src/main/kotlin/
│   │       └── com/oms/infra/mongo/
│   │           ├── document/
│   │           └── repository/
│   │
│   ├── infra-redis/               # Redis (캐시, 세션)
│   │
│   └── infra-external/            # 외부 API 연동
│       └── src/main/kotlin/
│           └── com/oms/infra/external/
│               ├── channel/       # 채널 API 클라이언트
│               │   ├── NaverClient.kt
│               │   ├── CoupangClient.kt
│               │   └── AmazonClient.kt
│               ├── carrier/       # 택배사 API
│               │   └── CJClient.kt
│               └── notification/  # 알림 서비스
│                   ├── SlackClient.kt
│                   └── EmailClient.kt
│
├── application/                   # 애플리케이션 서비스
│   └── src/main/kotlin/
│       └── com/oms/application/
│           ├── order/
│           │   ├── OrderApplicationService.kt
│           │   ├── dto/
│           │   │   ├── OrderRequest.kt
│           │   │   └── OrderResponse.kt
│           │   └── mapper/
│           ├── product/
│           ├── inventory/
│           └── ...
│
└── api/                           # API 모듈 (진입점)
    └── src/main/kotlin/
        └── com/oms/api/
            ├── OmsApplication.kt
            ├── controller/
            │   ├── OrderController.kt
            │   ├── ProductController.kt
            │   └── ...
            ├── security/
            │   └── JwtAuthFilter.kt
            └── webhook/
                └── WebhookController.kt
```

### 3.2 모듈 의존성

```kotlin
// settings.gradle.kts
rootProject.name = "oms-backend"

include(
    // Core
    ":core:core-domain",
    ":core:core-infra",

    // Domain
    ":domain:domain-identity",
    ":domain:domain-catalog",
    ":domain:domain-channel",
    ":domain:domain-order",
    ":domain:domain-inventory",
    ":domain:domain-claim",
    ":domain:domain-settlement",
    ":domain:domain-automation",

    // Infrastructure
    ":infrastructure:infra-mysql",
    ":infrastructure:infra-mongo",
    ":infrastructure:infra-redis",
    ":infrastructure:infra-external",

    // Application & API
    ":application",
    ":api"
)
```

```kotlin
// 의존성 다이어그램
//
//                    ┌─────────┐
//                    │   api   │
//                    └────┬────┘
//                         │
//                    ┌────▼────┐
//                    │application│
//                    └────┬────┘
//           ┌─────────────┼─────────────┐
//           │             │             │
//    ┌──────▼──────┐ ┌────▼────┐ ┌──────▼──────┐
//    │domain-order │ │domain-* │ │  infra-*    │
//    └──────┬──────┘ └────┬────┘ └──────┬──────┘
//           │             │             │
//           └─────────────┼─────────────┘
//                         │
//                   ┌─────▼─────┐
//                   │core-domain│
//                   └───────────┘
```

### 3.3 모듈별 build.gradle.kts 예시

```kotlin
// domain/domain-order/build.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":core:core-domain"))

    // JPA (interface만)
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
}

// 도메인 모듈은 인프라에 의존하지 않음 (DIP)
```

```kotlin
// infrastructure/infra-mysql/build.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":domain:domain-order"))
    implementation(project(":domain:domain-catalog"))
    // ... 다른 도메인 모듈들

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
}
```

```kotlin
// api/build.gradle.kts
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-infra"))
    implementation(project(":application"))
    implementation(project(":infrastructure:infra-mysql"))
    implementation(project(":infrastructure:infra-mongo"))
    implementation(project(":infrastructure:infra-redis"))
    implementation(project(":infrastructure:infra-external"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

---

## 4. 멀티테넌시 보안

### 4.1 Hibernate Filter 적용

**목적**: 회사(Company) 경계를 데이터베이스 수준에서 자동 격리

```kotlin
// core-infra/config/TenantFilter.kt
package com.oms.core.config

import jakarta.persistence.*
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef

@FilterDef(
    name = "companyFilter",
    parameters = [ParamDef(name = "companyId", type = String::class)]
)
@Filter(name = "companyFilter", condition = "company_id = :companyId")
@MappedSuperclass
abstract class CompanyAwareEntity {
    @Column(name = "company_id", nullable = false, updatable = false)
    lateinit var companyId: String  // CompanyId.value

    @PrePersist
    fun setCompanyId() {
        if (!::companyId.isInitialized) {
            companyId = SecurityContextHolder.getCurrentCompanyId()
        }
    }
}
```

### 4.2 Filter 자동 활성화

```kotlin
// core-infra/config/TenantFilterAspect.kt
package com.oms.core.config

import jakarta.persistence.EntityManager
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.hibernate.Session
import org.springframework.stereotype.Component

@Aspect
@Component
class TenantFilterAspect(
    private val entityManager: EntityManager
) {
    @Before("execution(* com.oms..repository.*.*(..))")
    fun enableCompanyFilter() {
        val session = entityManager.unwrap(Session::class.java)
        val filter = session.enableFilter("companyFilter")
        filter.setParameter("companyId", SecurityContextHolder.getCurrentCompanyId())
    }
}

// SecurityContextHolder.kt
object SecurityContextHolder {
    private val companyIdThreadLocal = ThreadLocal<String>()

    fun setCurrentCompanyId(companyId: String) {
        companyIdThreadLocal.set(companyId)
    }

    fun getCurrentCompanyId(): String {
        return companyIdThreadLocal.get()
            ?: throw IllegalStateException("No company context available")
    }

    fun clear() {
        companyIdThreadLocal.remove()
    }
}
```

### 4.3 Repository 레벨 보안

```kotlin
// 모든 Entity는 CompanyAwareEntity를 상속
@Entity
class Product(
    @Id val id: ProductId,
    val sku: Sku,
    var name: LocalizedString,
    // ...
) : CompanyAwareEntity()  // 상속 추가

// Repository는 자동으로 company_id 필터 적용
interface ProductRepository : JpaRepository<Product, ProductId> {
    // SELECT * FROM product WHERE company_id = :currentCompanyId AND sku = :sku
    fun findBySku(sku: Sku): Product?
}
```

### 4.4 크로스 컴퍼니 접근 제어

```kotlin
// 다른 회사 데이터 접근이 필요한 경우 (예: 관리자 기능)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BypassTenantFilter

@Aspect
@Component
class TenantFilterBypassAspect(
    private val entityManager: EntityManager
) {
    @Around("@annotation(BypassTenantFilter)")
    fun bypassFilter(joinPoint: ProceedingJoinPoint): Any? {
        val session = entityManager.unwrap(Session::class.java)
        session.disableFilter("companyFilter")
        try {
            return joinPoint.proceed()
        } finally {
            session.enableFilter("companyFilter")
        }
    }
}
```

### 4.5 보안 검증 테스트

```kotlin
@SpringBootTest
class TenantIsolationTest {
    @Test
    fun `다른 회사 데이터는 조회되지 않아야 함`() {
        // Given: 회사 A의 상품 생성
        SecurityContextHolder.setCurrentCompanyId("company-a")
        val productA = productRepository.save(Product(/* ... */))

        // When: 회사 B로 컨텍스트 변경
        SecurityContextHolder.setCurrentCompanyId("company-b")

        // Then: 회사 A의 상품은 조회되지 않음
        assertNull(productRepository.findById(productA.id))
    }
}
```

---

## 5. 도메인 이벤트

### 5.1 이벤트 목록

| 도메인 | 이벤트 | 설명 |
|--------|--------|------|
| Order | `OrderCreatedEvent` | 주문 생성됨 |
| Order | `OrderStatusChangedEvent` | 주문 상태 변경됨 |
| Order | `OrderCancelledEvent` | 주문 취소됨 |
| Order | `OrderShippedEvent` | 주문 출고됨 |
| Inventory | `StockReceivedEvent` | 재고 입고됨 |
| Inventory | `StockReservedEvent` | 재고 예약됨 |
| Inventory | `StockShippedEvent` | 재고 출고됨 |
| Inventory | `StockLowEvent` | 재고 부족 감지 |
| Claim | `ClaimCreatedEvent` | 클레임 생성됨 |
| Claim | `ClaimCompletedEvent` | 클레임 처리 완료 |
| Automation | `RuleExecutedEvent` | 자동화 규칙 실행됨 |

### 5.2 이벤트 발행 메커니즘

**선택된 방식**: Spring Application Events + MongoDB 영속화

**이유**:
- 단일 JVM 내 동기식 이벤트 전파 (Spring Events)
- 이벤트 영속화로 재처리 가능 (MongoDB)
- 추후 Kafka로 확장 용이

### 5.3 이벤트 발행 구현

```kotlin
// core-domain/event/DomainEvent.kt
package com.oms.core.event

import java.time.Instant
import java.util.UUID

abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now()
)

// core-domain/event/AggregateRoot.kt
package com.oms.core.event

abstract class AggregateRoot {
    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    protected fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun clearEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }
}

// core-infra/event/DomainEventPublisher.kt
package com.oms.core.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val domainEventRepository: DomainEventRepository
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishEvents(aggregate: AggregateRoot) {
        aggregate.clearEvents().forEach { event ->
            // MongoDB에 영속화
            domainEventRepository.save(event.toDocument())
            // Spring Event로 발행
            applicationEventPublisher.publishEvent(event)
        }
    }
}
```

### 5.4 이벤트 구독

```kotlin
// domain-inventory/service/InventoryEventHandler.kt
package com.oms.inventory.service

import com.oms.order.event.OrderCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class InventoryEventHandler(
    private val stockAllocationService: StockAllocationService
) {
    @EventListener
    fun handle(event: OrderCreatedEvent) {
        // 재고 예약 로직
        stockAllocationService.reserveStock(event.orderId, event.items)
    }
}
```

### 5.5 이벤트 흐름 예시

```
[주문 생성 흐름]

1. OrderCreatedEvent 발행
   ↓
2. Inventory Context 구독
   → StockReservedEvent 발행 (재고 예약)
   ↓
3. Automation Context 구독
   → 조건 충족 시 RuleExecutedEvent 발행 (Slack 알림)
   ↓
4. Integration Context 구독
   → 채널 API로 주문 확인 전송
```

---

## 6. 다음 단계

### Phase 3: ERD 설계
- [ ] MySQL 테이블 설계
- [ ] MongoDB 컬렉션 설계
- [ ] 인덱스 전략 정의
- [ ] 마이그레이션 스크립트

