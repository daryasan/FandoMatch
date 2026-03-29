import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.example.CoreApplication
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [CoreApplication::class, TestTokenApiConfig::class])
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestDatabaseConfig::class])
@ActiveProfiles("test")
abstract class BaseTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        private val keyPair: KeyPair = run {
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(2048)
            keyGen.generateKeyPair()
        }
        val publicKeyBase64: String = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val privateKey: PrivateKey = keyPair.private
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