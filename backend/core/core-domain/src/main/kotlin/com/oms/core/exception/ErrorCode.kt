package com.oms.core.exception

/**
 * Centralized error codes for the application
 */
enum class ErrorCode(
    val code: String,
    val message: String
) {
    // Common errors (1xxx)
    ENTITY_NOT_FOUND("1001", "Entity not found"),
    VALIDATION_ERROR("1002", "Validation error"),
    BUSINESS_RULE_VIOLATION("1003", "Business rule violation"),
    INVALID_STATE_TRANSITION("1004", "Invalid state transition"),

    // Authentication & Authorization (2xxx)
    UNAUTHORIZED("2001", "Unauthorized access"),
    FORBIDDEN("2002", "Access forbidden"),
    INVALID_TOKEN("2003", "Invalid token"),
    TOKEN_EXPIRED("2004", "Token expired"),

    // Identity errors (3xxx)
    COMPANY_NOT_FOUND("3001", "Company not found"),
    USER_NOT_FOUND("3002", "User not found"),
    DUPLICATE_EMAIL("3003", "Email already exists"),
    INVALID_CREDENTIALS("3004", "Invalid credentials"),

    // Catalog errors (4xxx)
    PRODUCT_NOT_FOUND("4001", "Product not found"),
    DUPLICATE_SKU("4002", "SKU already exists"),
    DUPLICATE_BARCODE("4003", "Barcode already exists"),
    INVALID_PRODUCT_STATUS("4004", "Invalid product status"),

    // Channel errors (5xxx)
    CHANNEL_NOT_FOUND("5001", "Channel not found"),
    WAREHOUSE_NOT_FOUND("5002", "Warehouse not found"),
    CHANNEL_SYNC_FAILED("5003", "Channel synchronization failed"),
    INVALID_CHANNEL_STATUS("5004", "Invalid channel status"),

    // Order errors (6xxx)
    ORDER_NOT_FOUND("6001", "Order not found"),
    INVALID_ORDER_STATUS("6002", "Invalid order status"),
    ORDER_ALREADY_CANCELLED("6003", "Order already cancelled"),
    ORDER_CANNOT_BE_MODIFIED("6004", "Order cannot be modified"),

    // Inventory errors (7xxx)
    STOCK_NOT_FOUND("7001", "Stock not found"),
    INSUFFICIENT_STOCK("7002", "Insufficient stock"),
    STOCK_ALREADY_RESERVED("7003", "Stock already reserved"),
    INVALID_STOCK_QUANTITY("7004", "Invalid stock quantity"),

    // Claim errors (8xxx)
    CLAIM_NOT_FOUND("8001", "Claim not found"),
    INVALID_CLAIM_TYPE("8002", "Invalid claim type"),
    CLAIM_ALREADY_PROCESSED("8003", "Claim already processed"),

    // Settlement errors (9xxx)
    SETTLEMENT_NOT_FOUND("9001", "Settlement not found"),
    SETTLEMENT_ALREADY_CONFIRMED("9002", "Settlement already confirmed"),

    // External integration errors (10xxx)
    EXTERNAL_API_ERROR("10001", "External API error"),
    WEBHOOK_PROCESSING_FAILED("10002", "Webhook processing failed"),
    RATE_LIMIT_EXCEEDED("10003", "Rate limit exceeded");

    override fun toString(): String = "[$code] $message"
}
