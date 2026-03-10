package org.example.models

import com.fandomatch.core.model.Fandom
import com.fandomatch.users.model.UserCredentials
import org.example.models.db_models.UserProfile

data class ProfileData(
    val userCredentials: UserCredentials?,
    val userProfile: UserProfile,
    val fandoms: List<Fandom>
)