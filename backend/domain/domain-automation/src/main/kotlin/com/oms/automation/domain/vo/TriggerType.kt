package com.oms.automation.domain.vo

enum class TriggerType {
    ORDER_CREATED,
    ORDER_STATUS_CHANGED,
    STOCK_LOW,
    STOCK_CHANGED,
    PAYMENT_FAILED,
    SHIPPING_STARTED
}
