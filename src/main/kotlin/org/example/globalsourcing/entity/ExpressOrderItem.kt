package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@Entity
class ExpressOrderItem : BaseEntity() {
    /**
     * 对应的物流单。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn(nullable = false)
    @JsonIgnore
    lateinit var expressOrder: ExpressOrder

    /**
     * 商品基本信息。
     */
    @NotNull(message = "商品基本信息不能为空")
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn(nullable = false)
    var product: Product? = null

    /**
     * 发货数量。
     */
    @Positive(message = "发货数量必须大于0")
    @Column(nullable = false)
    var quantity: Int = 0

    /**
     * 收货数量。
     */
    @PositiveOrZero(message = "收货数量不能为负")
    @Column(nullable = false)
    var receivedQuantity: Int = 0

    override fun toString(): String = "ExpressOrderItem(" +
            "product=$product, " +
            "quantity=$quantity, " +
            "receivedQuantity=$receivedQuantity" +
            ")"
}