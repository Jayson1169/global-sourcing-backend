package org.example.globalsourcing.controller

import org.example.globalsourcing.entity.Product
import org.example.globalsourcing.service.ProductService
import org.example.globalsourcing.util.ResponseData
import org.example.globalsourcing.validation.groups.Insert
import org.example.globalsourcing.validation.groups.Update
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/product")
@PreAuthorize("hasAnyRole('ADMIN', 'SALESPERSON', 'BUYER', 'WAREHOUSE_KEEPER')")
@Validated
class ProductController(private val productService: ProductService) {

    @PostMapping("/insert")
    fun insert(@Validated(Insert::class) @RequestBody product: Product): ResponseData<Product> {
        return ResponseData.success(productService.insert(product))
    }

    @PutMapping("/update")
    fun update(@Validated(Update::class) @RequestBody product: Product): ResponseData<Product> {
        return ResponseData.success(productService.update(product))
    }

    @DeleteMapping("/delete")
    fun delete(id: @NotNull(message = "ID不能为空") Long?): ResponseData<Product> {
        productService.delete(id!!)
        return ResponseData.success(null)
    }

    @GetMapping("/findAll")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<Product>> {
        val products = productService.findAll(page, size)
        return ResponseData.success(products)
    }

    @GetMapping("/findByBarcode")
    fun findByBarcode(@NotBlank(message = "条码不能为空") barcode: String?): ResponseData<Product> {
        val product = productService.findByBarcode(barcode!!)
        return ResponseData.success(product)
    }

    @GetMapping("/search")
    fun search(
        @NotBlank(message = "关键词不能为空") keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE.toString()) size: Int
    ): ResponseData<Page<Product>> {
        val products = productService.search(keyword!!, page, size)
        return ResponseData.success(products)
    }

    @GetMapping("/randomBarcode")
    fun randomBarcode(): ResponseData<String> {
        val barcode = productService.randomBarcode()
        return ResponseData.success(barcode)
    }

    @GetMapping("/getImage")
    @PreAuthorize("hasRole('USER')")
    fun getImage(@NotNull(message = "ID不能为空") id: Long?): ResponseData<String> {
        val image = productService.getImage(id!!)
        return ResponseData.success(image)
    }
}