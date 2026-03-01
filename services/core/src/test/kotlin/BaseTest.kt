import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.model.PublicJwtResponse
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.example.CoreApplication
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [CoreApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var tokenApi: TokenApi

    private lateinit var privateKey: PrivateKey

    @BeforeAll
    fun setupKeys() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()

        privateKey = keyPair.private
        val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)

        `when`(tokenApi.tokenPublicJwtGet()).thenReturn(PublicJwtResponse(publicKeyBase64))
    }

    fun performGet(url: String, token: String? = null) =
        mockMvc.get(url) {
            if (token != null) header("Authorization", "Bearer $token")
        }

    fun performWithValidAuth(
        url: String,
        uuid: String = "00000000-0000-0000-0000-000000000000",
        username: String = "username"
    ) = performGet(url, createValidJwtToken(uuid, username))

    fun createValidJwtToken(
        uuid: String = "00000000-0000-0000-0000-000000000000",
        username: String = "username"
    ): String {
        val now = Date()
        val exp = Date(now.time + 3600_000)
        return Jwts.builder()
            .subject(uuid)
            .claim("username", username)
            .issuedAt(now)
            .expiration(exp)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()
    }
}


