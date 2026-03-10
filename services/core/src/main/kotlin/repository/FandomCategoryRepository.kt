package org.example.repository

import org.example.models.db_models.FandomCategory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FandomCategoryRepository : JpaRepository<FandomCategory, UUID>