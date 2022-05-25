package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.globalsourcing.util.EXPRESS_NUMBER_PATTERN
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import org.example.globalsourcing.validation.groups.Update
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@Entity
class ExpressOrder : BaseEntity() {
    /**
     * 物流公司。
     */
    @NotBlank(message = "物流公司不能为空", groups = [Update::class])
    @Size(max = VARCHAR_MAX_SIZE, message = "物流公司长度不能超过{max}个字符")
    @Column
    var expressCompany: String? = null

    /**
     * 物流单号。
     */
    @NotBlank(message = "物流单号不能为空", groups = [Update::class])
    @Pattern(regexp = EXPRESS_NUMBER_PATTERN, message = "物流单号格式错误")
    @Column
    var expressNumber: String? = null

    /**
     * 物流单状态，可选值参考枚举类 [Status]，
     * 默认值 [Status.CREATED]。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var status = Status.CREATED

    /**
     * 发货人。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var deliverer: User? = null

    /**
     * 收货人。
     */
    @ManyToOne(cascade = [CascadeType.REFRESH])
    @JoinColumn
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var receiver: User? = null

    /**
     * 备注信息。
     */
    @Size(max = VARCHAR_MAX_SIZE, message = "备注信息不能超过{max}个字符")
    @Column
    var remark: String? = null

    /**
     * 物流单项目。
     */
    @OneToMany(mappedBy = "expressOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<ExpressOrderItem> = mutableListOf()

    /**
     * 物流单状态枚举类。
     */
    enum class Status {
        /**
         * 物流单已创建（由仓管员创建）。
         */
        CREATED,

        /**
         * 物流单已发出（由仓管员发出）。
         */
        DELIVERED,

        /**
         * 物流单已接收（由转运员接收）。
         */
        RECEIVED;
    }
}