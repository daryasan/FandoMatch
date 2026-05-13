package org.example.repository

import org.example.model.db_models.User
import org.example.model.db_models.enums.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?

    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.uid = :uid")
    fun updateEmail(uid: UUID, email: String)

    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.uid = :uid")
    fun updateStatus(uid: UUID, status: UserStatus)
}