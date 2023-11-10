package com.quizit.backend.global.jwt

import com.quizit.backend.global.util.getLogger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import javax.crypto.SecretKey

class JwtProvider(
    private val secretKey: SecretKey,
    private val accessTokenExpire: Long,
    private val refreshTokenExpire: Long
) {
    private val logger = getLogger()

    fun createAccessToken(authentication: JwtAuthentication): String =
        createToken(createClaims(authentication), accessTokenExpire)

    fun createRefreshToken(authentication: JwtAuthentication): String =
        createToken(createClaims(authentication), refreshTokenExpire)

    fun getAuthentication(token: String): JwtAuthentication =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .run {
                JwtAuthentication(
                    id = get("id") as String,
                    authorities = (get("authorities") as String)
                        .split(",")
                        .map(::SimpleGrantedAuthority)
                )
            }

    fun validate(token: String): Boolean =
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)

            true
        } catch (ex: JwtException) {
            logger.error { ex }

            false
        }

    private fun createToken(claims: Claims, expire: Long): String =
        Date().let {
            Jwts.builder()
                .setIssuedAt(it)
                .setExpiration(Date(it.time + expire))
                .setClaims(claims)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact()
        }

    private fun createClaims(authentication: JwtAuthentication): Claims =
        with(authentication) {
            Jwts.claims(
                mapOf(
                    "id" to id,
                    "authorities" to authorities.joinToString(",") { it.authority }
                )
            )
        }
}