package org.example.repositories

import org.example.models.UserCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserCredentialsRepository : JpaRepository<UserCredentials, UUID> {
    fun findByUserInternalId(userId: UUID): UserCredentials?
}