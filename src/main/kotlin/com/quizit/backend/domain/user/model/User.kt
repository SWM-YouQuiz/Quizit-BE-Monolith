package com.quizit.backend.domain.user.model

import com.quizit.backend.domain.user.model.enum.Provider
import com.quizit.backend.domain.user.model.enum.Role
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class User(
    @Id
    var id: String? = null,
    val email: String,
    var username: String,
    var image: String,
    var level: Int,
    val role: Role,
    var allowPush: Boolean,
    var dailyTarget: Int,
    var answerRate: Double,
    val provider: Provider,
    val correctQuizIds: HashSet<String>,
    val incorrectQuizIds: HashSet<String>,
    val markedQuizIds: HashSet<String>,
    val createdDate: LocalDateTime = LocalDateTime.now()
) {
    fun correctAnswer(quizId: String) {
        incorrectQuizIds.remove(quizId)
        correctQuizIds.add(quizId)
        changeAnswerRate()
    }

    fun incorrectAnswer(quizId: String) {
        correctQuizIds.remove(quizId)
        incorrectQuizIds.add(quizId)
        changeAnswerRate()
    }

    fun markQuiz(quizId: String) {
        markedQuizIds.add(quizId)
    }

    fun unmarkQuiz(quizId: String) {
        markedQuizIds.remove(quizId)
    }

    fun checkLevel() {
        if (correctQuizIds.size >= level * 5) {
            level += 1
        }
    }

    private fun changeAnswerRate() {
        answerRate = (correctQuizIds.size.toDouble() / (correctQuizIds.size + incorrectQuizIds.size).toDouble()) * 100
    }
}