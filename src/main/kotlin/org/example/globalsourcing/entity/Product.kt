package org.example.globalsourcing.entity

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.globalsourcing.util.BARCODE_PATTERN
import org.example.globalsourcing.util.MEDIUMTEXT_MAX_SIZE
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.*

/**
 * 商品基本信息。
 */
@Entity
class Product : BaseEntity() {
    /**
     * 商品名。
     */
    @NotBlank(message = "商品名不能为空")
    @Size(max = VARCHAR_MAX_SIZE, message = "商品名长度不能超过{max}个字符")
    @Column(nullable = false)
    var name: String? = null

    /**
     * 商品条码，13位为真条码，15位为随机伪条码。
     */
    @NotBlank(message = "商品条码不能为空")
    @Pattern(regexp = BARCODE_PATTERN, message = "商品条码格式错误")
    @Column(nullable = false, unique = true)
    var barcode: String? = null

    /**
     * 商品品牌。
     */
    @NotBlank(message = "商品品牌不能为空")
    @Size(max = VARCHAR_MAX_SIZE, message = "商品品牌长度不能超过{max}个字符")
    @Column(nullable = false)
    var brand: String? = null

    /**
     * 商品型号规格。
     */
    @NotBlank(message = "商品型号规格不能为空")
    @Size(max = VARCHAR_MAX_SIZE, message = "商品型号规格长度不能超过{max}个字符")
    @Column(nullable = false)
    var specification: String? = null

    /**
     * 商品图片，base64格式。
     */
    @NotBlank(message = "商品图片不能为空")
    @Size(max = MEDIUMTEXT_MAX_SIZE, message = "图片经过编码后不能超过{max}字节")
    @Column(nullable = false, columnDefinition = "mediumtext")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    var image: String? = null

    /**
     * 商品库存。
     */
    @Valid
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(nullable = false)
    var inventory: Inventory = Inventory()

    /**
     * 商品参考价，以分为单位。
     */
    @Positive(message = "参考价必须大于0")
    @Column(nullable = false)
    var price: Int = 0

    /**
     * 商品海关信息。
     */
    @Valid
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(nullable = false)
    var customsInfo: CustomsInfo = CustomsInfo()

    /**
     * 商品备注。
     */
    @Size(max = VARCHAR_MAX_SIZE, message = "备注信息长度不能超过{max}个字符")
    @Column
    var remark: String? = null
}