package org.example.repository

import org.example.model.db_models.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, UUID>
