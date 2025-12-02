package org.example.models

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "\"user\"")
data class User(
    @Id
    @Column(name = "internal_id", nullable = false, updatable = false)
    val internalId: UUID = UUID.randomUUID(),

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

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val credentials: UserCredentials? = null
)
