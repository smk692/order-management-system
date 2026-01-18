package com.oms.core.exception

/**
 * Base exception for all domain-related errors
 */
open class DomainException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception for entity not found errors
 */
class EntityNotFoundException(
    entityType: String,
    id: String
) : DomainException(
    errorCode = ErrorCode.ENTITY_NOT_FOUND,
    message = "$entityType not found with id: $id"
)

/**
 * Exception for business rule violations
 */
class BusinessRuleException(
    message: String,
    errorCode: ErrorCode = ErrorCode.BUSINESS_RULE_VIOLATION
) : DomainException(errorCode = errorCode, message = message)

/**
 * Exception for invalid state transitions
 */
class InvalidStateTransitionException(
    entityType: String,
    currentState: String,
    targetState: String
) : DomainException(
    errorCode = ErrorCode.INVALID_STATE_TRANSITION,
    message = "Cannot transition $entityType from $currentState to $targetState"
)

/**
 * Exception for authorization errors
 */
class UnauthorizedException(
    message: String = "Unauthorized access"
) : DomainException(errorCode = ErrorCode.UNAUTHORIZED, message = message)

/**
 * Exception for insufficient stock
 */
class InsufficientStockException(
    productId: String,
    requested: Int,
    available: Int
) : DomainException(
    errorCode = ErrorCode.INSUFFICIENT_STOCK,
    message = "Insufficient stock for product $productId. Requested: $requested, Available: $available"
)
