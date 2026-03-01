package adapter

import com.fandomatch.users.api.TokenApi
import com.fandomatch.users.model.PublicJwtResponse
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.client.UsersAdapter
import org.example.exceptions.UsersNotRespondingException
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UsersAdapterTest {

    @MockK
    lateinit var tokenApi: TokenApi

    @InjectMockKs
    lateinit var usersAdapter: UsersAdapter

    @Test
    fun `getBase64PublicJwt returns key when response is valid`() {
        val response = PublicJwtResponse(publicKey = "ABC123")
        every { tokenApi.tokenPublicJwtGet() } returns response

        val result = usersAdapter.getBase64PublicJwt()

        assertEquals("ABC123", result)
    }

    @Test
    fun `getBase64PublicJwt throws when response has no key`() {
        every { tokenApi.tokenPublicJwtGet() } returns PublicJwtResponse("")

        assertThrows<UsersNotRespondingException> {
            usersAdapter.getBase64PublicJwt()
        }
    }

}
