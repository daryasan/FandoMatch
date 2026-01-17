package org.example.repository

import org.example.model.db_models.UserCredential
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserCredentialsRepository : JpaRepository<UserCredential, UUID> {
    fun findByUserUid(userId: UUID): UserCredential?
}