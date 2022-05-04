package org.example.globalsourcing.service

import org.example.globalsourcing.entity.SaleOrder
import org.example.globalsourcing.entity.SaleOrderItem
import org.example.globalsourcing.entity.SaleOrderItem.Express
import org.example.globalsourcing.entity.User
import org.example.globalsourcing.repository.ProductRepository
import org.example.globalsourcing.repository.SaleOrderItemRepository
import org.example.globalsourcing.repository.SaleOrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
@Transactional
class SaleOrderService(
    private val saleOrderRepository: SaleOrderRepository,
    private val saleOrderItemRepository: SaleOrderItemRepository,
    private val productRepository: ProductRepository,
    private val commonService: CommonService
) {

    /**
     * 查询时的排序依据。
     */
    private val sort: Sort = Sort.by(Sort.Direction.DESC, "updateTime")

    /**
     * 新增销售单。
     */
    fun insert(saleOrder: SaleOrder): SaleOrder {
        Assert.notEmpty(saleOrder.items, "销售单项目不能为空！")
        for (item in saleOrder.items) {
            item.saleOrder = saleOrder
            val id = item.product!!.id
            item.product = commonService.findProduct(id)
        }

        return saleOrderRepository.save(saleOrder)
    }

    /**
     * 更新销售单主体信息，但不更新销售单商品信息以及销售员信息，
     * 需要在销售单中更新商品信息的，参考 [insertItem]、
     * [updateItem]、[deleteItem]。
     */
    fun update(saleOrder: SaleOrder): SaleOrder {
        val temp = commonService.findSaleOrder(saleOrder.id)
        temp.address = saleOrder.address
        temp.remark = saleOrder.remark
        return saleOrderRepository.save(temp)
    }

    /**
     * 依据ID删除销售单。
     */
    fun delete(id: Long) {
        saleOrderRepository.deleteById(id)
    }

    /**
     * 向指定销售单添加项目。
     */
    fun insertItem(saleOrderId: Long, item: SaleOrderItem): SaleOrderItem {
        val saleOrder = commonService.findSaleOrder(saleOrderId)
        item.saleOrder = saleOrder
        return saleOrderItemRepository.save(item)
    }

    /**
     * 更新单个销售单项目信息。
     */
    fun updateItem(item: SaleOrderItem): SaleOrderItem {
        val temp = commonService.findSaleOrderItem(item.id)
        item.saleOrder = temp.saleOrder
        return saleOrderItemRepository.save(item)
    }

    /**
     * 删除单个销售单项目。
     */
    fun deleteItem(itemId: Long) {
        saleOrderItemRepository.deleteById(itemId)
    }

    /**
     * 判断整个销售单能否直接发货，即所有商品的库存是否充足。
     */
    fun isSaleOrderDeliverable(id: Long): Boolean {
        val saleOrder = saleOrderRepository.findById(id)
            .filter { !it.delivered }
            .orElseThrow { ServiceException("id为'${id}'的商品不存在或已经完成发货！") }
        
        return saleOrder.items.all {
            it.product!!.inventory.hubInventory >= it.quantity && it.deliveredQuantity == 0
        }
    }

    /**
     * 对整个销售单的商品发货。
     */
    fun deliver(id: Long, expressCompany: String, expressNumber: String): SaleOrder {
        if (!isSaleOrderDeliverable(id)) {
            throw ServiceException("ID为'${id}'的销售单无法直接发货！")
        }

        val saleOrder = commonService.findSaleOrder(id)
        for (item in saleOrder.items) {
            // 更新库存
            val product = item.product!!
            val inventory = product.inventory
            val hubInventory = inventory.hubInventory
            inventory.hubInventory = hubInventory - item.quantity
            productRepository.save(product)
            item.deliveredQuantity = item.quantity
            item.expresses.add(Express(expressCompany, expressNumber))
        }

        return saleOrderRepository.save(saleOrder)
    }

    fun deliverItem(itemId: Long, quantity: Int, expressCompany: String, expressNumber: String): SaleOrderItem {
        val item: SaleOrderItem = saleOrderItemRepository.findById(itemId)
            .filter { !it.delivered }
            .orElseThrow { ServiceException("id为'${itemId}'的销售单项目不存在或已完成发货！") }

        val deliveredQuantity = item.deliveredQuantity + quantity
        if (deliveredQuantity > item.quantity) {
            throw ServiceException("发货数量超过销售单需发货的商品数量！")
        }

        // 检查并更新库存
        val product = item.product!!
        val inventory = product.inventory
        val hubInventory = inventory.hubInventory
        if (hubInventory < quantity) {
            throw ServiceException("ID为'${itemId}'的商品库存不足！")
        }

        inventory.hubInventory = hubInventory - quantity
        productRepository.save(product)

        item.deliveredQuantity = deliveredQuantity
        item.expresses.add(Express(expressCompany, expressNumber))

        return saleOrderItemRepository.save(item)
    }

    /**
     * 查询销售单，可选条件：销售员id [salespersonId] 、是否完成发货 [delivered]。
     */
    fun findAll(salespersonId: Long?, delivered: Boolean?, page: Int, size: Int): Page<SaleOrder> {
        val salesperson = salespersonId?.let { commonService.findUserAndCheckRole(it, User.Role.SALESPERSON) }

        return saleOrderRepository.findAll(
            { root, _, criteriaBuilder ->
                val predicates = mutableListOf<Predicate>()
                salesperson?.let { predicates.add(criteriaBuilder.equal(root.get<User>("salesperson"), it)) }
                delivered?.let { predicates.add(criteriaBuilder.equal(root.get<Boolean>("delivered"), it)) }

                criteriaBuilder.and(*predicates.toTypedArray())
            }, PageRequest.of(page, size, sort)
        )
    }

    /**
     * 按地址或备注信息搜索销售单。
     */
    fun search(keyword: String, page: Int, size: Int): Page<SaleOrder> {
        val pattern = "%$keyword%"
        return saleOrderRepository.findAll(
            { root, _, criteriaBuilder ->
                criteriaBuilder.or(
                    criteriaBuilder.like(root.get("address"), pattern),
                    criteriaBuilder.like(root.get("remark"), pattern)
                )
            }, PageRequest.of(page, size, sort)
        )
    }
}