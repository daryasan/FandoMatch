package org.example.service


import io.jsonwebtoken.Jwts
import jakarta.annotation.PostConstruct
import org.example.exceptions.BusinessException
import org.example.models.User
import org.example.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.privateKey}")
    private val privateKeyBase64: String,
    @Value("\${jwt.publicKey}")
    private val publicKeyBase64: String,
    private val userRepository: UserRepository
) {

    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    @PostConstruct
    fun initKeys() {
        privateKey = loadPrivateKey(privateKeyBase64)
        publicKey = loadPublicKey(publicKeyBase64)
    }

    fun getPublicKey() = publicKey

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

    fun validateAndLoadUser(authHeader: String): User {
        val token = extractToken(authHeader)
        val claims = try {
            extractUserId(token)
        } catch (e: Exception) {
            throw BusinessException("INVALID_TOKEN", "Токен недействителен")
        }
        val user = userRepository.findById(claims) ?: throw BusinessException(
            "USER_NOT_FOUND",
            "Пользователь не найден"
        )
        return user.get()
    }

    private fun extractToken(header: String): String {
        if (!header.startsWith("Bearer ")) {
            throw BusinessException("INVALID_AUTH_HEADER", "Некорректный Authorization header")
        }
        return header.removePrefix("Bearer ").trim()
    }

    fun extractUserId(token: String): UUID {
        val claims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return UUID.fromString(claims.subject)
    }

}
