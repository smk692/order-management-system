package com.oms.catalog.domain

import jakarta.persistence.*
import java.time.Instant

/**
 * CustomsStrategy child entity
 * Contains country-specific customs information for a product
 */
@Entity
@Table(
    name = "product_customs_strategy",
    indexes = [
        Index(name = "idx_customs_product", columnList = "product_id"),
        Index(name = "idx_customs_country", columnList = "country_code")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_customs_product_country", columnNames = ["product_id", "country_code"])
    ]
)
class CustomsStrategy private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,

    @Column(name = "country_code", nullable = false, length = 3)
    val countryCode: String,

    @Column(name = "local_hs_code", nullable = false, length = 20)
    var localHsCode: String,

    @Column(name = "invoice_name", nullable = false, length = 200)
    var invoiceName: String,

    @Column(name = "duty_rate", length = 20)
    var dutyRate: String? = null,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "product_customs_required_docs",
        joinColumns = [JoinColumn(name = "customs_strategy_id")]
    )
    @Column(name = "document_name", length = 100)
    private val _requiredDocs: MutableList<String> = mutableListOf(),

    @Column(name = "compliance_alert", length = 500)
    var complianceAlert: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {

    val requiredDocs: List<String> get() = _requiredDocs.toList()

    companion object {
        fun create(
            countryCode: String,
            localHsCode: String,
            invoiceName: String,
            dutyRate: String? = null,
            requiredDocs: List<String> = emptyList(),
            complianceAlert: String? = null
        ): CustomsStrategy {
            require(countryCode.isNotBlank()) { "Country code cannot be blank" }
            require(countryCode.length in 2..3) { "Country code must be 2-3 characters" }
            require(localHsCode.isNotBlank()) { "Local HS code cannot be blank" }
            require(invoiceName.isNotBlank()) { "Invoice name cannot be blank" }

            val strategy = CustomsStrategy(
                countryCode = countryCode.uppercase(),
                localHsCode = localHsCode,
                invoiceName = invoiceName,
                dutyRate = dutyRate,
                complianceAlert = complianceAlert
            )
            strategy._requiredDocs.addAll(requiredDocs)

            return strategy
        }
    }

    internal fun assignToProduct(product: Product) {
        this.product = product
    }

    fun addRequiredDocument(document: String) {
        require(document.isNotBlank()) { "Document name cannot be blank" }
        if (!_requiredDocs.contains(document)) {
            _requiredDocs.add(document)
            updatedAt = Instant.now()
        }
    }

    fun removeRequiredDocument(document: String) {
        if (_requiredDocs.remove(document)) {
            updatedAt = Instant.now()
        }
    }

    fun updateLocalHsCode(newHsCode: String) {
        require(newHsCode.isNotBlank()) { "Local HS code cannot be blank" }
        this.localHsCode = newHsCode
        updatedAt = Instant.now()
    }

    fun updateInvoiceName(newInvoiceName: String) {
        require(newInvoiceName.isNotBlank()) { "Invoice name cannot be blank" }
        this.invoiceName = newInvoiceName
        updatedAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomsStrategy) return false
        if (id == 0L && other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)
}
