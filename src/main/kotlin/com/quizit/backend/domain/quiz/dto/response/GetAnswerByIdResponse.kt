package com.quizit.backend.domain.quiz.dto.response

import com.quizit.backend.domain.quiz.model.Quiz

data class GetAnswerByIdResponse(
    val answer: Int
) {
    companion object {
        operator fun invoke(quiz: Quiz): GetAnswerByIdResponse =
            GetAnswerByIdResponse(quiz.answer)
    }
}