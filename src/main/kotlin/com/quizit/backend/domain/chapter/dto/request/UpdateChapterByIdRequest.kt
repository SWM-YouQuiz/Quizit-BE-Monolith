package com.quizit.backend.domain.chapter.dto.request

data class UpdateChapterByIdRequest(
    val description: String,
    val document: String,
    val image: String,
    val index: Int
)