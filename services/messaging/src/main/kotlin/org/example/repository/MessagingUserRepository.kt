package org.example.repository

import org.example.models.db_models.MessagingUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MessagingUserRepository : JpaRepository<MessagingUser, UUID>
