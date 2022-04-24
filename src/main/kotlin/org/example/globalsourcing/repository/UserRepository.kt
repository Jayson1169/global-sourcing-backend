package org.example.globalsourcing.repository

import org.example.globalsourcing.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    fun findByUsername(username: String): User?

    fun existsByUsername(username: String): Boolean
}