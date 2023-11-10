package com.quizit.backend.fixture

import com.quizit.backend.domain.auth.model.RefreshToken
import com.quizit.backend.domain.user.model.enum.Role
import com.quizit.backend.global.config.JwtConfiguration
import com.quizit.backend.global.jwt.JwtAuthentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

val SECRET_KEY = (1..100).map { ('a'..'z').random() }.joinToString("")
const val ACCESS_TOKEN_EXPIRE = 5L
const val REFRESH_TOKEN_EXPIRE = 10L
val AUTHORITIES = listOf(SimpleGrantedAuthority(Role.USER.name))
val jwtProvider = JwtConfiguration()
    .jwtProvider(SECRET_KEY, ACCESS_TOKEN_EXPIRE, REFRESH_TOKEN_EXPIRE)
val ACCESS_TOKEN = jwtProvider.createAccessToken(createJwtAuthentication())
val REFRESH_TOKEN = jwtProvider.createRefreshToken(createJwtAuthentication())

fun createJwtAuthentication(
    id: String = ID,
    authorities: List<GrantedAuthority> = AUTHORITIES,
): JwtAuthentication =
    JwtAuthentication(
        id = id,
        authorities = authorities,
    )

fun createToken(
    userId: String = ID,
    content: String = REFRESH_TOKEN
): RefreshToken = RefreshToken(
    userId = userId,
    content = content
)