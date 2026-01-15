package org.example.service.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.annotation.PostConstruct
import org.example.model.GeneratedToken
import org.example.model.db_models.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.ZoneId
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.privateKey}") private val privateKeyBase64: String,
    @Value("\${jwt.publicKey}") private val publicKeyBase64: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) {

    private lateinit var privateKey: PrivateKey
    private lateinit var publicKey: PublicKey

    @PostConstruct
    fun initKeys() {
        privateKey = loadPrivateKey(privateKeyBase64)
        publicKey = loadPublicKey(publicKeyBase64)
    }

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

    fun generateToken(user: User): GeneratedToken {
        val now = Date()
        val expiry = Date(now.time + expiration)

        val token = Jwts.builder()
            .setSubject(user.internalId.toString())
            .claim("username", user.username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()

        return GeneratedToken(
            token = token,
            expiresAt = expiry.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }


    fun getTokenHash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    fun extractUserId(token: String): UUID {
        val claims = Jwts.parser()
            .setSigningKey(publicKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return UUID.fromString(claims.subject)
    }
}
