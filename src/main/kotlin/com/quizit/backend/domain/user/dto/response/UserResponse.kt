package com.quizit.backend.domain.user.dto.response

import com.quizit.backend.domain.user.model.User
import com.quizit.backend.domain.user.model.enum.Provider
import com.quizit.backend.domain.user.model.enum.Role
import java.time.LocalDateTime

data class UserResponse(
    val id: String,
    val email: String,
    val username: String,
    val image: String,
    val level: Int,
    val role: Role,
    val allowPush: Boolean,
    val dailyTarget: Int,
    val answerRate: Double,
    val provider: Provider,
    val createdDate: LocalDateTime,
    val correctQuizIds: HashSet<String>,
    val incorrectQuizIds: HashSet<String>,
    val markedQuizIds: HashSet<String>
) {
    companion object {
        operator fun invoke(user: User): UserResponse =
            with(user) {
                UserResponse(
                    id = id!!,
                    email = email,
                    username = username,
                    image = image,
                    level = level,
                    role = role,
                    allowPush = allowPush,
                    dailyTarget = dailyTarget,
                    answerRate = answerRate,
                    provider = provider,
                    createdDate = createdDate,
                    correctQuizIds = correctQuizIds,
                    incorrectQuizIds = incorrectQuizIds,
                    markedQuizIds = markedQuizIds
                )
            }
    }
}