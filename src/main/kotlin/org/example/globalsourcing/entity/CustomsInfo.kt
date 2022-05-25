package org.example.globalsourcing.entity

import org.example.globalsourcing.util.HS_CODE_PATTERN
import org.example.globalsourcing.util.TEXT_MAX_SIZE
import org.example.globalsourcing.util.VARCHAR_MAX_SIZE
import javax.persistence.Column
import javax.persistence.Entity
import javax.validation.constraints.*

/**
 * 商品海关信息。
 */
@Entity
class CustomsInfo : BaseEntity() {
    @Pattern(regexp = HS_CODE_PATTERN, message = "hsCode格式错误")
    @Column
    var hsCode: String? = null

    @Size(max = TEXT_MAX_SIZE, message = "Material Beschaffenheit不能超过{max}个字符")
    @Column(columnDefinition = "text")
    var materialBeschaffenheit: String? = null

    @Size(max = VARCHAR_MAX_SIZE, message = "Brand Article no.不能超过{max}个字符")
    @Column
    var brandArticleNo: String? = null

    @Size(max = VARCHAR_MAX_SIZE, message = "Brand不能超过{max}个字符")
    @Column
    var brand: String? = null

    @Size(max = VARCHAR_MAX_SIZE, message = "Article Name不能超过{max}个字符")
    @Column
    var articleName: String? = null

    @PositiveOrZero(message = "unitPrice不能为负")
    @Column
    var unitPrice: Int? = null
}