package utils

import java.util.*

object Constants {
    val USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val TARGET_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    val CANDIDATE_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000003")
    const val USERNAME = "testuser"
    const val TARGET_USERNAME = "targetuser"
    const val NAME = "Test User"
    const val TARGET_NAME = "Target User"
    const val EMAIL = "test@example.com"
    const val BIO = "Test bio"
    const val AVATAR_URL = "https://example.com/avatar.png"
    const val BACKGROUND_URL = "https://example.com/bg.png"
    const val BIRTH_DATE_EPOCH = 946684800L // 2000-01-01
}
