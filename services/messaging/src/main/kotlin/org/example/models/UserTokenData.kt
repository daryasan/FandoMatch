package org.example.models

import java.util.UUID

data class UserTokenData(
    val userId: UUID,
    val username: String
)
