package org.example.globalsourcing.service

import org.example.globalsourcing.entity.ExpressOrder
import org.example.globalsourcing.entity.ExpressOrder.Status
import org.example.globalsourcing.entity.User
import org.example.globalsourcing.repository.ExpressOrderRepository
import org.example.globalsourcing.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
@Transactional
class ExpressOrderService(
    private val expressOrderRepository: ExpressOrderRepository,
    private val productRepository: ProductRepository,
    private val commonService: CommonService
) {

    /**
     * 新增物流单，物流单状态为初始状态 [Status.CREATED]。
     */
    fun insert(expressOrder: ExpressOrder): ExpressOrder {
        Assert.notEmpty(expressOrder.items, "物流单项目不能为空！")
        for (item in expressOrder.items) {
            item.expressOrder = expressOrder
            item.product = commonService.findProduct(item.product!!.id)
        }

        return expressOrderRepository.save(expressOrder)
    }

    /**
     * 将物流单发出，物流单状态由 [Status.CREATED]
     * 更新为 [Status.DELIVERED]。
     */
    fun deliver(deliverer: User, id: Long, expressCompany: String, expressNumber: String): ExpressOrder {
        val expressOrder = commonService.findExpressOrderAndCheckStatus(id, Status.CREATED)

        // 更新库存
        for (item in expressOrder.items) {
            val product = item.product!!
            val inventory = product.inventory
            val quantity = item.quantity
            if (inventory.warehouseInventory < quantity) {
                throw ServiceException("ID为'${product.id}'的商品库存不足${quantity}件！")
            }

            inventory.warehouseInventory -= quantity
            inventory.midwayInventory += quantity
            productRepository.save(product)
        }

        expressOrder.expressCompany = expressCompany
        expressOrder.expressNumber = expressNumber
        expressOrder.deliverer = deliverer
        expressOrder.status = Status.DELIVERED
        return expressOrderRepository.save(expressOrder)
    }

    /**
     * 国内转运员接收物流单。
     */
    fun receive(expressOrder: ExpressOrder): ExpressOrder {
        val temp = commonService.findExpressOrderAndCheckStatus(expressOrder.id, Status.DELIVERED)

        // 验证物流单项目及数量
        val items = expressOrder.items
        for (tempItem in temp.items) {
            val item = items.find { it == tempItem } ?: throw ServiceException("物流单项目信息异常！")
            val deliveredQuantity = tempItem.quantity
            val receivedQuantity = item.receivedQuantity
            if (receivedQuantity > deliveredQuantity) {
                throw ServiceException("ID为'${item.id}'的物流单项目收货数量超过发货数量！")
            }

            tempItem.receivedQuantity = receivedQuantity
        }

        // 更新库存
        for (item in temp.items) {
            val product = item.product!!
            val inventory = product.inventory
            inventory.midwayInventory -= item.quantity
            inventory.hubInventory += item.receivedQuantity
            productRepository.save(product)
        }

        temp.receiver = expressOrder.receiver
        temp.status = Status.RECEIVED
        return expressOrderRepository.save(temp)
    }

    /**
     * 查询物流单，可选条件：状态 [status]。
     */
    fun findAll(status: Status?, page: Int, size: Int): Page<ExpressOrder> {
        return expressOrderRepository.findAll(
            { root, _, criteriaBuilder ->
                val predicates = mutableListOf<Predicate>()
                status?.let { predicates.add(criteriaBuilder.equal(root.get<Status>("status"), it)) }

                criteriaBuilder.and(*predicates.toTypedArray())
            }, PageRequest.of(page, size, SORT)
        )
    }

    companion object {
        /**
         * 查询时的排序依据。
         */
        private val SORT: Sort = Sort.by(Sort.Direction.DESC, "updateTime")
    }
}