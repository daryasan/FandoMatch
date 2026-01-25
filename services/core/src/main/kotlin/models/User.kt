package org.example.models

import jakarta.persistence.*
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


)
