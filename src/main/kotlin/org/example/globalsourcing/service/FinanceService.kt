package org.example.globalsourcing.service

import org.example.globalsourcing.entity.Finance
import org.example.globalsourcing.entity.PurchaseOrder
import org.example.globalsourcing.entity.SaleOrder
import org.example.globalsourcing.repository.PurchaseOrderRepository
import org.example.globalsourcing.repository.SaleOrderRepository
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

@Service
@Transactional
class FinanceService(
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val saleOrderRepository: SaleOrderRepository
) {

    /**
     * 统计当天的财务信息（从当天0点开始）。
     */
    fun countCurrentFinance(): Finance {
        val endTime = LocalDateTime.now()
        val startTime = endTime.truncatedTo(ChronoUnit.DAYS)
        return countFinance(startTime, endTime)
    }

    /**
     * 统计一段时间内的财务信息。
     */
    fun countFinance(startTime: LocalDateTime, endTime: LocalDateTime): Finance {
        Assert.isTrue(!endTime.isBefore(startTime), "结束时间不能早于开始时间！")

        val finance = Finance(startTime, endTime)
        val saleOrders = saleOrderRepository.findAll { root, _, criteriaBuilder ->
            criteriaBuilder.and(
                criteriaBuilder.between(root.get("updateTime"), startTime, endTime),
                criteriaBuilder.equal(root.get<SaleOrder.Status>("status"), SaleOrder.Status.DELIVERED)
            )
        }
        val purchaseOrders = purchaseOrderRepository.findAll { root, _, criteriaBuilder ->
            criteriaBuilder.and(
                criteriaBuilder.between(root.get("updateTime"), startTime, endTime),
                criteriaBuilder.equal(root.get<PurchaseOrder.Status>("status"), PurchaseOrder.Status.WAREHOUSED)
            )
        }

        for ((salesperson, orders) in saleOrders.groupBy { it.salesperson }) {
            val item = Finance.SalespersonFinanceItem(salesperson, orders.sumOf { it.totalPrice }, orders.size)
            finance.salespersonFinanceItems.add(item)
        }

        for ((buyer, orders) in purchaseOrders.groupBy { it.buyer }) {
            val item = Finance.BuyerFinanceItem(buyer!!, orders.sumOf { it.totalPrice }, orders.size)
            finance.buyerFinanceItems.add(item)
        }

        finance.sales = saleOrders.sumOf { it.totalPrice }
        finance.saleOrderQuantity = saleOrders.size
        finance.purchases = purchaseOrders.sumOf { it.totalPrice }
        finance.purchaseOrderQuantity = purchaseOrders.size

        return finance
    }
}