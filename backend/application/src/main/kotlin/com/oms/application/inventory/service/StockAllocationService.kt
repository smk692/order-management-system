package com.oms.application.inventory.service

import com.oms.application.inventory.dto.AllocateToChannelCommand
import com.oms.application.inventory.dto.DeallocateFromChannelCommand
import com.oms.application.inventory.dto.StockResult
import com.oms.inventory.repository.StockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for Stock channel allocation
 */
@Service
@Transactional
class StockAllocationService(
    private val stockRepository: StockRepository
) {

    /**
     * Allocate stock to a specific channel
     */
    fun allocateToChannel(command: AllocateToChannelCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        stock.allocateToChannel(command.channelId, command.quantity)
        val savedStock = stockRepository.save(stock)

        return toStockResult(savedStock)
    }

    /**
     * Deallocate stock from a specific channel
     */
    fun deallocateFromChannel(command: DeallocateFromChannelCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        stock.deallocateFromChannel(command.channelId, command.quantity)
        val savedStock = stockRepository.save(stock)

        return toStockResult(savedStock)
    }

    private fun toStockResult(stock: com.oms.inventory.domain.Stock): StockResult {
        return StockResult(
            id = stock.id,
            companyId = stock.companyId,
            productId = stock.productId,
            warehouseId = stock.warehouseId,
            total = stock.total,
            available = stock.available,
            reserved = stock.reserved,
            safetyStock = stock.safetyStock,
            status = stock.status,
            channelAllocations = stock.channelAllocations.toMap()
        )
    }
}
