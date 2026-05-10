package org.example.adapter

import com.fandomatch.users.api.UserApi
import com.fandomatch.users.model.GetFcmTokenRequest
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UsersNotificationAdapter(
    private val userApi: UserApi,
    @Value("\${service.api-key}") private val serviceApiKey: String
) {

    companion object : KLogging()

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