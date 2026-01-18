package com.oms.catalog.domain

import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.domain.LocalizedString
import com.oms.core.domain.Money
import com.oms.core.event.DomainEvent
import com.oms.catalog.domain.event.ProductCreatedEvent
import com.oms.catalog.domain.event.ProductStatusChangedEvent
import com.oms.catalog.domain.event.BarcodeAddedEvent
import jakarta.persistence.*
import java.util.UUID

/**
 * Product aggregate root
 * Represents a product master in the catalog
 */
@Entity
@Table(
    name = "product",
    indexes = [
        Index(name = "idx_product_company", columnList = "company_id"),
        Index(name = "idx_product_sku", columnList = "sku"),
        Index(name = "idx_product_status", columnList = "status")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_product_company_sku", columnNames = ["company_id", "sku"])
    ]
)
class Product private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "sku", nullable = false, length = 50)
    val sku: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "ko", column = Column(name = "name_ko", nullable = false, length = 200)),
        AttributeOverride(name = "en", column = Column(name = "name_en", length = 200))
    )
    var name: LocalizedString,

    @Column(name = "brand", length = 100)
    var brand: String?,

    @Embedded
    @AttributeOverride(name = "path", column = Column(name = "category_path", length = 500))
    var category: Category?,

    @Enumerated(EnumType.STRING)
    @Column(name = "uom", nullable = false, length = 10)
    var uom: UnitOfMeasure,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "base_price", precision = 19, scale = 2)),
        AttributeOverride(name = "currency", column = Column(name = "base_price_currency", length = 3))
    )
    var basePrice: Money?,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ProductStatus = ProductStatus.ACTIVE,

    // Dimensions
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "width", column = Column(name = "dim_width", precision = 10, scale = 2)),
        AttributeOverride(name = "length", column = Column(name = "dim_length", precision = 10, scale = 2)),
        AttributeOverride(name = "height", column = Column(name = "dim_height", precision = 10, scale = 2)),
        AttributeOverride(name = "unit", column = Column(name = "dim_unit", length = 5))
    )
    var dimensions: Dimensions? = null,

    // Weight
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "net", column = Column(name = "weight_net", precision = 10, scale = 3)),
        AttributeOverride(name = "gross", column = Column(name = "weight_gross", precision = 10, scale = 3)),
        AttributeOverride(name = "unit", column = Column(name = "weight_unit", length = 5))
    )
    var weight: Weight? = null,

    // Logistics Info
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "tempManagement", column = Column(name = "temp_management", length = 20)),
        AttributeOverride(name = "shelfLifeManagement", column = Column(name = "shelf_life_mgmt")),
        AttributeOverride(name = "serialNumberManagement", column = Column(name = "serial_no_mgmt")),
        AttributeOverride(name = "isDangerous", column = Column(name = "is_dangerous")),
        AttributeOverride(name = "isFragile", column = Column(name = "is_fragile")),
        AttributeOverride(name = "isHighValue", column = Column(name = "is_high_value")),
        AttributeOverride(name = "isNonStandard", column = Column(name = "is_non_standard"))
    )
    var logisticsInfo: LogisticsInfo = LogisticsInfo(),

    // Additional Attributes
    @Column(name = "color", length = 50)
    var color: String? = null,

    @Column(name = "owner_name", length = 100)
    var ownerName: String? = null,

    @Column(name = "customer_goods_no", length = 50)
    var customerGoodsNo: String? = null,

    // Customs & Compliance Info
    @Column(name = "hs_code", length = 20)
    var hsCode: String? = null,

    @Column(name = "country_of_origin", length = 50)
    var countryOfOrigin: String? = null,

    @Column(name = "material", length = 200)
    var material: String? = null,

    @Column(name = "manufacturer", length = 100)
    var manufacturer: String? = null,

    @Column(name = "manufacturer_address", length = 500)
    var manufacturerAddress: String? = null

) : CompanyAwareEntity() {

    // Child entities
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _barcodes: MutableList<Barcode> = mutableListOf()
    val barcodes: List<Barcode> get() = _barcodes.toList()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _customsStrategies: MutableList<CustomsStrategy> = mutableListOf()
    val customsStrategies: List<CustomsStrategy> get() = _customsStrategies.toList()

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        fun create(
            companyId: String,
            sku: String,
            nameKo: String,
            nameEn: String? = null,
            uom: UnitOfMeasure = UnitOfMeasure.PCS
        ): Product {
            require(sku.isNotBlank()) { "SKU cannot be blank" }
            require(nameKo.isNotBlank()) { "Product name (KO) cannot be blank" }

            val product = Product(
                id = UUID.randomUUID().toString(),
                sku = sku,
                name = LocalizedString(ko = nameKo, en = nameEn),
                brand = null,
                category = null,
                uom = uom,
                basePrice = null,
                status = ProductStatus.ACTIVE
            )
            product.assignToCompany(companyId)

            product.registerEvent(
                ProductCreatedEvent(
                    productId = product.id,
                    companyId = companyId,
                    sku = sku,
                    name = nameKo
                )
            )

            return product
        }
    }

    /**
     * Add a barcode to the product
     */
    fun addBarcode(code: String, isMain: Boolean = false) {
        require(code.isNotBlank()) { "Barcode cannot be blank" }

        // Check for duplicate
        require(_barcodes.none { it.code == code }) { "Barcode already exists" }

        // If this is the main barcode, unset other main barcodes
        if (isMain) {
            _barcodes.forEach { it.isMain = false }
        }

        val barcode = Barcode.create(this, code, isMain)
        _barcodes.add(barcode)

        registerEvent(
            BarcodeAddedEvent(
                productId = id,
                companyId = companyId,
                barcode = code,
                isMain = isMain
            )
        )
    }

    /**
     * Set a barcode as the main barcode
     */
    fun setMainBarcode(code: String) {
        require(_barcodes.any { it.code == code }) { "Barcode not found: $code" }
        _barcodes.forEach { it.isMain = (it.code == code) }
    }

    /**
     * Remove a barcode
     */
    fun removeBarcode(code: String) {
        val barcode = _barcodes.find { it.code == code }
            ?: throw IllegalArgumentException("Barcode not found: $code")

        _barcodes.remove(barcode)
    }

    /**
     * Get the main barcode
     */
    fun getMainBarcode(): Barcode? = _barcodes.find { it.isMain }

    /**
     * Add or update a customs strategy for a country
     */
    fun addCustomsStrategy(strategy: CustomsStrategy) {
        // Remove existing strategy for the same country
        _customsStrategies.removeIf { it.countryCode == strategy.countryCode }
        strategy.assignToProduct(this)
        _customsStrategies.add(strategy)
    }

    /**
     * Get customs strategy for a specific country
     */
    fun getCustomsStrategy(countryCode: String): CustomsStrategy? =
        _customsStrategies.find { it.countryCode == countryCode }

    /**
     * Activate the product
     */
    fun activate() {
        if (status != ProductStatus.ACTIVE) {
            val previousStatus = status
            status = ProductStatus.ACTIVE
            registerEvent(
                ProductStatusChangedEvent(
                    productId = id,
                    companyId = companyId,
                    previousStatus = previousStatus,
                    newStatus = status
                )
            )
        }
    }

    /**
     * Deactivate the product
     */
    fun deactivate() {
        if (status != ProductStatus.INACTIVE) {
            val previousStatus = status
            status = ProductStatus.INACTIVE
            registerEvent(
                ProductStatusChangedEvent(
                    productId = id,
                    companyId = companyId,
                    previousStatus = previousStatus,
                    newStatus = status
                )
            )
        }
    }

    /**
     * Mark as out of stock
     */
    fun markOutOfStock() {
        if (status != ProductStatus.OUT_OF_STOCK) {
            val previousStatus = status
            status = ProductStatus.OUT_OF_STOCK
            registerEvent(
                ProductStatusChangedEvent(
                    productId = id,
                    companyId = companyId,
                    previousStatus = previousStatus,
                    newStatus = status
                )
            )
        }
    }

    fun isActive(): Boolean = status == ProductStatus.ACTIVE

    fun canBeOrdered(): Boolean = status == ProductStatus.ACTIVE

    private fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun clearEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Product status enum
 */
enum class ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK
}

/**
 * Unit of Measure enum
 */
enum class UnitOfMeasure {
    PCS,  // Pieces
    SET,  // Set
    BOX,  // Box
    KG,   // Kilogram
    EA,   // Each
    TAI   // 台 (Taiwan unit)
}
