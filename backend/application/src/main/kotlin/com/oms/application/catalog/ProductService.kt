package com.oms.application.catalog

import com.oms.catalog.domain.*
import com.oms.catalog.repository.ProductRepository
import com.oms.core.domain.LocalizedString
import com.oms.core.domain.Money
import com.oms.core.exception.BusinessRuleException
import com.oms.core.exception.EntityNotFoundException
import com.oms.core.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Application service for Product operations
 */
@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {

    /**
     * Create a new product
     */
    fun createProduct(command: CreateProductCommand): ProductResult {
        // Check for duplicate SKU
        if (productRepository.existsByCompanyIdAndSku(command.companyId, command.sku)) {
            throw BusinessRuleException(
                "SKU already exists: ${command.sku}",
                ErrorCode.DUPLICATE_SKU
            )
        }

        val product = Product.create(
            companyId = command.companyId,
            sku = command.sku,
            nameKo = command.nameKo,
            nameEn = command.nameEn,
            uom = command.uom
        )

        // Set optional fields
        command.brand?.let { product.brand = it }
        command.category?.let { product.category = Category(it) }
        command.basePrice?.let { product.basePrice = it }
        command.dimensions?.let { product.dimensions = it }
        command.weight?.let { product.weight = it }
        command.logisticsInfo?.let { product.logisticsInfo = it }

        // Additional attributes
        command.color?.let { product.color = it }
        command.ownerName?.let { product.ownerName = it }
        command.customerGoodsNo?.let { product.customerGoodsNo = it }

        // Customs info
        command.hsCode?.let { product.hsCode = it }
        command.countryOfOrigin?.let { product.countryOfOrigin = it }
        command.material?.let { product.material = it }
        command.manufacturer?.let { product.manufacturer = it }
        command.manufacturerAddress?.let { product.manufacturerAddress = it }

        val savedProduct = productRepository.save(product)
        return toProductResult(savedProduct)
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    fun getProduct(productId: String): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        return toProductResult(product)
    }

    /**
     * Get products by company
     */
    @Transactional(readOnly = true)
    fun getProductsByCompany(companyId: String): List<ProductResult> {
        return productRepository.findByCompanyId(companyId)
            .map { toProductResult(it) }
    }

    /**
     * Search products by name
     */
    @Transactional(readOnly = true)
    fun searchProducts(companyId: String, keyword: String): List<ProductResult> {
        return productRepository.searchByName(companyId, keyword)
            .map { toProductResult(it) }
    }

    /**
     * Update product
     */
    fun updateProduct(productId: String, command: UpdateProductCommand): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        command.nameKo?.let { product.name = LocalizedString(ko = it, en = command.nameEn ?: product.name.en) }
        command.brand?.let { product.brand = it }
        command.category?.let { product.category = Category(it) }
        command.uom?.let { product.uom = it }
        command.basePrice?.let { product.basePrice = it }
        command.dimensions?.let { product.dimensions = it }
        command.weight?.let { product.weight = it }
        command.logisticsInfo?.let { product.logisticsInfo = it }

        // Additional attributes
        if (command.color != null) product.color = command.color
        if (command.ownerName != null) product.ownerName = command.ownerName
        if (command.customerGoodsNo != null) product.customerGoodsNo = command.customerGoodsNo

        // Customs info
        if (command.hsCode != null) product.hsCode = command.hsCode
        if (command.countryOfOrigin != null) product.countryOfOrigin = command.countryOfOrigin
        if (command.material != null) product.material = command.material
        if (command.manufacturer != null) product.manufacturer = command.manufacturer
        if (command.manufacturerAddress != null) product.manufacturerAddress = command.manufacturerAddress

        val updatedProduct = productRepository.save(product)
        return toProductResult(updatedProduct)
    }

    /**
     * Add barcode to product
     */
    fun addBarcode(productId: String, barcode: String, isMain: Boolean = false): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        // Check for duplicate barcode
        if (productRepository.existsByCompanyIdAndBarcode(product.companyId, barcode)) {
            throw BusinessRuleException(
                "Barcode already exists: $barcode",
                ErrorCode.DUPLICATE_BARCODE
            )
        }

        product.addBarcode(barcode, isMain)
        val updatedProduct = productRepository.save(product)

        return toProductResult(updatedProduct)
    }

    /**
     * Set main barcode
     */
    fun setMainBarcode(productId: String, barcode: String): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        product.setMainBarcode(barcode)
        val updatedProduct = productRepository.save(product)

        return toProductResult(updatedProduct)
    }

    /**
     * Add customs strategy
     */
    fun addCustomsStrategy(productId: String, command: AddCustomsStrategyCommand): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        val strategy = CustomsStrategy.create(
            countryCode = command.countryCode,
            localHsCode = command.localHsCode,
            invoiceName = command.invoiceName,
            dutyRate = command.dutyRate,
            requiredDocs = command.requiredDocs,
            complianceAlert = command.complianceAlert
        )

        product.addCustomsStrategy(strategy)
        val updatedProduct = productRepository.save(product)

        return toProductResult(updatedProduct)
    }

    /**
     * Activate product
     */
    fun activateProduct(productId: String): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        product.activate()
        val updatedProduct = productRepository.save(product)

        return toProductResult(updatedProduct)
    }

    /**
     * Deactivate product
     */
    fun deactivateProduct(productId: String): ProductResult {
        val product = productRepository.findById(productId)
            ?: throw EntityNotFoundException("Product", productId)

        product.deactivate()
        val updatedProduct = productRepository.save(product)

        return toProductResult(updatedProduct)
    }

    private fun toProductResult(product: Product): ProductResult {
        return ProductResult(
            id = product.id,
            companyId = product.companyId,
            sku = product.sku,
            nameKo = product.name.ko,
            nameEn = product.name.en,
            brand = product.brand,
            category = product.category?.path,
            uom = product.uom,
            basePrice = product.basePrice,
            status = product.status,
            barcodes = product.barcodes.map { BarcodeResult(it.code, it.isMain) },
            mainBarcode = product.getMainBarcode()?.code
        )
    }
}

