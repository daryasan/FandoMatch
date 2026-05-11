package org.example.models.db_models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "match_filter")
data class MatchFilter(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @JdbcTypeCode(SqlTypes.ARRAY)
    val gender: List<String>? = null,

    @Column(name = "age_from")
    val ageFrom: Int? = null,

    @Column(name = "age_to")
    val ageTo: Int? = null,

    @Column(name = "only_in_user_city")
    val onlyInUserCity: Boolean? = false,

    @Column(name = "fandom_categories")
    @JdbcTypeCode(SqlTypes.ARRAY)
    val fandomCategory: List<String>? = null,

    @Column(name = "fandom_ids")
    @JdbcTypeCode(SqlTypes.ARRAY)
    val fandomIds: List<String>? = null
)