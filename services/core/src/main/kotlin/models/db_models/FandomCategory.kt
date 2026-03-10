package org.example.models.db_models

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "fandom_category")
data class FandomCategory(
    @Id
    @GeneratedValue
    val id: UUID,

    @Column(unique = true, nullable = false)
    val name: String
)