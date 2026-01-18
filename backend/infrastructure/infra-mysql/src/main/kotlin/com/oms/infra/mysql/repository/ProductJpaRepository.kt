package com.oms.infra.mysql.repository

import com.oms.catalog.domain.Product
import com.oms.catalog.domain.ProductStatus
import com.oms.catalog.repository.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for Product
 */
interface ProductJpaRepositoryInterface : JpaRepository<Product, String> {

    fun findByCompanyId(companyId: String): List<Product>

    fun findByCompanyIdAndSku(companyId: String, sku: String): Product?

    fun findByCompanyIdAndStatus(companyId: String, status: ProductStatus): List<Product>

    @Query("""
        SELECT p FROM Product p
        JOIN p._barcodes b
        WHERE p.companyId = :companyId AND b.code = :barcode
    """)
    fun findByCompanyIdAndBarcode(@Param("companyId") companyId: String, @Param("barcode") barcode: String): Product?

    fun existsByCompanyIdAndSku(companyId: String, sku: String): Boolean

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Product p
        JOIN p._barcodes b
        WHERE p.companyId = :companyId AND b.code = :barcode
    """)
    fun existsByCompanyIdAndBarcode(@Param("companyId") companyId: String, @Param("barcode") barcode: String): Boolean

    @Query("SELECT p FROM Product p WHERE p.companyId = :companyId AND (p.name.ko LIKE %:keyword% OR p.name.en LIKE %:keyword%)")
    fun searchByName(@Param("companyId") companyId: String, @Param("keyword") keyword: String): List<Product>

    fun countByCompanyId(companyId: String): Long

    @Query("SELECT p FROM Product p WHERE p.companyId = :companyId ORDER BY p.createdAt DESC")
    fun findByCompanyIdWithPaging(@Param("companyId") companyId: String, pageable: org.springframework.data.domain.Pageable): List<Product>
}

/**
 * Implementation of ProductRepository using Spring Data JPA
 */
@Repository
class ProductJpaRepository(
    private val jpaRepository: ProductJpaRepositoryInterface
) : ProductRepository {

    override fun save(product: Product): Product {
        return jpaRepository.save(product)
    }

    override fun findById(id: String): Product? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByCompanyId(companyId: String): List<Product> {
        return jpaRepository.findByCompanyId(companyId)
    }

    override fun findByCompanyIdAndSku(companyId: String, sku: String): Product? {
        return jpaRepository.findByCompanyIdAndSku(companyId, sku)
    }

    override fun findByCompanyIdAndStatus(companyId: String, status: ProductStatus): List<Product> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status)
    }

    override fun findByCompanyIdAndBarcode(companyId: String, barcode: String): Product? {
        return jpaRepository.findByCompanyIdAndBarcode(companyId, barcode)
    }

    override fun existsByCompanyIdAndSku(companyId: String, sku: String): Boolean {
        return jpaRepository.existsByCompanyIdAndSku(companyId, sku)
    }

    override fun existsByCompanyIdAndBarcode(companyId: String, barcode: String): Boolean {
        return jpaRepository.existsByCompanyIdAndBarcode(companyId, barcode)
    }

    override fun searchByName(companyId: String, keyword: String): List<Product> {
        return jpaRepository.searchByName(companyId, keyword)
    }

    override fun findByCompanyIdWithPaging(companyId: String, offset: Int, limit: Int): List<Product> {
        val pageable = PageRequest.of(offset / limit, limit)
        return jpaRepository.findByCompanyIdWithPaging(companyId, pageable)
    }

    override fun countByCompanyId(companyId: String): Long {
        return jpaRepository.countByCompanyId(companyId)
    }

    override fun delete(product: Product) {
        jpaRepository.delete(product)
    }
}
