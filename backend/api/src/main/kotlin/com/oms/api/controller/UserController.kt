package com.oms.api.controller

import com.oms.application.identity.*
import com.oms.identity.domain.UserRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST API for User operations
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management APIs")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/invite")
    @Operation(summary = "Invite a new user to a company")
    fun inviteUser(@Valid @RequestBody request: InviteUserRequest): ResponseEntity<UserResponse> {
        val result = userService.inviteUser(
            InviteUserCommand(
                companyId = request.companyId,
                email = request.email,
                name = request.name,
                role = UserRole.valueOf(request.role)
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    fun getUser(@PathVariable userId: String): ResponseEntity<UserResponse> {
        val result = userService.getUser(userId)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get users by company ID")
    fun getUsersByCompany(@PathVariable companyId: String): ResponseEntity<List<UserResponse>> {
        val results = userService.getUsersByCompany(companyId)
        return ResponseEntity.ok(results.map { it.toResponse() })
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "Activate a user")
    fun activateUser(@PathVariable userId: String): ResponseEntity<UserResponse> {
        val result = userService.activateUser(userId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate a user")
    fun deactivateUser(@PathVariable userId: String): ResponseEntity<UserResponse> {
        val result = userService.deactivateUser(userId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Change user role")
    fun changeUserRole(
        @PathVariable userId: String,
        @Valid @RequestBody request: ChangeUserRoleRequest
    ): ResponseEntity<UserResponse> {
        val result = userService.changeUserRole(userId, UserRole.valueOf(request.role))
        return ResponseEntity.ok(result.toResponse())
    }

    @PatchMapping("/{userId}/profile")
    @Operation(summary = "Update user profile")
    fun updateUserProfile(
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<UserResponse> {
        val result = userService.updateUserProfile(
            userId,
            UpdateUserCommand(name = request.name, email = request.email)
        )
        return ResponseEntity.ok(result.toResponse())
    }

    private fun UserResult.toResponse() = UserResponse(
        id = id,
        companyId = companyId,
        email = email,
        name = name,
        role = role.name,
        status = status.name
    )
}

// Request/Response DTOs
data class InviteUserRequest(
    @field:NotBlank(message = "Company ID is required")
    val companyId: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Name is required")
    val name: String,

    val role: String = "VIEWER"
)

data class ChangeUserRoleRequest(
    @field:NotBlank(message = "Role is required")
    val role: String
)

data class UpdateUserProfileRequest(
    val name: String? = null,
    @field:Email(message = "Invalid email format")
    val email: String? = null
)

data class UserResponse(
    val id: String,
    val companyId: String,
    val email: String,
    val name: String,
    val role: String,
    val status: String
)
