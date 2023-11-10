package com.quizit.backend.domain.chapter.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Chapter(
    @Id
    var id: String? = null,
    var description: String,
    @Indexed
    var document: String,
    var courseId: String,
    var image: String,
    var index: Int
)