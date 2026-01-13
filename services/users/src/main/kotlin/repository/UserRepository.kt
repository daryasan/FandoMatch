package org.example.repository

import org.example.model.db_models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?
}