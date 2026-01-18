package com.oms.api.controller

import com.oms.application.order.*
import com.oms.core.domain.Address
import com.oms.core.domain.Currency
import com.oms.core.domain.Money
import com.oms.order.domain.Carrier
import com.oms.order.domain.FulfillmentMethod
import com.oms.order.domain.OrderStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant

/**
 * REST API for Order operations
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    @Operation(summary = "Create a new order")
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val command = CreateOrderCommand(
            companyId = request.companyId,
            channelId = request.channelId,
            customerName = request.customer.name,
            customerPhone = request.customer.phone,
            customerEmail = request.customer.email,
            shippingAddress = Address(
                recipient = request.shippingAddress.recipient,
                phone = request.shippingAddress.phone,
                zipCode = request.shippingAddress.zipCode,
                address1 = request.shippingAddress.address1,
                address2 = request.shippingAddress.address2,
                city = request.shippingAddress.city,
                state = request.shippingAddress.state,
                country = request.shippingAddress.country
            ),
            fulfillmentMethod = request.fulfillmentMethod?.let { FulfillmentMethod.valueOf(it) }
                ?: FulfillmentMethod.WMS,
            externalOrderId = request.externalOrderId,
            items = request.items.map { item ->
                CreateOrderItemCommand(
                    productId = item.productId,
                    productName = item.productName,
                    sku = item.sku,
                    quantity = item.quantity,
                    unitPrice = Money(
                        amount = BigDecimal(item.unitPrice.amount),
                        currency = Currency.valueOf(item.unitPrice.currency)
                    )
                )
            }
        )
        val result = orderService.createOrder(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    fun getOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val result = orderService.getOrder(orderId)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get orders by company ID")
    fun getOrdersByCompany(@PathVariable companyId: String): ResponseEntity<List<OrderResponse>> {
        val results = orderService.getOrdersByCompany(companyId)
        return ResponseEntity.ok(results.map { it.toResponse() })
    }

    @GetMapping("/company/{companyId}/status/{status}")
    @Operation(summary = "Get orders by status")
    fun getOrdersByStatus(
        @PathVariable companyId: String,
        @PathVariable status: String
    ): ResponseEntity<List<OrderResponse>> {
        val results = orderService.getOrdersByStatus(companyId, OrderStatus.valueOf(status))
        return ResponseEntity.ok(results.map { it.toResponse() })
    }

    @PostMapping("/{orderId}/pay")
    @Operation(summary = "Mark order as paid")
    fun markOrderPaid(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val result = orderService.markOrderPaid(orderId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{orderId}/prepare")
    @Operation(summary = "Start preparing order")
    fun startPreparingOrder(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val result = orderService.startPreparingOrder(orderId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{orderId}/ready-to-ship")
    @Operation(summary = "Mark order ready to ship")
    fun markOrderReadyToShip(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val result = orderService.markOrderReadyToShip(orderId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{orderId}/ship")
    @Operation(summary = "Ship order")
    fun shipOrder(
        @PathVariable orderId: String,
        @Valid @RequestBody request: ShipOrderRequest
    ): ResponseEntity<OrderResponse> {
        val result = orderService.shipOrder(
            orderId,
            ShipOrderCommand(
                carrier = Carrier.valueOf(request.carrier),
                trackingNumber = request.trackingNumber
            )
        )
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Mark order as delivered")
    fun markOrderDelivered(@PathVariable orderId: String): ResponseEntity<OrderResponse> {
        val result = orderService.markOrderDelivered(orderId)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    fun cancelOrder(
        @PathVariable orderId: String,
        @Valid @RequestBody request: CancelOrderRequest
    ): ResponseEntity<OrderResponse> {
        val result = orderService.cancelOrder(orderId, request.reason)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{orderId}/assign-warehouse")
    @Operation(summary = "Assign order to warehouse")
    fun assignToWarehouse(
        @PathVariable orderId: String,
        @Valid @RequestBody request: AssignWarehouseRequest
    ): ResponseEntity<OrderResponse> {
        val result = orderService.assignToWarehouse(orderId, request.warehouseId, request.routingLogic)
        return ResponseEntity.ok(result.toResponse())
    }

    private fun OrderResult.toResponse() = OrderResponse(
        id = id,
        companyId = companyId,
        channelId = channelId,
        externalOrderId = externalOrderId,
        status = status.name,
        orderDate = orderDate.toString(),
        customer = CustomerDto(
            name = customer.name,
            phone = customer.phone,
            email = customer.email
        ),
        shippingAddress = AddressDto(
            recipient = shippingAddress.recipient,
            phone = shippingAddress.phone,
            zipCode = shippingAddress.zipCode,
            address1 = shippingAddress.address1,
            address2 = shippingAddress.address2,
            city = shippingAddress.city,
            state = shippingAddress.state,
            country = shippingAddress.country
        ),
        fulfillmentMethod = fulfillmentMethod.name,
        assignedWarehouseId = assignedWarehouseId,
        totalAmount = MoneyDto(totalAmount.amount.toString(), totalAmount.currency.name),
        items = items.map { item ->
            OrderItemDto(
                id = item.id,
                productId = item.productId,
                productName = item.productName,
                sku = item.sku,
                quantity = item.quantity,
                unitPrice = MoneyDto(item.unitPrice.amount.toString(), item.unitPrice.currency.name),
                totalPrice = MoneyDto(item.totalPrice.amount.toString(), item.totalPrice.currency.name)
            )
        },
        shipping = shipping?.let { s ->
            ShippingDto(
                carrier = s.carrier.name,
                trackingNumber = s.trackingNumber,
                status = s.status.name,
                shippedAt = s.shippedAt?.toString(),
                deliveredAt = s.deliveredAt?.toString()
            )
        }
    )
}

// Request/Response DTOs
data class CreateOrderRequest(
    @field:NotBlank(message = "Company ID is required")
    val companyId: String,

    @field:NotBlank(message = "Channel ID is required")
    val channelId: String,

    @field:Valid
    val customer: CustomerDto,

    @field:Valid
    val shippingAddress: AddressDto,

    val fulfillmentMethod: String? = null,
    val externalOrderId: String? = null,

    @field:NotEmpty(message = "At least one item is required")
    val items: List<CreateOrderItemDto>
)

data class CustomerDto(
    @field:NotBlank(message = "Customer name is required")
    val name: String,

    @field:NotBlank(message = "Customer phone is required")
    val phone: String,

    val email: String? = null
)

data class AddressDto(
    @field:NotBlank(message = "Recipient is required")
    val recipient: String,

    @field:NotBlank(message = "Phone is required")
    val phone: String,

    val zipCode: String? = null,

    @field:NotBlank(message = "Address line 1 is required")
    val address1: String,

    val address2: String? = null,
    val city: String? = null,
    val state: String? = null,

    @field:NotBlank(message = "Country is required")
    val country: String
)

data class CreateOrderItemDto(
    @field:NotBlank(message = "Product ID is required")
    val productId: String,

    @field:NotBlank(message = "Product name is required")
    val productName: String,

    @field:NotBlank(message = "SKU is required")
    val sku: String,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,

    @field:Valid
    val unitPrice: MoneyDto
)

data class ShipOrderRequest(
    @field:NotBlank(message = "Carrier is required")
    val carrier: String,

    @field:NotBlank(message = "Tracking number is required")
    val trackingNumber: String
)

data class CancelOrderRequest(
    @field:NotBlank(message = "Reason is required")
    val reason: String
)

data class AssignWarehouseRequest(
    @field:NotBlank(message = "Warehouse ID is required")
    val warehouseId: String,

    val routingLogic: String = ""
)

data class OrderItemDto(
    val id: Long,
    val productId: String,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: MoneyDto,
    val totalPrice: MoneyDto
)

data class ShippingDto(
    val carrier: String,
    val trackingNumber: String,
    val status: String,
    val shippedAt: String?,
    val deliveredAt: String?
)

data class OrderResponse(
    val id: String,
    val companyId: String,
    val channelId: String,
    val externalOrderId: String?,
    val status: String,
    val orderDate: String,
    val customer: CustomerDto,
    val shippingAddress: AddressDto,
    val fulfillmentMethod: String,
    val assignedWarehouseId: String?,
    val totalAmount: MoneyDto,
    val items: List<OrderItemDto>,
    val shipping: ShippingDto?
)
