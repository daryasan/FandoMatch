package org.example.repository

import org.example.model.db_models.Token
import org.example.model.db_models.User
import org.example.model.db_models.enums.TokenType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TokenRepository : JpaRepository<Token, UUID> {
    fun findByTokenValueAndTokenType(value: String, type: TokenType): Token?

    fun findAllByUserAndTokenTypeAndRevokedFalse(
        user: User,
        tokenType: TokenType
    ): List<Token>
}