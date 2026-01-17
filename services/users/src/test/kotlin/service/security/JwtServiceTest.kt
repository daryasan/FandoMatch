package service.security

import BaseTest
import org.example.model.GeneratedToken
import org.example.service.security.JwtService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import utils.createUser
import java.security.KeyPairGenerator
import java.util.*

class JwtServiceTest : BaseTest() {

    @Autowired
    lateinit var jwtService: JwtService

    companion object {
        private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

        private val privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.private.encoded)

        private val publicKeyBase64 =
            Base64.getEncoder().encodeToString(keyPair.public.encoded)
        
        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            registry.add("jwt.privateKey") { privateKeyBase64 }
            registry.add("jwt.publicKey") { publicKeyBase64 }
            registry.add("jwt.expiration") { 3600000 }
        }
    }

    @Test
    fun `generateToken should create valid JWT and expiration`() {
        val user = createUser("test@example.com", "+123456789", "testuser")

        val generated: GeneratedToken = jwtService.generateToken(user)

        assertNotNull(generated.token)
        assertNotNull(generated.expiresAt)

        val extractedId = jwtService.extractUserId(generated.token)
        assertEquals(user.uid, extractedId)
    }

    @Test
    fun `getTokenHash should return deterministic SHA-256 hash`() {
        val token = "test-token-123"

        val hash1 = jwtService.getTokenHash(token)
        val hash2 = jwtService.getTokenHash(token)

        assertEquals(hash1, hash2)
        assertTrue(hash1.isNotBlank())
    }

    @Test
    fun `extractUserId should return correct UUID from token`() {
        val user = createUser("test@example.com", "+123456789", "testuser")

        val generated = jwtService.generateToken(user)

        val extracted = jwtService.extractUserId(generated.token)

        assertEquals(user.uid, extracted)
    }
}
