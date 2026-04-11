package org.example.model.db_models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "device_token")
class DeviceToken(
    @Id
    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "fcm_token", nullable = false)
    var fcmToken: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
