package org.example.models.db_models

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

data class UserFandomId(
    val userId: UUID = UUID.randomUUID(),
    val fandomId: UUID = UUID.randomUUID()
) : Serializable

@Entity
@Table(name = "user_fandom")
@IdClass(UserFandomId::class)
data class UserFandom(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @Id
    @Column(name = "fandom_id")
    val fandomId: UUID
)
