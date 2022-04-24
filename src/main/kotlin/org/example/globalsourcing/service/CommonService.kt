package org.example.globalsourcing.service

import org.apache.commons.lang3.ArrayUtils
import org.example.globalsourcing.entity.*
import org.example.globalsourcing.repository.*
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * 公共 Service 层，存放 Service 层通用方法和常量。
 */
@Service
@Transactional
class CommonService(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val saleOrderRepository: SaleOrderRepository,
    private val saleOrderItemRepository: SaleOrderItemRepository,
    private val expressOrderRepository: ExpressOrderRepository
) {

    /**
     * 依据ID查找指定用户，并核验其角色。
     */
    internal fun findUserAndCheckRole(id: Long, role: User.Role): User {
        return userRepository.findById(id)
            .filter { it.role == role }
            .orElseThrow { ServiceException("ID为'${id}'的用户不存在或不是${role.description}！") }
    }

    /**
     * 依据ID查找指定采购单并核验状态是否在预期状态列表中。
     */
    internal fun findPurchaseOrderAndCheckStatus(id: Long, vararg statuses: PurchaseOrder.Status): PurchaseOrder {
        return purchaseOrderRepository.findById(id)
            .filter { statuses.isEmpty() || ArrayUtils.contains(statuses, it.status) }
            .orElseThrow { ServiceException("ID为'${id}'的采购单不存在或状态异常！") }
    }

    /**
     * 依据ID查找指定销售单。
     */
    internal fun findSaleOrder(id: Long): SaleOrder {
        return saleOrderRepository.findById(id).orElseThrow { ServiceException("ID为'${id}'的销售单不存在！") }
    }

    /**
     * 依据ID查找指定销售单项目。
     */
    internal fun findSaleOrderItem(id: Long): SaleOrderItem {
        return saleOrderItemRepository.findById(id).orElseThrow { ServiceException("ID为'${id}'的销售单项目不存在！") }
    }

    /**
     * 依据ID查找指定商品。
     */
    internal fun findProduct(id: Long): Product {
        return productRepository.findById(id).orElseThrow { ServiceException("ID为'${id}'的商品不存在！") }
    }

    /**
     * 依据ID查找指定物流单并核验状态是否在预期状态列表中。
     */
    internal fun findExpressOrderAndCheckStatus(id: Long, vararg statuses: ExpressOrder.Status): ExpressOrder {
        return expressOrderRepository.findById(id)
            .filter { statuses.isEmpty() || ArrayUtils.contains(statuses, it.status) }
            .orElseThrow { ServiceException("ID为'${id}'的物流单不存在或状态异常！") }
    }
}