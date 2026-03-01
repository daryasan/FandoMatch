package config

import BaseTest
import UrlConstants.USER_PROFILE_URL
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SecurityConfigTest : BaseTest() {

    @Test
    fun `valid token should authenticate successfully`() {
        performWithValidAuth(USER_PROFILE_URL)
            .andExpect { status().isOk() }
    }

    @Test
    fun `invalid token should return 401`() {
        performGet(USER_PROFILE_URL, "invalid token")
            .andExpect { status().isUnauthorized() }
    }

    @Test
    fun `no token should return 401`() {
        performGet(USER_PROFILE_URL, null)
            .andExpect { status().isUnauthorized() }
    }
}
