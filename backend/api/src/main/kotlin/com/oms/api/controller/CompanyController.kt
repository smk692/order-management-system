package com.oms.api.controller

import com.oms.application.identity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST API for Company operations
 */
@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "Company management APIs")
class CompanyController(
    private val companyService: CompanyService
) {

    @PostMapping
    @Operation(summary = "Create a new company")
    fun createCompany(@Valid @RequestBody request: CreateCompanyRequest): ResponseEntity<CompanyResponse> {
        val result = companyService.createCompany(
            CreateCompanyCommand(
                name = request.name,
                businessNumber = request.businessNumber,
                ownerEmail = request.ownerEmail,
                ownerName = request.ownerName
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by ID")
    fun getCompany(@PathVariable companyId: String): ResponseEntity<CompanyResponse> {
        val result = companyService.getCompany(companyId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PatchMapping("/{companyId}/name")
    @Operation(summary = "Update company name")
    fun updateCompanyName(
        @PathVariable companyId: String,
        @Valid @RequestBody request: UpdateCompanyNameRequest
    ): ResponseEntity<CompanyResponse> {
        val result = companyService.updateCompanyName(companyId, request.name)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{companyId}/suspend")
    @Operation(summary = "Suspend a company")
    fun suspendCompany(@PathVariable companyId: String): ResponseEntity<CompanyResponse> {
        val result = companyService.suspendCompany(companyId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{companyId}/reactivate")
    @Operation(summary = "Reactivate a company")
    fun reactivateCompany(@PathVariable companyId: String): ResponseEntity<CompanyResponse> {
        val result = companyService.reactivateCompany(companyId)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping
    @Operation(summary = "List active companies")
    fun listActiveCompanies(): ResponseEntity<List<CompanyResponse>> {
        val results = companyService.listActiveCompanies()
        return ResponseEntity.ok(results.map { it.toResponse() })
    }

    private fun CompanyResult.toResponse() = CompanyResponse(
        id = id,
        name = name,
        businessNumber = businessNumber,
        status = status.name
    )
}

// Request/Response DTOs
data class CreateCompanyRequest(
    @field:NotBlank(message = "Company name is required")
    val name: String,

    @field:NotBlank(message = "Business number is required")
    val businessNumber: String,

    @field:NotBlank(message = "Owner email is required")
    @field:Email(message = "Invalid email format")
    val ownerEmail: String,

    @field:NotBlank(message = "Owner name is required")
    val ownerName: String
)

data class UpdateCompanyNameRequest(
    @field:NotBlank(message = "Company name is required")
    val name: String
)

data class CompanyResponse(
    val id: String,
    val name: String,
    val businessNumber: String,
    val status: String
)
