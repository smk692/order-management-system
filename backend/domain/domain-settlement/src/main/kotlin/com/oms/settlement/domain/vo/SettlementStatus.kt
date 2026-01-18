package com.oms.settlement.domain.vo

enum class SettlementStatus {
    DRAFT,      // 정산 초안 (생성 후 계산 전)
    CONFIRMED,  // 정산 확정 (계산 완료, 지급 대기)
    PAID        // 지급 완료
}
