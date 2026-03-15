package org.example.repository

import org.example.models.db_models.MatchFilter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MatchFilterRepository : JpaRepository<MatchFilter, UUID>