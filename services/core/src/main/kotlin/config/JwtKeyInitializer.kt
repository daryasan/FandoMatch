package org.example.config

import org.example.client.UsersAdapter
import org.springframework.context.annotation.Configuration
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Configuration
open class JwtKeyInitializer(
    private val usersAdapter: UsersAdapter
) {

    val publicKey: PublicKey by lazy { decodePublicKey(usersAdapter.getBase64PublicJwt()) }

    private  fun decodePublicKey(base64: String): PublicKey {
        val cleaned = base64.replace("\n", "").trim()
        val keyBytes = Base64.getDecoder().decode(cleaned)
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }
}