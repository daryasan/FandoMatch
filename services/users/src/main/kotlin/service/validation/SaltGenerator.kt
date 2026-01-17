package org.example.service.validation

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class SaltGenerator {
    private val random = SecureRandom()

    fun generateSalt(): String {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }
}