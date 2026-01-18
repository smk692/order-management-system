package com.oms.application.strategy

import com.oms.strategy.domain.GlobalReadiness
import com.oms.strategy.domain.vo.ReadinessCategory
import com.oms.strategy.domain.vo.ReadinessItem
import com.oms.strategy.domain.vo.ReadinessStatus
import com.oms.strategy.repository.GlobalReadinessRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class GlobalReadinessService(
    private val readinessRepository: GlobalReadinessRepository
) {

    fun createReadiness(
        companyId: String,
        country: String,
        initialChecklist: List<ReadinessItem> = emptyList()
    ): GlobalReadiness {
        // Check if readiness already exists for this country
        val existing = readinessRepository.findByCompanyIdAndCountry(companyId, country)
        require(existing == null) {
            "Readiness already exists for country $country in company $companyId"
        }

        val readiness = GlobalReadiness(
            country = country,
            initialChecklist = initialChecklist
        ).apply {
            assignToCompany(companyId)
        }

        return readinessRepository.save(readiness)
    }

    @Transactional(readOnly = true)
    fun getReadiness(readinessId: UUID): GlobalReadiness {
        return readinessRepository.findByIdOrThrow(readinessId)
    }

    @Transactional(readOnly = true)
    fun getReadinessByCountry(companyId: String, country: String): GlobalReadiness? {
        return readinessRepository.findByCompanyIdAndCountry(companyId, country)
    }

    @Transactional(readOnly = true)
    fun getReadinessByCompany(companyId: String): List<GlobalReadiness> {
        return readinessRepository.findByCompanyId(companyId)
    }

    @Transactional(readOnly = true)
    fun getReadinessByStatus(companyId: String, status: ReadinessStatus): List<GlobalReadiness> {
        return readinessRepository.findByCompanyIdAndStatus(companyId, status)
    }

    fun updateChecklistItem(
        readinessId: UUID,
        itemId: String,
        category: ReadinessCategory,
        description: String,
        completed: Boolean
    ): GlobalReadiness {
        val readiness = readinessRepository.findByIdOrThrow(readinessId)

        val item = ReadinessItem(
            id = itemId,
            category = category,
            description = description,
            completed = completed
        )

        readiness.updateChecklist(item)
        return readinessRepository.save(readiness)
    }

    fun addChecklistItem(
        readinessId: UUID,
        itemId: String,
        category: ReadinessCategory,
        description: String
    ): GlobalReadiness {
        val readiness = readinessRepository.findByIdOrThrow(readinessId)

        val item = ReadinessItem(
            id = itemId,
            category = category,
            description = description,
            completed = false
        )

        readiness.addChecklistItem(item)
        return readinessRepository.save(readiness)
    }

    fun removeChecklistItem(readinessId: UUID, itemId: String): GlobalReadiness {
        val readiness = readinessRepository.findByIdOrThrow(readinessId)
        readiness.removeChecklistItem(itemId)
        return readinessRepository.save(readiness)
    }

    fun launchCountry(readinessId: UUID): GlobalReadiness {
        val readiness = readinessRepository.findByIdOrThrow(readinessId)
        readiness.launch()
        return readinessRepository.save(readiness)
    }
}
