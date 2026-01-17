package org.example.model.db_models

import jakarta.persistence.*
import org.example.model.db_models.enums.TokenType
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "tokens")
class Token(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var internalId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    val tokenType: TokenType,

    @Column(name = "token_value", nullable = false, unique = true)
    val tokenValue: String,

    @Column(name = "issued_at", nullable = false)
    val issuedAt: LocalDateTime,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(name = "revoked", nullable = false)
    var revoked: Boolean = false
)
