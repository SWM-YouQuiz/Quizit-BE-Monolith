package com.quizit.backend.domain.curriculum.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Curriculum(
    @Id
    var id: String? = null,
    var title: String,
    var image: String
)