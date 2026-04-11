package org.example.models.db_models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "user_profile")
data class UserProfile(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    val name: String?,

    @Column(nullable = false)
    val username: String,

    val bio: String?,

    @Column(name = "avatar_media_id")
    val avatarMediaId: String?,

    @Column(name = "background_media_id")
    val backgroundMediaId: String?,

    val gender: String?,

    @Column(name = "birth_date")
    val birthDate: LocalDate?,

    val city: String?,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)