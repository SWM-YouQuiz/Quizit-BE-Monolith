package com.quizit.backend.domain.chapter.dto.response

import com.quizit.backend.domain.chapter.model.Chapter

data class ChapterResponse(
    val id: String,
    val description: String,
    val document: String,
    val courseId: String,
    val image: String,
    val index: Int
) {
    companion object {
        operator fun invoke(chapter: Chapter): ChapterResponse =
            with(chapter) {
                ChapterResponse(
                    id = id!!,
                    description = description,
                    document = document,
                    courseId = courseId,
                    image = image,
                    index = index
                )
            }
    }
}