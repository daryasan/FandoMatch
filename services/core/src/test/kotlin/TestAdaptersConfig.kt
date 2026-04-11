import BaseTest.Companion.publicKeyBase64
import com.fandomatch.media.MediaService
import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.model.PublicJwtResponse
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestTokenApiConfig {
    @Bean
    @Primary
    fun tokenApi(): TokenApi {
        val mock = mock<TokenApi>()
        `when`(mock.tokenPublicJwtGet()).thenReturn(PublicJwtResponse(publicKeyBase64))
        return mock
    }

    @Bean
    @Primary
    fun mediaService(): MediaService = mock(MediaService::class.java)
}
