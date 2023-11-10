package com.quizit.backend.domain.course.dto.request

data class CreateCourseRequest(
    val title: String,
    val image: String,
    val curriculumId: String
)