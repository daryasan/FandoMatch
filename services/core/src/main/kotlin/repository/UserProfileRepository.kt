package org.example.repository

import org.example.models.db_models.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserProfileRepository : JpaRepository<UserProfile, UUID> {
    fun findByUsername(username : String) : Optional<UserProfile>
}