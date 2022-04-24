package org.example.globalsourcing.repository

import org.example.globalsourcing.entity.SaleOrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface SaleOrderItemRepository : JpaRepository<SaleOrderItem, Long>