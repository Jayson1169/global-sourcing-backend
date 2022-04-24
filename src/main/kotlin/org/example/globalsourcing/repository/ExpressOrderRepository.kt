package org.example.globalsourcing.repository

import org.example.globalsourcing.entity.ExpressOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ExpressOrderRepository : JpaRepository<ExpressOrder, Long>, JpaSpecificationExecutor<ExpressOrder>