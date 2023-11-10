package com.quizit.backend.domain.curriculum.dto.response

import com.quizit.backend.domain.curriculum.model.Curriculum

data class CurriculumResponse(
    val id: String,
    val title: String,
    val image: String,
) {
    companion object {
        operator fun invoke(curriculum: Curriculum) =
            with(curriculum) {
                CurriculumResponse(
                    id = id!!,
                    title = title,
                    image = image
                )
            }
    }
}