package org.example.service.validation

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordHasherService {

    private val encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    fun hash(password: String, salt: String): String {
        val salted = password + salt
        return encoder.encode(salted)
    }

    fun matches(password: String, storedPassword: String, salt: String): Boolean {
        val salted = password + salt
        return encoder.matches(salted, storedPassword)
    }
}
