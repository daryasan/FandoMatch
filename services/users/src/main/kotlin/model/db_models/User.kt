package org.example.model.db_models

import jakarta.persistence.*
import org.example.model.db_models.enums.UserStatus
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "\"user\"")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uid: UUID? = null,

    @Column(name = "email", unique = true)
    val email: String? = null,

    @Column(name = "phone", unique = true)
    val phone: String? = null,

    @Column(name = "username", nullable = false, unique = true)
    val username: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: UserStatus = UserStatus.ACTIVE,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val tokens: Set<Token> = emptySet(),

    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val credentials: MutableSet<UserCredential> = mutableSetOf()

)
