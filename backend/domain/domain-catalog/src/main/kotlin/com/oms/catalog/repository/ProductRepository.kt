package com.oms.catalog.repository

import com.oms.catalog.domain.Product
import com.oms.catalog.domain.ProductStatus

/**
 * Repository interface for Product aggregate
 * Implementation will be in infrastructure module
 */
interface ProductRepository {

    fun save(product: Product): Product

    fun findById(id: String): Product?

    fun findByCompanyId(companyId: String): List<Product>

    fun findByCompanyIdAndSku(companyId: String, sku: String): Product?

    fun findByCompanyIdAndStatus(companyId: String, status: ProductStatus): List<Product>

    fun findByCompanyIdAndBarcode(companyId: String, barcode: String): Product?

    fun existsByCompanyIdAndSku(companyId: String, sku: String): Boolean

    fun existsByCompanyIdAndBarcode(companyId: String, barcode: String): Boolean

    fun searchByName(companyId: String, keyword: String): List<Product>

    fun findByCompanyIdWithPaging(companyId: String, offset: Int, limit: Int): List<Product>

    fun countByCompanyId(companyId: String): Long

    fun delete(product: Product)
}