/**
 * Command to create a product
 */
data class CreateProductCommand(
    val companyId: String,
    val sku: String,
    val nameKo: String,
    val nameEn: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val uom: UnitOfMeasure = UnitOfMeasure.PCS,
    val basePrice: Money? = null,
    val dimensions: Dimensions? = null,
    val weight: Weight? = null,
    val logisticsInfo: LogisticsInfo? = null,
    val color: String? = null,
    val ownerName: String? = null,
    val customerGoodsNo: String? = null,
    val hsCode: String? = null,
    val countryOfOrigin: String? = null,
    val material: String? = null,
    val manufacturer: String? = null,
    val manufacturerAddress: String? = null
)

/**
 * Command to update a product
 */
data class UpdateProductCommand(
    val nameKo: String? = null,
    val nameEn: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val uom: UnitOfMeasure? = null,
    val basePrice: Money? = null,
    val dimensions: Dimensions? = null,
    val weight: Weight? = null,
    val logisticsInfo: LogisticsInfo? = null,
    val color: String? = null,
    val ownerName: String? = null,
    val customerGoodsNo: String? = null,
    val hsCode: String? = null,
    val countryOfOrigin: String? = null,
    val material: String? = null,
    val manufacturer: String? = null,
    val manufacturerAddress: String? = null
)

/**
 * Command to add customs strategy
 */
data class AddCustomsStrategyCommand(
    val countryCode: String,
    val localHsCode: String,
    val invoiceName: String,
    val dutyRate: String? = null,
    val requiredDocs: List<String> = emptyList(),
    val complianceAlert: String? = null
)

/**
 * Result DTO for Product
 */
data class ProductResult(
    val id: String,
    val companyId: String,
    val sku: String,
    val nameKo: String,
    val nameEn: String?,
    val brand: String?,
    val category: String?,
    val uom: UnitOfMeasure,
    val basePrice: Money?,
    val status: ProductStatus,
    val barcodes: List<BarcodeResult>,
    val mainBarcode: String?
)

/**
 * Result DTO for Barcode
 */
data class BarcodeResult(
    val code: String,
    val isMain: Boolean
)
