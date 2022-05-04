package org.example.globalsourcing.service

import org.example.globalsourcing.entity.Product
import org.example.globalsourcing.entity.PurchaseOrder
import org.example.globalsourcing.entity.PurchaseOrder.Status
import org.example.globalsourcing.entity.User
import org.example.globalsourcing.entity.User.Role
import org.example.globalsourcing.repository.ProductRepository
import org.example.globalsourcing.repository.PurchaseOrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
@Transactional
class PurchaseOrderService(
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val productRepository: ProductRepository,
    private val commonService: CommonService
) {

    /**
     * 查询时的排序依据。
     */
    private val sort: Sort = Sort.by(Sort.Direction.DESC, "updateTime")

    /**
     * 依据商品ID [productId] 和采购数量 [quantity] 创建采购单。
     */
    fun create(productId: Long, quantity: Int): PurchaseOrder {
        val product: Product = commonService.findProduct(productId)
        return purchaseOrderRepository.save(PurchaseOrder.of(product, quantity))
    }

    /**
     * 管理员分配采购单给采购员，采购单状态从 [Status.CREATED] 更新为 [Status.READY]。
     */
    fun assign(id: Long, buyerId: Long, quantity: Int): PurchaseOrder {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id, Status.CREATED)
        val buyer = commonService.findUserAndCheckRole(buyerId, Role.BUYER)

        purchaseOrder.buyer = buyer
        purchaseOrder.quantity = quantity
        purchaseOrder.status = Status.READY
        return purchaseOrderRepository.save(purchaseOrder)
    }

    /**
     * 采购员上传采购单信息，交给管理员核验，
     * 采购单状态从 [Status.READY] 或 [Status.REJECTED] 更新为 [Status.PENDING]。
     */
    fun uploadPurchaseInfo(purchaseOrder: PurchaseOrder): PurchaseOrder {
        val id = purchaseOrder.id
        val purchasedQuantity = purchaseOrder.purchasedQuantity
        val temp = commonService.findPurchaseOrderAndCheckStatus(id, Status.READY, Status.REJECTED)

        if (temp.buyer != purchaseOrder.buyer) {
            throw ServiceException("ID为'${id}'的采购单与当前采购员不匹配！")
        }
        if (purchasedQuantity != temp.quantity) {
            throw ServiceException("已采购数量与待采购的数量不等!")
        }

        temp.invoice = purchaseOrder.invoice
        temp.invoiceDate = purchaseOrder.invoiceDate
        temp.purchasePrice = purchaseOrder.purchasePrice
        temp.purchasedQuantity = purchasedQuantity
        temp.photo = purchaseOrder.photo
        temp.status = Status.PENDING
        return purchaseOrderRepository.save(temp)
    }

    /**
     * 管理员核验采购单，采购单状态从 [Status.PENDING] 更新为 [Status.CONFIRMED]。
     */
    fun assignToWarehouseKeeper(id: Long, warehouseKeeperId: Long): PurchaseOrder {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id, Status.PENDING)
        val warehouseKeeper = commonService.findUserAndCheckRole(warehouseKeeperId, Role.WAREHOUSE_KEEPER)

        purchaseOrder.warehouseKeeper = warehouseKeeper
        purchaseOrder.status = Status.CONFIRMED
        purchaseOrder.rejectReason = null
        return purchaseOrderRepository.save(purchaseOrder)
    }

    /**
     * 管理员驳回采购单，采购单状态从 [Status.PENDING] 更新为 [Status.REJECTED]。
     */
    fun reject(id: Long, rejectReason: String): PurchaseOrder {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id, Status.PENDING)

        purchaseOrder.status = Status.REJECTED
        purchaseOrder.rejectReason = rejectReason
        return purchaseOrderRepository.save(purchaseOrder)
    }

    /**
     * 采购单商品入库，入库数量足够时采购单由 [Status.CONFIRMED] 更新为 [Status.WAREHOUSED]，
     * 否则采购单状态维持在 [Status.CONFIRMED]。
     */
    fun putIntoWarehouse(warehouseKeeper: User, id: Long, quantity: Int): PurchaseOrder {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id, Status.CONFIRMED)
        val product = purchaseOrder.product!!
        if (purchaseOrder.warehouseKeeper != warehouseKeeper) {
            throw ServiceException("ID为'${id}'的采购单与当前仓管员不匹配！")
        }

        val warehousedQuantity = purchaseOrder.warehousedQuantity + quantity
        val purchasedQuantity = purchaseOrder.purchasedQuantity
        if (warehousedQuantity > purchaseOrder.purchasedQuantity) {
            throw ServiceException("入库数量超过采购数量，无法入库！")
        }

        val inventory = product.inventory
        inventory.warehouseInventory += quantity
        productRepository.save(product)

        purchaseOrder.warehousedQuantity = warehousedQuantity
        if (warehousedQuantity == purchasedQuantity) {
            purchaseOrder.status = Status.WAREHOUSED
        }

        return purchaseOrderRepository.save(purchaseOrder)
    }

    /**
     * 在采购单入库数量不够的情况下完成强制完成入库，采购单状态从 [Status.CONFIRMED] 更新为 [Status.WAREHOUSED]。
     */
    fun forceFinishWarehousing(warehouseKeeper: User, id: Long): PurchaseOrder {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id, Status.CONFIRMED)
        if (purchaseOrder.warehouseKeeper != warehouseKeeper) {
            throw ServiceException("ID为'${id}'的采购单与当前仓管员不匹配！")
        }

        purchaseOrder.status = Status.WAREHOUSED
        return purchaseOrderRepository.save(purchaseOrder)
    }

    /**
     * 依据ID删除采购单，需要采购单处于未采购状态，即 [Status.CREATED] 或 [Status.READY]。
     */
    fun delete(id: Long) {
        val purchaseOrder = purchaseOrderRepository.findById(id).orElse(null) ?: return
        if (purchaseOrder.status != Status.CREATED && purchaseOrder.status != Status.READY) {
            throw ServiceException("ID为'${id}'的采购单无法删除！")
        }
        purchaseOrderRepository.delete(purchaseOrder)
    }

    /**
     * 查询采购单，可选条件：采购员id [buyerId]、仓管员id [warehouseKeeperId]、状态 [status]。
     */
    fun findAll(buyerId: Long?, warehouseKeeperId: Long?, status: Status?, page: Int, size: Int): Page<PurchaseOrder> {
        val buyer = buyerId?.let { commonService.findUserAndCheckRole(it, Role.BUYER) }
        val warehouseKeeper = warehouseKeeperId?.let { commonService.findUserAndCheckRole(it, Role.WAREHOUSE_KEEPER) }

        return purchaseOrderRepository.findAll(
            { root, _, criteriaBuilder ->
                val predicates = mutableListOf<Predicate>()
                buyer?.let { predicates.add(criteriaBuilder.equal(root.get<User>("buyer"), it)) }
                warehouseKeeper?.let { predicates.add(criteriaBuilder.equal(root.get<User>("warehouseKeeper"), it)) }
                status?.let { predicates.add(criteriaBuilder.equal(root.get<Status>("status"), it)) }

                criteriaBuilder.and(*predicates.toTypedArray())
            }, PageRequest.of(page, size, sort)
        )
    }

    /**
     * 依据ID获取采购照片。
     */
    fun getPhoto(id: Long): String {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id)
        return purchaseOrder.photo ?: throw ServiceException("ID为'${id}'的采购单的采购照片尚未上传！")
    }

    /**
     * 依据ID获取发票。
     */
    fun getInvoice(id: Long): String? {
        val purchaseOrder = commonService.findPurchaseOrderAndCheckStatus(id)
        return purchaseOrder.invoice ?: throw ServiceException("ID为'${id}'的采购单的发票尚未上传！")
    }
}