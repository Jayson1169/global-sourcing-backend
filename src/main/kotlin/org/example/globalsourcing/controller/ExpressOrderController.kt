package org.example.globalsourcing.controller

import org.example.globalsourcing.entity.ExpressOrder
import org.example.globalsourcing.entity.ExpressOrder.Status
import org.example.globalsourcing.entity.User
import org.example.globalsourcing.service.ExpressOrderService
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
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@RestController
@RequestMapping("/expressOrder")
@Validated
class ExpressOrderController(private val expressOrderService: ExpressOrderService) {

    @PostMapping("/insert")
    @PreAuthorize("hasRole('WAREHOUSE_KEEPER')")
    fun insert(@Validated(Insert::class) @RequestBody expressOrder: ExpressOrder): ResponseData<ExpressOrder> {
        return ResponseData.success(expressOrderService.insert(expressOrder))
    }

    @PutMapping("/deliver")
    @PreAuthorize("hasRole('WAREHOUSE_KEEPER')")
    fun deliver(
        @NotNull(message = "ID不能为空") id: Long?,
        @NotBlank(message = "物流公司不能为空")
        @Size(max = VARCHAR_MAX_SIZE, message = "物流公司长度不能超过{max}个字符") expressCompany: String?,
        @NotBlank(message = "物流单号不能为空")
        @Pattern(regexp = EXPRESS_NUMBER_PATTERN, message = "物流单号格式错误") expressNumber: String?,
        authentication: Authentication
    ): ResponseData<ExpressOrder> {
        val deliverer = authentication.principal as User
        val expressOrder = expressOrderService.deliver(deliverer, id!!, expressCompany!!, expressNumber!!)
        return ResponseData.success(expressOrder)
    }

    @PutMapping("/receive")
    @PreAuthorize("hasRole('TRANSPORTER')")
    fun receive(
        @Validated(Update::class) @RequestBody expressOrder: ExpressOrder,
        authentication: Authentication
    ): ResponseData<ExpressOrder> {
        expressOrder.receiver = authentication.principal as User
        return ResponseData.success(expressOrderService.receive(expressOrder))
    }

    @GetMapping("/findById")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER', 'TRANSPORTER')")
    fun findById(@NotNull(message = "ID不能为空") id: Long?): ResponseData<ExpressOrder> {
        val expressOrder = expressOrderService.findById(id!!)
        return ResponseData.success(expressOrder)
    }

    @GetMapping("/findAll")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER', 'TRANSPORTER')")
    fun findAll(
        @RequestParam(required = false) status: Status?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<ExpressOrder>> {
        val expressOrders = expressOrderService.findAll(status, page, size)
        return ResponseData.success(expressOrders)
    }

    @GetMapping("/findAll")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_KEEPER', 'TRANSPORTER')")
    fun search(
        @NotBlank(message = "关键词不能为空") keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<ExpressOrder>> {
        val expressOrders = expressOrderService.search(keyword!!, page, size)
        return ResponseData.success(expressOrders)
    }
}