import com.fandomatch.users.model.UserRegistrationRequest
import com.fandomatch.users.model.UserRegistrationResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.example.UsersApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(
    classes = [
        UsersApplication::class
    ]
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
open class BaseTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    fun performRegisterRequestAndReturn(request: UserRegistrationRequest): UserRegistrationResponse =
        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .let { objectMapper.readValue(it.contentAsString, UserRegistrationResponse::class.java) }

}