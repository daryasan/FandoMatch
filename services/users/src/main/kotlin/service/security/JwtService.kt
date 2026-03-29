package org.example.service.security

import io.github.oshai.kotlinlogging.KLogging
import io.jsonwebtoken.Jwts
import jakarta.annotation.PostConstruct
import org.example.config.JwtProperties
import org.example.exception.BusinessException
import org.example.model.GeneratedToken
import org.example.model.db_models.User
import org.example.repository.UserRepository
import org.example.service.TokenService.Companion.TOKEN_PREFIX
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.privateKey}")
    private val privateKeyBase64: String,
    @Value("\${jwt.publicKey}")
    private val publicKeyBase64: String,
    private val expirationProperties: JwtProperties,
    private val userRepository: UserRepository
) {

    companion object : KLogging()

    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    @PostConstruct
    fun initKeys() {
        privateKey = loadPrivateKey(privateKeyBase64)
        publicKey = loadPublicKey(publicKeyBase64)
    }

    fun getPublicKey() = publicKeyBase64

    private fun loadPrivateKey(base64: String): PrivateKey {
        val cleaned = base64.replace("\n", "").trim()
        val keyBytes = Base64.getDecoder().decode(cleaned)
        val spec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(spec)
    }

    private fun loadPublicKey(base64: String): PublicKey {
        val cleaned = base64.replace("\n", "").trim()
        val keyBytes = Base64.getDecoder().decode(cleaned)
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    fun validateAndLoadUser(token: String): User {
        val claims = try {
            extractUserId(token)
        } catch (e: Exception) {
            throw BusinessException("INVALID_TOKEN", "Invalid token")
        }
        val user = userRepository.findById(claims) ?: throw BusinessException(
            "USER_NOT_FOUND",
            "Пользователь не найден"
        )
        return user.get()
    }

    fun generateAccessToken(user: User): GeneratedToken {
        val now = Date()
        val expiry = Date(now.time + expirationProperties.accessExpiration)

        val token = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.uid.toString())
            .claim("username", user.username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(privateKey)
            .compact()

        return GeneratedToken(
            token = token,
            expiresAt = expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    fun generateRefreshToken() = GeneratedToken(
        UUID.randomUUID().toString(), LocalDateTime.now().plusDays(expirationProperties.refreshExpirationDays)
    )


    fun getTokenHash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    fun extractUserId(token: String): UUID {
        val tokenWithoutPrefix = token.removePrefix(TOKEN_PREFIX)
        try {
            val claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(tokenWithoutPrefix)
                .payload
            return UUID.fromString(claims.subject)
        } catch (e: Exception) {
            logger.info {"TOKEN VALIDATION ERROR: ${e::class.simpleName} - ${e.message}"}
            throw e
        }

    }

}
