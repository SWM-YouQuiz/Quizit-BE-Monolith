package com.quizit.backend.domain.course.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Course(
    @Id
    var id: String? = null,
    var title: String,
    var image: String,
    @Indexed
    var curriculumId: String
)