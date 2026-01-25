package org.example.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.service.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization") ?: return filterChain.doFilter(request, response)

        if (!header.startsWith("Bearer ")) {
            return filterChain.doFilter(request, response)
        }

        val token = header.removePrefix("Bearer ").trim()

        val userDetails = jwtService.validateAndLoadUser(token)
        val auth = UsernamePasswordAuthenticationToken(userDetails, null, emptyList())

        SecurityContextHolder.getContext().authentication = auth

        filterChain.doFilter(request, response)
    }
}
