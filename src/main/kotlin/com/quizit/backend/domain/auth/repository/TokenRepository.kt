package com.quizit.backend.domain.auth.repository

import com.quizit.backend.domain.auth.model.RefreshToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class TokenRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    @Value("\${jwt.refreshTokenExpire}")
    private val refreshTokenExpire: Long
) {
    fun findByUserId(userId: String): Mono<RefreshToken> =
        redisTemplate.opsForValue()
            .get(getKey(userId))
            .map {
                RefreshToken(
                    userId = userId,
                    content = it
                )
            }

    fun save(token: RefreshToken): Mono<Boolean> =
        with(token) {
            redisTemplate.opsForValue()
                .set(getKey(userId), content, Duration.ofMinutes(refreshTokenExpire))
        }

    fun deleteByUserId(userId: String): Mono<Boolean> =
        redisTemplate.opsForValue()
            .delete(getKey(userId))

    private fun getKey(id: String): String = "refreshToken:$id"
}