package com.oms.application.identity

import com.oms.core.exception.BusinessRuleException
import com.oms.core.exception.EntityNotFoundException
import com.oms.core.exception.ErrorCode
import com.oms.identity.domain.Company
import com.oms.identity.domain.CompanyStatus
import com.oms.identity.domain.User
import com.oms.identity.domain.UserRole
import com.oms.identity.repository.CompanyRepository
import com.oms.identity.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for Company operations
 */
@Service
@Transactional
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val userRepository: UserRepository
) {

    /**
     * Create a new company with an owner
     */
    fun createCompany(command: CreateCompanyCommand): CompanyResult {
        // Check if business number already exists
        if (companyRepository.existsByBusinessNumber(command.businessNumber)) {
            throw BusinessRuleException(
                "Business number already exists: ${command.businessNumber}",
                ErrorCode.BUSINESS_RULE_VIOLATION
            )
        }

        // Create company
        val company = Company.create(
            name = command.name,
            businessNumber = command.businessNumber
        )
        val savedCompany = companyRepository.save(company)

        // Create owner user
        val owner = User.createOwner(
            companyId = savedCompany.id,
            email = command.ownerEmail,
            name = command.ownerName
        )
        userRepository.save(owner)

        return CompanyResult(
            id = savedCompany.id,
            name = savedCompany.name,
            businessNumber = savedCompany.businessNumber,
            status = savedCompany.status
        )
    }

    /**
     * Get company by ID
     */
    @Transactional(readOnly = true)
    fun getCompany(companyId: String): CompanyResult {
        val company = companyRepository.findById(companyId)
            ?: throw EntityNotFoundException("Company", companyId)

        return CompanyResult(
            id = company.id,
            name = company.name,
            businessNumber = company.businessNumber,
            status = company.status
        )
    }

    /**
     * Update company name
     */
    fun updateCompanyName(companyId: String, newName: String): CompanyResult {
        val company = companyRepository.findById(companyId)
            ?: throw EntityNotFoundException("Company", companyId)

        company.updateName(newName)
        val updatedCompany = companyRepository.save(company)

        return CompanyResult(
            id = updatedCompany.id,
            name = updatedCompany.name,
            businessNumber = updatedCompany.businessNumber,
            status = updatedCompany.status
        )
    }

    /**
     * Suspend a company
     */
    fun suspendCompany(companyId: String): CompanyResult {
        val company = companyRepository.findById(companyId)
            ?: throw EntityNotFoundException("Company", companyId)

        company.suspend()
        val updatedCompany = companyRepository.save(company)

        return CompanyResult(
            id = updatedCompany.id,
            name = updatedCompany.name,
            businessNumber = updatedCompany.businessNumber,
            status = updatedCompany.status
        )
    }

    /**
     * Reactivate a company
     */
    fun reactivateCompany(companyId: String): CompanyResult {
        val company = companyRepository.findById(companyId)
            ?: throw EntityNotFoundException("Company", companyId)

        company.reactivate()
        val updatedCompany = companyRepository.save(company)

        return CompanyResult(
            id = updatedCompany.id,
            name = updatedCompany.name,
            businessNumber = updatedCompany.businessNumber,
            status = updatedCompany.status
        )
    }

    /**
     * List all active companies
     */
    @Transactional(readOnly = true)
    fun listActiveCompanies(): List<CompanyResult> {
        return companyRepository.findAllByStatus(CompanyStatus.ACTIVE)
            .map { company ->
                CompanyResult(
                    id = company.id,
                    name = company.name,
                    businessNumber = company.businessNumber,
                    status = company.status
                )
            }
    }
}

/**
 * Command to create a company
 */
data class CreateCompanyCommand(
    val name: String,
    val businessNumber: String,
    val ownerEmail: String,
    val ownerName: String
)

/**
 * Result DTO for Company
 */
data class CompanyResult(
    val id: String,
    val name: String,
    val businessNumber: String,
    val status: CompanyStatus
)
