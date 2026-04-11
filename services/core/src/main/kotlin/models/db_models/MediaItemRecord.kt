package org.example.models.db_models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "media_item")
data class MediaItemRecord(
    @Id
    @Column(name = "media_id")
    val mediaId: String,

    @Column(name = "media_type", nullable = false)
    val mediaType: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
