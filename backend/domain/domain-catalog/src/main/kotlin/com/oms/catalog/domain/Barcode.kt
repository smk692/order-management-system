package com.oms.catalog.domain

import jakarta.persistence.*
import java.time.Instant

/**
 * Barcode child entity
 * Belongs to Product aggregate
 */
@Entity
@Table(
    name = "product_barcode",
    indexes = [
        Index(name = "idx_barcode_product", columnList = "product_id"),
        Index(name = "idx_barcode_code", columnList = "code")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_barcode_product_code", columnNames = ["product_id", "code"])
    ]
)
class Barcode private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "code", nullable = false, length = 50)
    val code: String,

    @Column(name = "is_main", nullable = false)
    var isMain: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {

    companion object {
        fun create(product: Product, code: String, isMain: Boolean = false): Barcode {
            require(code.isNotBlank()) { "Barcode code cannot be blank" }

            return Barcode(
                product = product,
                code = code,
                isMain = isMain
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Barcode) return false
        if (id == 0L && other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)
}
