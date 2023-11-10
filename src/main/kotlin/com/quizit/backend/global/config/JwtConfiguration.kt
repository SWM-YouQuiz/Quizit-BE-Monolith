package com.quizit.backend.global.config

import com.quizit.backend.global.jwt.JwtAuthenticationFilter
import com.quizit.backend.global.jwt.JwtProvider
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class JwtConfiguration {
    @Bean
    fun jwtProvider(
        @Value("\${jwt.secretKey}")
        secretKey: String,
        @Value("\${jwt.accessTokenExpire}")
        accessTokenExpire: Long,
        @Value("\${jwt.refreshTokenExpire}")
        refreshTokenExpire: Long
    ): JwtProvider = JwtProvider(
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)),
        accessTokenExpire = TimeUnit.HOURS.toMillis(accessTokenExpire),
        refreshTokenExpire = TimeUnit.HOURS.toMillis(refreshTokenExpire)
    )

    @Bean
    fun jwtAuthenticationFilter(jwtProvider: JwtProvider): JwtAuthenticationFilter =
        JwtAuthenticationFilter(jwtProvider)
}