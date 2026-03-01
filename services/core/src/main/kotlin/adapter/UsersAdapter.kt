package org.example.client

import com.fandomatch.users.api.TokenApi
import org.example.exceptions.UsersNotRespondingException
import org.springframework.stereotype.Service

@Service
class UsersAdapter(
    private val tokenApi: TokenApi
) {
    fun getBase64PublicJwt(): String {
        val response = tokenApi.tokenPublicJwtGet()
        if (response.publicKey.isBlank()) {
            throw UsersNotRespondingException("Empty public key")
        }
        return response.publicKey
    }
}

