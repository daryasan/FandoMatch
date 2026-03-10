package org.example.client

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.api.UserApi
import com.fandomatch.users.model.ResponseStatus
import com.fandomatch.users.model.UserByIdRequest
import com.fandomatch.users.model.UserCredentials
import io.github.oshai.kotlinlogging.KLogging
import org.example.exceptions.UsersNotRespondingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class UsersAdapter(
    @Value("\${service.api-key}") private val validApiKey: String,
    private val tokenApi: TokenApi,
    private val userApi: UserApi
) {

    companion object : KLogging()

    fun getBase64PublicJwt(): String {
        val response = tokenApi.tokenPublicJwtGet()
        if (response.publicKey.isBlank()) {
            logger.error { "Users api responded with empty public key" }
            throw UsersNotRespondingException("Empty public key")
        }
        return response.publicKey
    }

    fun getUserCredentialsByUuid(uuid: UUID): UserCredentials {
        logger.info { "Requesting user info from users service" }
        val response = userApi.usersGetByIdPost(UserByIdRequest(uuid), validApiKey)

        if (response.status != ResponseStatus.SUCCESS) {
            logger.error { "Users api responded with ${response.status} with error ${response.errorResponse}" }
            throw UsersNotRespondingException("Empty public key")
        }
        return response.successResponse!!
    }

}

