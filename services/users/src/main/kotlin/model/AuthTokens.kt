package org.example.model

import java.util.UUID

data class AuthTokens(
    val uuid: UUID,
    val accessToken: String,
    val refreshToken: String
)