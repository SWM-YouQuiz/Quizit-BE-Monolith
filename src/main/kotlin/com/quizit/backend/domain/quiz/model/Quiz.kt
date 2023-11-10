package com.quizit.backend.domain.quiz.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Quiz(
    @Id
    var id: String? = null,
    var question: String,
    var answer: Int,
    var solution: String,
    var writerId: String,
    @Indexed
    var chapterId: String,
    @Indexed
    var curriculumId: String,
    @Indexed
    var courseId: String,
    var options: List<String>,
    var answerRate: Double,
    var correctCount: Long,
    var incorrectCount: Long,
    val markedUserIds: HashSet<String>,
    val likedUserIds: HashSet<String>,
    val unlikedUserIds: HashSet<String>,
    val createdDate: LocalDateTime = LocalDateTime.now()
) {
    fun correctAnswer() {
        correctCount += 1
        changeAnswerRate()
    }

    fun incorrectAnswer() {
        incorrectCount += 1
        changeAnswerRate()
    }

    fun mark(userId: String) {
        markedUserIds.add(userId)
    }

    fun unmark(userId: String) {
        markedUserIds.remove(userId)
    }

    fun like(userId: String) {
        likedUserIds.add(userId)
    }

    fun unlike(userId: String) {
        unlikedUserIds.add(userId)
    }

    private fun changeAnswerRate() {
        answerRate = (correctCount.toDouble() / (correctCount + incorrectCount).toDouble()) * 100
    }
}