package org.example.config

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JwtParserConfig(
    private val keyInitializer: JwtKeyInitializer
) {
    @Bean
    open fun jwtParserFactory(): JwtParser = Jwts.parser().verifyWith(keyInitializer.publicKey).build()
}
