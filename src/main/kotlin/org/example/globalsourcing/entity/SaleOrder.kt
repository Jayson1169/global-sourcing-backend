package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

/**
 * 销售单，一个销售单包含多种商品。
 */
@Entity
class SaleOrder : BaseEntity() {
    /**
     * 对应的销售员。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    lateinit var salesperson: User

    /**
     * 买家地址信息。
     */
    @NotBlank(message = "地址信息不能为空")
    @Size(max = VARCHAR_MAX_SIZE, message = "地址信息不能超过{max}个字符")
    @Column(nullable = false)
    var address: String? = null

    /**
     * 备注信息。
     */
    @Size(max = VARCHAR_MAX_SIZE, message = "备注信息不能超过{max}个字符")
    @Column
    var remark: String? = null

    /**
     * 是否已完成发货。
     */
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var delivered: Boolean = false

    /**
     * 销售单项目。
     */
    @Valid
    @OneToMany(mappedBy = "saleOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<SaleOrderItem> = mutableListOf()

    /**
     * 计算销售单商品总销售价。
     */
    fun totalPrice(): Int = items.sumOf { it.quantity * it.salePrice }

    override fun toString(): String = "SaleOrder(" +
            "salesperson=$salesperson, " +
            "address=$address, " +
            "remark=$remark, " +
            "delivered=$delivered" +
            ")"
}