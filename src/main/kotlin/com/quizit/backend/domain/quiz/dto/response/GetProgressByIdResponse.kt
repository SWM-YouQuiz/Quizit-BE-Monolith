package com.quizit.backend.domain.quiz.dto.response

data class GetProgressByIdResponse(
    val total: Int,
    val solved: Int
)