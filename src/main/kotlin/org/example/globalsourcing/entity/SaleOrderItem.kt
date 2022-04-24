package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Entity
@TypeDef(name = "json", typeClass = JsonStringType::class)
class SaleOrderItem : BaseEntity() {
    /**
     * 对应的销售单。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn(nullable = false)
    @JsonIgnore
    lateinit var saleOrder: SaleOrder

    /**
     * 商品基本信息。
     */
    @NotNull(message = "商品基本信息不能为空")
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn(nullable = false)
    var product: Product? = null

    /**
     * 销售价，以分为单位。
     */
    @Positive(message = "销售价必须大于0")
    @Column(nullable = false)
    var salePrice: Int = 0

    /**
     * 销售数量。
     */
    @Positive(message = "销售数量必须大于0")
    @Column(nullable = false)
    var quantity: Int = 0

    /**
     * 已发货数量。
     */
    @Positive(message = "已发货数量不能为负")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var deliveredQuantity: Int = 0

    /**
     * 是否已完成发货。
     */
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var delivered: Boolean = false

    /**
     * 物流信息集合。
     */
    @Column(columnDefinition = "json")
    @Type(type = "json")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val expresses: MutableList<Express> = mutableListOf()

    data class Express(val expressCompany: String, val expressNumber: String)

    override fun toString(): String = "SaleOrderItem(" +
            "saleOrder=$saleOrder, " +
            "product=$product, " +
            "salePrice=$salePrice, " +
            "quantity=$quantity, " +
            "deliveredQuantity=$deliveredQuantity, " +
            "delivered=$delivered" +
            ")"
}