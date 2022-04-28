package org.example.globalsourcing.controller

import org.example.globalsourcing.entity.SaleOrder
import org.example.globalsourcing.entity.SaleOrderItem
import org.example.globalsourcing.entity.User
import org.example.globalsourcing.service.SaleOrderService
import org.example.globalsourcing.util.EXPRESS_NUMBER_PATTERN
import org.example.globalsourcing.util.ResponseData
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import org.example.globalsourcing.validation.groups.Insert
import org.example.globalsourcing.validation.groups.Update
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.*

@RestController
@RequestMapping("/saleOrder")
@Validated
class SaleOrderController(private val saleOrderService: SaleOrderService) {

    @PostMapping("/insert")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON')")
    fun insert(
        @Validated(Insert::class) @RequestBody saleOrder: SaleOrder,
        authentication: Authentication
    ): ResponseData<SaleOrder> {
        saleOrder.salesperson = authentication.principal as User
        return ResponseData.success(saleOrderService.insert(saleOrder))
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON')")
    fun update(@Validated(Update::class) @RequestBody saleOrder: SaleOrder): ResponseData<SaleOrder> {
        return ResponseData.success(saleOrderService.update(saleOrder))
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON')")
    fun delete(@NotNull(message = "ID不能为空") id: Long?): ResponseData<SaleOrder?>? {
        saleOrderService.delete(id!!)
        return ResponseData.success(null)
    }

    @PostMapping("/insertItem")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON')")
    fun insertItem(
        @NotNull(message = "ID不能为空") saleOrderId: Long?,
        @Validated(Insert::class) @RequestBody item: SaleOrderItem
    ): ResponseData<SaleOrderItem> {
        val saleOrderItem = saleOrderService.insertItem(saleOrderId!!, item)
        return ResponseData.success(saleOrderItem)
    }

    @PutMapping("/updateItem")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON')")
    fun updateItem(@Validated(Update::class) @RequestBody item: SaleOrderItem): ResponseData<SaleOrderItem> {
        return ResponseData.success(saleOrderService.updateItem(item))
    }

    @DeleteMapping("/deleteItem")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON')")
    fun deleteItem(@NotNull(message = "ID不能为空") itemId: Long?): ResponseData<SaleOrder> {
        saleOrderService.deleteItem(itemId!!)
        return ResponseData.success(null)
    }

    @GetMapping("/isSaleOrderDeliverable")
    @PreAuthorize("hasRole('TRANSPORTER')")
    fun isSaleOrderDeliverable(id: @NotNull(message = "ID不能为空") Long?): ResponseData<Boolean> {
        val deliverable = saleOrderService.isSaleOrderDeliverable(id!!)
        return ResponseData.success(deliverable)
    }

    @PutMapping("/deliver")
    @PreAuthorize("hasRole('TRANSPORTER')")
    fun deliver(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotBlank(message = "物流公司不能为空")
        @Size(max = VARCHAR_MAX_SIZE, message = "物流公司长度不能超过{max}个字符") expressCompany: String?,
        @NotBlank(message = "物流单号不能为空")
        @Pattern(regexp = EXPRESS_NUMBER_PATTERN, message = "物流单号格式错误") expressNumber: String?
    ): ResponseData<SaleOrder> {
        val saleOrder = saleOrderService.deliver(id!!, expressCompany!!, expressNumber!!)
        return ResponseData.success(saleOrder)
    }

    @PutMapping("/deliverItem")
    @PreAuthorize("hasRole('TRANSPORTER')")
    fun deliverItem(
        @NotNull(message = "ID不能为空") itemId: Long?,
        @NotNull(message = "发货数量不能为空")
        @Positive(message = "发货数量不能小于1") quantity: Int?,
        @NotBlank(message = "物流公司不能为空")
        @Size(max = VARCHAR_MAX_SIZE, message = "物流公司长度不能超过{max}个字符") expressCompany: String?,
        @NotBlank(message = "物流单号不能为空")
        @Pattern(regexp = EXPRESS_NUMBER_PATTERN, message = "物流单号格式错误") expressNumber: String?
    ): ResponseData<SaleOrder> {
        val saleOrder = saleOrderService.deliverItem(itemId!!, quantity!!, expressCompany!!, expressNumber!!)
        return ResponseData.success(saleOrder)
    }

    @GetMapping("/findAll")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTER', 'SALESPERSON')")
    fun findAll(
        @RequestParam(required = false) salespersonId: Long?,
        @RequestParam(required = false) delivered: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<SaleOrder>> {
        val saleOrders = saleOrderService.findAll(salespersonId, delivered, page, size)
        return ResponseData.success(saleOrders)
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTER', 'SALESPERSON')")
    fun search(
        @NotBlank(message = "关键词不能为空") keyword: String?,
        @RequestParam(defaultValue = "0") page: Int?,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int?
    ): ResponseData<Page<SaleOrder>> {
        val saleOrders = saleOrderService.search(keyword!!, page!!, size!!)
        return ResponseData.success(saleOrders)
    }
}