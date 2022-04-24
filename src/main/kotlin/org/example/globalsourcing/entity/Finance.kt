package org.example.globalsourcing.entity

import java.time.LocalDateTime

class Finance(var startTime: LocalDateTime, var endTime: LocalDateTime) {

    /**
     * 销售单数。
     */
    var saleOrderQuantity: Int = 0

    /**
     * 采购单数。
     */
    var purchaseOrderQuantity: Int = 0

    /**
     * 销售额，以分为单位。
     */
    var sales: Int = 0

    /**
     * 采购额，以分为单位。
     */
    var purchases: Int = 0

    /**
     * 销售员财务信息列表。
     */
    val salespersonFinanceItems: MutableList<SalespersonFinanceItem> = mutableListOf()

    /**
     * 采购员财务信息列表。
     */
    val buyerFinanceItems: MutableList<BuyerFinanceItem> = mutableListOf()

    /**
     * 单个销售员财务信息。
     */
    data class SalespersonFinanceItem(val salesperson: User, val sales: Int, val saleOrderQuantity: Int)

    /**
     * 单个采购员财务信息。
     */
    data class BuyerFinanceItem(val buyer: User, val purchases: Int, val purchaseOrderQuantity: Int)

    override fun toString(): String = "Finance(" +
            "startTime=$startTime, " +
            "endTime=$endTime, " +
            "saleOrderQuantity=$saleOrderQuantity, " +
            "purchaseOrderQuantity=$purchaseOrderQuantity, " +
            "sales=$sales, " +
            "purchases=$purchases, " +
            "salespersonFinanceItems=$salespersonFinanceItems, " +
            "buyerFinanceItems=$buyerFinanceItems" +
            ")"
}