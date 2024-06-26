package org.example.globalsourcing.controller

import org.example.globalsourcing.entity.PurchaseOrder
import org.example.globalsourcing.entity.PurchaseOrder.Status
import org.example.globalsourcing.entity.User
import org.example.globalsourcing.service.PurchaseOrderService
import org.example.globalsourcing.service.ServiceException
import org.example.globalsourcing.util.ResponseData
import org.example.globalsourcing.validation.groups.Update
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.util.Assert
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.*

@RestController
@RequestMapping("/purchaseOrder")
@Validated
class PurchaseOrderController(private val purchaseOrderService: PurchaseOrderService) {

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'SALESPERSON')")
    fun create(
        @NotNull(message = "ID不能为空") productId: Long?,
        @NotNull(message = "采购数量不能为空")
        @Positive(message = "采购数量不能小于1") quantity: Int?
    ): ResponseData<PurchaseOrder> {
        val purchaseOrder = purchaseOrderService.create(productId!!, quantity!!)
        return ResponseData.success(purchaseOrder)
    }

    @PostMapping("/createBatch")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'SALESPERSON')")
    fun createBatch(
        @NotEmpty(message = "ID列表不能为空") productIds: Array<Long>,
        @NotEmpty(message = "采购数量列表不能为空") quantities: Array<Int>
    ): ResponseData<List<PurchaseOrder>> {
        Assert.isTrue(productIds.all { it > 0 }, "ID列表中含有非法值！")
        Assert.isTrue(quantities.all { it > 0 }, "采购数量列表中含有非法值！")
        Assert.isTrue(productIds.size == quantities.size, "ID列表与采购数量列表不对应！")

        val purchaseOrders = purchaseOrderService.createBatch(productIds, quantities)
        return ResponseData.success(purchaseOrders)
    }

    @PutMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    fun assign(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotNull(message = "ID不能为空") buyerId: Long?,
        @NotNull(message = "采购数量不能为空")
        @Positive(message = "采购数量不能小于1") quantity: Int?
    ): ResponseData<PurchaseOrder> {
        val purchaseOrder = purchaseOrderService.assign(id!!, buyerId!!, quantity!!)
        return ResponseData.success(purchaseOrder)
    }

    @PutMapping("/uploadPurchaseInfo")
    @PreAuthorize("hasRole('BUYER')")
    fun uploadPurchaseInfo(
        @Validated(Update::class) @RequestBody purchaseOrder: PurchaseOrder,
        authentication: Authentication
    ): ResponseData<PurchaseOrder> {
        purchaseOrder.buyer = authentication.principal as User
        return ResponseData.success(purchaseOrderService.uploadPurchaseInfo(purchaseOrder))
    }

    @PutMapping("/confirmAndAssignToWarehouseKeeper")
    @PreAuthorize("hasRole('ADMIN')")
    @Throws(ServiceException::class)
    fun assignToWarehouseKeeper(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotNull(message = "ID不能为空") warehouseKeeperId: Long?
    ): ResponseData<PurchaseOrder> {
        val purchaseOrder = purchaseOrderService.assignToWarehouseKeeper(id!!, warehouseKeeperId!!)
        return ResponseData.success(purchaseOrder)
    }

    @PutMapping("/reject")
    @PreAuthorize("hasRole('ADMIN')")
    fun reject(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotBlank(message = "驳回理由不能为空")
        @Size(max = 255, message = "驳回理由不能超过{max}个字符") rejectReason: String?
    ): ResponseData<PurchaseOrder> {
        val purchaseOrder = purchaseOrderService.reject(id!!, rejectReason!!)
        return ResponseData.success(purchaseOrder)
    }

    @PutMapping("/putIntoWarehouse")
    @PreAuthorize("hasRole('WAREHOUSE_KEEPER')")
    fun putIntoWarehouse(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotNull(message = "入库数量不能为空")
        @Positive(message = "入库数量不能小于1") quantity: Int?,
        authentication: Authentication
    ): ResponseData<PurchaseOrder> {
        val warehouseKeeper = authentication.principal as User
        val purchaseOrder = purchaseOrderService.putIntoWarehouse(warehouseKeeper, id!!, quantity!!)
        return ResponseData.success(purchaseOrder)
    }

    @PutMapping("/forceFinishWarehousing")
    @PreAuthorize("hasRole('WAREHOUSE_KEEPER')")
    fun forceFinishWarehousing(
        @NotNull(message = "ID不能为空") id: Long?,
        authentication: Authentication
    ): ResponseData<PurchaseOrder> {
        val warehouseKeeper = authentication.principal as User
        val purchaseOrder = purchaseOrderService.forceFinishWarehousing(warehouseKeeper, id!!)
        return ResponseData.success(purchaseOrder)
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@NotNull(message = "ID不能为空") id: Long?): ResponseData<Nothing> {
        purchaseOrderService.delete(id!!)
        return ResponseData.success()
    }

    @GetMapping("/findById")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'WAREHOUSE_KEEPER', 'SALESPERSON')")
    fun findById(@NotNull(message = "ID不能为空") id: Long?): ResponseData<PurchaseOrder> {
        val purchaseOrder = purchaseOrderService.findById(id!!)
        return ResponseData.success(purchaseOrder)
    }

    @GetMapping("/findAll")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'WAREHOUSE_KEEPER', 'SALESPERSON')")
    fun findAll(
        @RequestParam(required = false) buyerId: Long?,
        @RequestParam(required = false) warehouseKeeperId: Long?,
        @RequestParam(required = false) status: Status?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<PurchaseOrder>> {
        val purchaseOrders = purchaseOrderService.findAll(buyerId, warehouseKeeperId, status, page, size)
        return ResponseData.success(purchaseOrders)
    }

    @GetMapping("/getPhoto")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'WAREHOUSE_KEEPER', 'SALESPERSON')")
    fun getPhoto(@NotNull(message = "ID不能为空") id: Long?): ResponseData<String> {
        val photo = purchaseOrderService.getPhoto(id!!)
        return ResponseData.success(photo)
    }

    @GetMapping("/getInvoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'WAREHOUSE_KEEPER', 'SALESPERSON')")
    fun getInvoice(id: @NotNull(message = "ID不能为空") Long?): ResponseData<String> {
        val invoice = purchaseOrderService.getInvoice(id!!)
        return ResponseData.success(invoice)
    }
}