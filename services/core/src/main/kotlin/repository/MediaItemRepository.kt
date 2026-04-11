package org.example.repository

import org.example.models.db_models.MediaItemRecord
import org.springframework.data.jpa.repository.JpaRepository

interface MediaItemRepository : JpaRepository<MediaItemRecord, String> {
    fun findAllByMediaIdIn(mediaIds: List<String>): List<MediaItemRecord>
}
