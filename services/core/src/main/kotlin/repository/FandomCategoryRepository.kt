package org.example.repository

import org.example.models.db_models.FandomCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FandomCategoryRepository : JpaRepository<FandomCategory, UUID> {
    fun findByName(name: String): FandomCategory?
}