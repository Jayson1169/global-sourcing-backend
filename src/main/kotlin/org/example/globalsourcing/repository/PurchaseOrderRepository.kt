package org.example.globalsourcing.repository

import org.example.globalsourcing.entity.PurchaseOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface PurchaseOrderRepository : JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder>