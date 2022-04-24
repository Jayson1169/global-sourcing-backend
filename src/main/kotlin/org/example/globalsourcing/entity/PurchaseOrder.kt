package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.globalsourcing.util.MEDIUMTEXT_MAX_SIZE
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import org.example.globalsourcing.validation.groups.Update
import java.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.*

@Entity
class PurchaseOrder : BaseEntity() {
    /**
     * 对应的采购员。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var buyer: User? = null

    /**
     * 采购单状态，可选值参考枚举类[Status]，
     * 默认值[Status.CREATED]。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var status: Status = Status.CREATED

    /**
     * 采购单发票（图片），base64格式存储。
     */
    @NotBlank(message = "采购单发票不能为空", groups = [Update::class])
    @Size(max = MEDIUMTEXT_MAX_SIZE, message = "图片经过编码后不能超过{max}字节")
    @Column(columnDefinition = "mediumtext")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var invoice: String? = null

    /**
     * 发票日期（只包含日期不包含时间）。
     */
    @NotNull(message = "采购单发票日期不能为空", groups = [Update::class])
    @PastOrPresent(message = "采购单发票日期不能晚于当前日期")
    @Column
    var invoiceDate: LocalDate? = null

    /**
     * 采购照片，base64格式存储。
     */
    @NotBlank(message = "采购照片不能为空", groups = [Update::class])
    @Size(max = MEDIUMTEXT_MAX_SIZE, message = "图片经过编码后不能超过{max}字节")
    @Column(columnDefinition = "mediumtext")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var photo: String? = null

    /**
     * 商品基本信息。
     */
    @NotNull(message = "商品基本信息不能为空")
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn(nullable = false)
    var product: Product? = null

    /**
     * 采购价，以分为单位。
     */
    @Positive(message = "采购价必须大于0", groups = [Update::class])
    @Column(nullable = false)
    var purchasePrice: Int = 0

    /**
     * 采购数量。
     */
    @Positive(message = "采购数量必须大于0")
    @Column(nullable = false)
    var quantity: Int = 0

    /**
     * 已采购的数量。
     */
    @PositiveOrZero(message = "已采购数量不能为负")
    @Column(nullable = false)
    var purchasedQuantity: Int = 0

    /**
     * 已入库的数量。
     */
    @PositiveOrZero(message = "已入库数量不能为负")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var warehousedQuantity: Int = 0

    /**
     * 驳回原因，仅采购单被驳回时有效。
     */
    @Size(max = VARCHAR_MAX_SIZE, message = "驳回理由不能超过{max}个字符")
    @Column
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var rejectReason: String? = null

    /**
     * 对应的仓管员。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var warehouseKeeper: User? = null

    /**
     * 采购单状态枚举类。
     */
    enum class Status {
        /**
         * 采购单创建。
         */
        CREATED,

        /**
         * 采购单待采购（由管理员将采购单分配至采购员）。
         */
        READY,

        /**
         * 采购单待核验（采购员完成采购，等待管理员核验）。
         */
        PENDING,

        /**
         * 采购单被驳回（需要重新返回至采购员采购）。
         */
        REJECTED,

        /**
         * 采购单已完成核验（管理员核验通过，等待入库）。
         */
        CONFIRMED,

        /**
         * 采购单商品已入库（仓管员已确认入库）。
         */
        WAREHOUSED;
    }

    /**
     * 计算采购单总价。
     */
    fun totalPrice(): Int = purchasePrice * quantity

    companion object {
        /**
         * 依据商品 [product] 和采购数量 [quantity] 创建采购单。
         */
        fun of(product: Product, quantity: Int): PurchaseOrder {
            val purchaseOrder = PurchaseOrder()
            purchaseOrder.product = product
            purchaseOrder.quantity = quantity
            return purchaseOrder
        }
    }
}