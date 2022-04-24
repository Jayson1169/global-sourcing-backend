package org.example.globalsourcing.repository

import org.example.globalsourcing.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    fun existsByBarcode(barcode: String): Boolean

    fun findByBarcode(barcode: String): Product?
}