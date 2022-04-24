package org.example.globalsourcing.repository

import org.example.globalsourcing.entity.SaleOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface SaleOrderRepository : JpaRepository<SaleOrder, Long>, JpaSpecificationExecutor<SaleOrder>