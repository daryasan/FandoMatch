package org.example.model.db_models

import jakarta.persistence.*
import org.example.model.db_models.enums.CredentialType
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_credentials")
class UserCredential(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false)
    val credentialType: CredentialType,

    @Column(name = "hash")
    var hash: String? = null,

    @Column(name = "salt")
    var salt: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

