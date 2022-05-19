package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.globalsourcing.util.MEDIUMTEXT_MAX_SIZE
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.PositiveOrZero
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
     * 销售单状态，可选值参考枚举类 [Status]，默认值 [Status.CREATED]。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var status: Status = Status.CREATED

    /**
     * 备注信息。
     */
    @Size(max = VARCHAR_MAX_SIZE, message = "备注信息不能超过{max}个字符")
    @Column
    var remark: String? = null

    /**
     * 已付金额。
     */
    @PositiveOrZero(message = "已付金额不能为负")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var paidAmount: Int = 0

    /**
     * 付款凭证（图片），base64格式存储。
     */
    @Size(max = MEDIUMTEXT_MAX_SIZE, message = "图片经过编码后不能超过{max}字节")
    @Column(columnDefinition = "mediumtext")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var certificate: String? = null

    /**
     * 销售单项目。
     */
    @Valid
    @OneToMany(mappedBy = "saleOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<SaleOrderItem> = mutableListOf()

    /**
     * 计算销售单商品总价。
     */
    val totalPrice: Int
        get() = items.sumOf { it.quantity * it.salePrice }

    /**
     * 销售单状态枚举类。
     */
    enum class Status {
        /**
         * 销售单创建。
         */
        CREATED,

        /**
         * 销售单已付款（用户完成支付）。
         */
        PAID,

        /**
         * 销售单已完成发货（转运员将销售单货物发出）。
         */
        DELIVERED;
    }

    override fun toString(): String = "SaleOrder(" +
            "salesperson=$salesperson, " +
            "address=$address, " +
            "status=$status, " +
            "remark=$remark, " +
            "paidAmount=$paidAmount" +
            ")"
}