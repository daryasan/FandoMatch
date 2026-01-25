package org.example.service

import com.fandomatch.core.model.EditUserProfileRequest
import com.fandomatch.core.model.EditUserProfileResponse
import com.fandomatch.core.model.FullUserProfileResponse
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class UsersService {

    // мок‑профиль пользователя
    private var profile = FullUserProfileResponse(
        username = "darya",
        email = "darya@example.com",
        phone = "+79998887766",
        status = "ACTIVE",
        createdAt = OffsetDateTime.now().minusDays(10),
        bio = "Люблю котиков и чистый код",
    )

    fun getFullProfile(): FullUserProfileResponse {
        return profile
    }

    fun updateProfile(req: EditUserProfileRequest): EditUserProfileResponse {
        profile = profile.copy(
            bio = req.bio ?: profile.bio,
            avatarUrl = req.avatarUrl ?: profile.avatarUrl
        )

        return EditUserProfileResponse(
            status = EditUserProfileResponse.Status.SUCCESS,
            updatedProfile = profile
        )
    }
}
