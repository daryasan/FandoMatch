
import io.jsonwebtoken.JwtParser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.models.UserTokenData
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JwtAuthFilterConfig(
    private val jwtParser: JwtParser
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        logger.info { "Start authorizing user" }
        val header = request.getHeader("Authorization")

        if (header.isNullOrBlank() || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = header.removePrefix("Bearer ").trim()

        try {
            val auth = UsernamePasswordAuthenticationToken(
                parseToken(token),
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )

            SecurityContextHolder.getContext().authentication = auth
        } catch (e: Exception) {
            logger.warn { "Invalid JWT: ${e.message}" }
        }

        filterChain.doFilter(request, response)
    }

    private fun parseToken(unparsedToken: String): UserTokenData {
        val claims = jwtParser
            .parseSignedClaims(unparsedToken)
            .payload

        val userId = UUID.fromString(claims.subject)
        val username = claims["username"] as String

        return UserTokenData(
            userId = userId,
            username = username
        )
    }
}


