package com.quizit.backend.domain.quiz.dto.response

import com.quizit.backend.domain.quiz.model.Quiz
import java.time.LocalDateTime

data class QuizResponse(
    val id: String,
    val question: String,
    val writerId: String,
    val chapterId: String,
    val answerRate: Double,
    val options: List<String>,
    val correctCount: Long,
    val incorrectCount: Long,
    val markedUserIds: HashSet<String>,
    val likedUserIds: HashSet<String>,
    val unlikedUserIds: HashSet<String>,
    val createdDate: LocalDateTime,
) {
    companion object {
        operator fun invoke(quiz: Quiz) =
            with(quiz) {
                QuizResponse(
                    id = id!!,
                    question = question,
                    writerId = writerId,
                    chapterId = chapterId,
                    options = options,
                    answerRate = answerRate,
                    correctCount = correctCount,
                    incorrectCount = incorrectCount,
                    markedUserIds = markedUserIds,
                    likedUserIds = likedUserIds,
                    unlikedUserIds = unlikedUserIds,
                    createdDate = createdDate
                )
            }
    }
}