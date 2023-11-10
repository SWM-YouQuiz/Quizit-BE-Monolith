package com.quizit.backend.domain.auth.model

import org.springframework.data.redis.core.RedisHash

@RedisHash
data class RefreshToken(
    val userId: String,
    val content: String
)