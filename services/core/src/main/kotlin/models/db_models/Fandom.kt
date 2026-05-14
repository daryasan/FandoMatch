package org.example.models.db_models

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "fandom")
data class Fandom(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "category_id", nullable = false)
    val categoryId: UUID,

    @Column(nullable = false)
    val name: String,

    val description: String?
)