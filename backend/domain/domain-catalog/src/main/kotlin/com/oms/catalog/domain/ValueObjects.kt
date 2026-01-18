package com.oms.catalog.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.math.BigDecimal

/**
 * Category value object
 * Represents a product category path
 */
@Embeddable
data class Category(
    @Column(name = "category_path")
    val path: String
) {
    val depth: Int get() = path.split(" > ").size
    val leaf: String get() = path.split(" > ").last()
    val root: String get() = path.split(" > ").first()

    fun getParentPath(): String? {
        val parts = path.split(" > ")
        return if (parts.size > 1) {
            parts.dropLast(1).joinToString(" > ")
        } else null
    }

    override fun toString(): String = path
}

/**
 * Dimensions value object
 * Represents product physical dimensions
 */
@Embeddable
data class Dimensions(
    @Column(name = "dim_width")
    val width: BigDecimal,

    @Column(name = "dim_length")
    val length: BigDecimal,

    @Column(name = "dim_height")
    val height: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "dim_unit")
    val unit: DimensionUnit = DimensionUnit.CM
) {
    init {
        require(width > BigDecimal.ZERO) { "Width must be positive" }
        require(length > BigDecimal.ZERO) { "Length must be positive" }
        require(height > BigDecimal.ZERO) { "Height must be positive" }
    }

    /**
     * Calculate volume in cubic units
     */
    fun volume(): BigDecimal = width.multiply(length).multiply(height)

    /**
     * Convert to different unit
     */
    fun convertTo(targetUnit: DimensionUnit): Dimensions {
        if (unit == targetUnit) return this

        val factor = when {
            unit == DimensionUnit.CM && targetUnit == DimensionUnit.MM -> BigDecimal.TEN
            unit == DimensionUnit.MM && targetUnit == DimensionUnit.CM -> BigDecimal("0.1")
            else -> BigDecimal.ONE
        }

        return Dimensions(
            width = width.multiply(factor),
            length = length.multiply(factor),
            height = height.multiply(factor),
            unit = targetUnit
        )
    }
}

/**
 * Dimension unit enum
 */
enum class DimensionUnit {
    CM,  // Centimeter
    MM   // Millimeter
}

/**
 * Weight value object
 * Represents product weight (net and gross)
 */
@Embeddable
data class Weight(
    @Column(name = "weight_net")
    val net: BigDecimal,

    @Column(name = "weight_gross")
    val gross: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit")
    val unit: WeightUnit = WeightUnit.KG
) {
    init {
        require(net > BigDecimal.ZERO) { "Net weight must be positive" }
        require(gross >= net) { "Gross weight must be >= net weight" }
    }

    /**
     * Calculate packaging weight
     */
    fun packagingWeight(): BigDecimal = gross.subtract(net)

    /**
     * Convert to different unit
     */
    fun convertTo(targetUnit: WeightUnit): Weight {
        if (unit == targetUnit) return this

        val factor = when {
            unit == WeightUnit.KG && targetUnit == WeightUnit.G -> BigDecimal("1000")
            unit == WeightUnit.G && targetUnit == WeightUnit.KG -> BigDecimal("0.001")
            else -> BigDecimal.ONE
        }

        return Weight(
            net = net.multiply(factor),
            gross = gross.multiply(factor),
            unit = targetUnit
        )
    }
}

/**
 * Weight unit enum
 */
enum class WeightUnit {
    KG,  // Kilogram
    G    // Gram
}

/**
 * Logistics info value object
 * Contains logistics-related flags for a product
 */
@Embeddable
data class LogisticsInfo(
    @Enumerated(EnumType.STRING)
    @Column(name = "temp_management")
    val tempManagement: TemperatureType = TemperatureType.NORMAL,

    @Column(name = "shelf_life_mgmt")
    val shelfLifeManagement: Boolean = false,

    @Column(name = "serial_no_mgmt")
    val serialNumberManagement: Boolean = false,

    @Column(name = "is_dangerous")
    val isDangerous: Boolean = false,

    @Column(name = "is_fragile")
    val isFragile: Boolean = false,

    @Column(name = "is_high_value")
    val isHighValue: Boolean = false,

    @Column(name = "is_non_standard")
    val isNonStandard: Boolean = false
) {
    /**
     * Check if product requires special handling
     */
    fun requiresSpecialHandling(): Boolean =
        tempManagement != TemperatureType.NORMAL ||
                isDangerous ||
                isFragile ||
                isHighValue

    /**
     * Check if product requires cold chain
     */
    fun requiresColdChain(): Boolean =
        tempManagement in listOf(
            TemperatureType.COLD,
            TemperatureType.FREEZING,
            TemperatureType.CRYOGENIC
        )
}

/**
 * Temperature type enum
 * Represents temperature control requirements
 */
enum class TemperatureType {
    NORMAL,             // No temperature control required
    TEMPERATURE_CONTROL, // General temperature control
    COLD,               // Cold storage (2-8°C)
    FREEZING,           // Frozen (-18°C)
    CRYOGENIC           // Ultra-cold (-80°C or below)
}
