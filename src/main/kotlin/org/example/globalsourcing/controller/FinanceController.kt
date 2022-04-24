package org.example.globalsourcing.controller

import org.example.globalsourcing.entity.Finance
import org.example.globalsourcing.service.FinanceService
import org.example.globalsourcing.util.ResponseData
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Past
import javax.validation.constraints.PastOrPresent

@RestController
@RequestMapping("/finance")
@PreAuthorize("hasAnyRole('ADMIN', 'TREASURER')")
@Validated
class FinanceController(private val financeService: FinanceService) {

    @GetMapping("/countCurrentFinance")
    fun countCurrentFinance(): ResponseData<Finance> {
        return ResponseData.success(financeService.countCurrentFinance())
    }

    @GetMapping("/countFinance")
    fun countFinance(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "开始日期不能为空")
        @Past(message = "开始日期不能晚于当前日期") startDate: LocalDate?,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "结束日期不能为空")
        @PastOrPresent(message = "结束日期不能晚于当前日期") endDate: LocalDate?
    ): ResponseData<Finance> {
        val finance = financeService.countFinance(startDate!!.atStartOfDay(), endDate!!.plusDays(1).atStartOfDay())
        return ResponseData.success(finance)
    }
}