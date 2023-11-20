package com.quizit.backend.domain.quiz.dto.response

data class CheckAnswerResponse(
    val answer: Int,
    val solution: String,
    val quiz: QuizResponse
)