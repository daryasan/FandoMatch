package org.example.repository

import org.example.models.db_models.FandomRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FandomRequestRepository : JpaRepository<FandomRequest, UUID>
