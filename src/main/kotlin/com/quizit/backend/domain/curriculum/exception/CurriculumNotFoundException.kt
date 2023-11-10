package com.quizit.backend.domain.curriculum.exception

import com.quizit.backend.global.exception.ServerException

data class CurriculumNotFoundException(
    override val message: String = "커리큘럼을 찾을 수 없습니다."
) : ServerException(code = 404, message)