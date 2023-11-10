package com.quizit.backend.domain.user.dto.request

import com.quizit.backend.domain.user.model.enum.Provider

data class CreateUserRequest(
    val email: String,
    val username: String,
    val image: String,
    val allowPush: Boolean,
    val dailyTarget: Int,
    val provider: Provider
)