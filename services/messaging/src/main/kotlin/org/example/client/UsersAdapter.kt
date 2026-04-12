package org.example.client

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.api.UserApi
import com.fandomatch.users.model.GetFcmTokenRequest
import com.fandomatch.users.model.UserByIdRequest
import io.github.oshai.kotlinlogging.KLogging
import org.example.exceptions.UsersNotRespondingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UsersAdapter(
    private val tokenApi: TokenApi,
    private val userApi: UserApi,
    @Value("\${service.api-key}") private val serviceApiKey: String
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

    fun getUsernameById(userId: UUID): String? {
        return try {
            val response = userApi.usersGetByIdPost(UserByIdRequest(userId), serviceApiKey)
            response.successResponse?.username
        } catch (e: Exception) {
            logger.warn { "Could not fetch user $userId from users service: ${e.message}" }
            null
        }
    }

    fun getFcmToken(userId: UUID): String? {
        return try {
            val response = userApi.usersInternalDeviceTokenPost(
                GetFcmTokenRequest(userId = userId.toString()),
                serviceApiKey
            )
            response.fcmToken
        } catch (e: Exception) {
            logger.warn { "Could not fetch FCM token for user $userId: ${e.message}" }
            null
        }
    }
}
