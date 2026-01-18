package com.oms.api.controller

import com.oms.application.catalog.*
import com.oms.catalog.domain.UnitOfMeasure
import com.oms.core.domain.Currency
import com.oms.core.domain.Money
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * REST API for Product operations
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management APIs")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    @Operation(summary = "Create a new product")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ResponseEntity<ProductResponse> {
        val command = CreateProductCommand(
            companyId = request.companyId,
            sku = request.sku,
            nameKo = request.nameKo,
            nameEn = request.nameEn,
            brand = request.brand,
            category = request.category,
            uom = request.uom?.let { UnitOfMeasure.valueOf(it) } ?: UnitOfMeasure.PCS,
            basePrice = request.basePrice?.let {
                Money(
                    amount = BigDecimal(it.amount),
                    currency = Currency.valueOf(it.currency)
                )
            }
        )
        val result = productService.createProduct(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    fun getProduct(@PathVariable productId: String): ResponseEntity<ProductResponse> {
        val result = productService.getProduct(productId)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get products by company ID")
    fun getProductsByCompany(@PathVariable companyId: String): ResponseEntity<List<ProductResponse>> {
        val results = productService.getProductsByCompany(companyId)
        return ResponseEntity.ok(results.map { it.toResponse() })
    }

    @GetMapping("/company/{companyId}/search")
    @Operation(summary = "Search products by name")
    fun searchProducts(
        @PathVariable companyId: String,
        @RequestParam keyword: String
    ): ResponseEntity<List<ProductResponse>> {
        val results = productService.searchProducts(companyId, keyword)
        return ResponseEntity.ok(results.map { it.toResponse() })
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "Update product")
    fun updateProduct(
        @PathVariable productId: String,
        @Valid @RequestBody request: UpdateProductRequest
    ): ResponseEntity<ProductResponse> {
        val command = UpdateProductCommand(
            nameKo = request.nameKo,
            nameEn = request.nameEn,
            brand = request.brand,
            category = request.category,
            uom = request.uom?.let { UnitOfMeasure.valueOf(it) },
            basePrice = request.basePrice?.let {
                Money(
                    amount = BigDecimal(it.amount),
                    currency = Currency.valueOf(it.currency)
                )
            }
        )
        val result = productService.updateProduct(productId, command)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{productId}/barcodes")
    @Operation(summary = "Add barcode to product")
    fun addBarcode(
        @PathVariable productId: String,
        @Valid @RequestBody request: AddBarcodeRequest
    ): ResponseEntity<ProductResponse> {
        val result = productService.addBarcode(productId, request.barcode, request.isMain)
        return ResponseEntity.ok(result.toResponse())
    }

    @PatchMapping("/{productId}/barcodes/{barcode}/main")
    @Operation(summary = "Set main barcode")
    fun setMainBarcode(
        @PathVariable productId: String,
        @PathVariable barcode: String
    ): ResponseEntity<ProductResponse> {
        val result = productService.setMainBarcode(productId, barcode)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{productId}/activate")
    @Operation(summary = "Activate product")
    fun activateProduct(@PathVariable productId: String): ResponseEntity<ProductResponse> {
        val result = productService.activateProduct(productId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{productId}/deactivate")
    @Operation(summary = "Deactivate product")
    fun deactivateProduct(@PathVariable productId: String): ResponseEntity<ProductResponse> {
        val result = productService.deactivateProduct(productId)
        return ResponseEntity.ok(result.toResponse())
    }

    private fun ProductResult.toResponse() = ProductResponse(
        id = id,
        companyId = companyId,
        sku = sku,
        nameKo = nameKo,
        nameEn = nameEn,
        brand = brand,
        category = category,
        uom = uom.name,
        basePrice = basePrice?.let { MoneyDto(it.amount.toString(), it.currency.name) },
        status = status.name,
        barcodes = barcodes.map { BarcodeDto(it.code, it.isMain) },
        mainBarcode = mainBarcode
    )
}

// Request/Response DTOs
data class CreateProductRequest(
    @field:NotBlank(message = "Company ID is required")
    val companyId: String,

    @field:NotBlank(message = "SKU is required")
    val sku: String,

    @field:NotBlank(message = "Korean name is required")
    val nameKo: String,

    val nameEn: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val uom: String? = null,
    val basePrice: MoneyDto? = null
)

data class UpdateProductRequest(
    val nameKo: String? = null,
    val nameEn: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val uom: String? = null,
    val basePrice: MoneyDto? = null
)

data class AddBarcodeRequest(
    @field:NotBlank(message = "Barcode is required")
    val barcode: String,
    val isMain: Boolean = false
)

data class MoneyDto(
    val amount: String,
    val currency: String = "KRW"
)

data class BarcodeDto(
    val code: String,
    val isMain: Boolean
)

data class ProductResponse(
    val id: String,
    val companyId: String,
    val sku: String,
    val nameKo: String,
    val nameEn: String?,
    val brand: String?,
    val category: String?,
    val uom: String,
    val basePrice: MoneyDto?,
    val status: String,
    val barcodes: List<BarcodeDto>,
    val mainBarcode: String?
)
