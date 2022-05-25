package org.example.globalsourcing.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.validation.constraints.PositiveOrZero

/**
 * 商品库存信息。
 */
@Entity
class Inventory : BaseEntity() {
    /**
     * 国外仓库库存。
     */
    @PositiveOrZero(message = "商品库存不能为负")
    @Column(nullable = false)
    var warehouseInventory: Int = 0

    /**
     * 国内仓库库存。
     */
    @PositiveOrZero(message = "商品库存不能为负")
    @Column(nullable = false)
    var hubInventory: Int = 0

    /**
     * 运输中库存。
     */
    @PositiveOrZero(message = "商品库存不能为负")
    @Column(nullable = false)
    var midwayInventory: Int = 0
}