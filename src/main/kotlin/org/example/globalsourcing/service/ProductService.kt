package org.example.globalsourcing.service

import org.apache.commons.lang3.RandomStringUtils
import org.example.globalsourcing.entity.Product
import org.example.globalsourcing.repository.ProductRepository
import org.example.globalsourcing.util.RANDOM_BARCODE_LENGTH
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val commonService: CommonService
) {

    /**
     * 查询时的排序依据。
     */
    private val sort: Sort = Sort.by(Sort.Direction.DESC, "updateTime")

    /**
     * 新增商品基本信息，要求商品条码不能重复。
     */
    fun insert(product: Product): Product {
        val barcode = product.barcode!!
        if (productRepository.existsByBarcode(barcode)) {
            throw ServiceException("条码为'${barcode}'的商品已存在！")
        }

        return productRepository.save(product)
    }

    /**
     * 更新商品基本信息，要求更新后条码不重复。
     */
    fun update(product: Product): Product {
        val barcode = product.barcode!!
        val temp: Product = commonService.findProduct(product.id)

        // 有更新条码的情况，检查更新后条码是否重复
        if (temp.barcode != barcode && productRepository.existsByBarcode(barcode)) {
            throw ServiceException("条码为'${barcode}'的商品已存在！")
        }
        return productRepository.save(product)
    }

    /**
     * 依据ID删除商品基本信息。
     */
    fun delete(id: Long) {
        productRepository.deleteById(id)
    }

    /**
     * 获取所有商品。
     */
    fun findAll(page: Int, size: Int): Page<Product> {
        return productRepository.findAll(PageRequest.of(page, size, sort))
    }

    /**
     * 依据条码获取指定商品信息。
     */
    fun findByBarcode(barcode: String): Product {
        return productRepository.findByBarcode(barcode) ?: throw ServiceException("不存在条码为'${barcode}'的商品！")
    }

    /**
     * 按照商品名、商品品牌或型号搜索商品。
     */
    fun search(keyword: String, page: Int, size: Int): Page<Product> {
        val pattern = "%${keyword}%"
        return productRepository.findAll(
            { root, _, criteriaBuilder ->
                criteriaBuilder.or(
                    criteriaBuilder.like(root.get("name"), pattern),
                    criteriaBuilder.like(root.get("brand"), pattern),
                    criteriaBuilder.like(root.get("specification"), pattern)
                )
            }, PageRequest.of(page, size, sort)
        )
    }

    /**
     * 随机生成条码，并保证条码在数据库中唯一。
     */
    fun randomBarcode(): String {
        while (true) {
            val barcode = RandomStringUtils.randomNumeric(RANDOM_BARCODE_LENGTH)
            if (!productRepository.existsByBarcode(barcode)) {
                return barcode
            }
        }
    }

    /**
     * 依据ID获取商品图片。
     */
    fun getImage(id: Long): String {
        val product = commonService.findProduct(id)
        return product.image!!
    }
}