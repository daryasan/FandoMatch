package org.example.models.db_models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "match_filter")
data class MatchFilter(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    val gender: String? = null,

    @Column(name = "age_from")
    val ageFrom: Int? = null,

    @Column(name = "age_to")
    val ageTo: Int? = null,

    val city: String? = null,

    @Column(name = "fandom_category")
    val fandomCategory: UUID? = null,

    @Column(name = "fandom_id")
    val fandomId: UUID? = null
)