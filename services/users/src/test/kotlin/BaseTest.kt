
import com.fandomatch.users.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.example.UsersApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(classes = [UsersApplication::class])
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestDatabaseConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BaseTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    fun <T> performPostRequest(
        url: String,
        body: Any,
        responseType: Class<T>
    ): T =
        mockMvc.post(url) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .let { objectMapper.readValue(it.contentAsString, responseType) }

    fun performRegisterRequestAndReturn(request: UserRegistrationRequest): UserRegistrationResponse =
        performPostRequest("/auth/register", request, UserRegistrationResponse::class.java)


    fun performLoginRequestAndReturn(request: UserLoginRequest): UserLoginResponse =
        performPostRequest("/auth/login", request, UserLoginResponse::class.java)


    fun performLogoutRequestAndReturn(): LogoutResponse =
        performPostRequest("/auth/logout", Unit, LogoutResponse::class.java)

    fun performTokenRefreshRequestAndReturn(refreshToken: String): RefreshTokenResponse =
        performPostRequest("/token/refresh", RefreshToken(refreshToken), RefreshTokenResponse::class.java)

}
