package com.quizit.backend.domain.course.exception

import com.quizit.backend.global.exception.ServerException

data class CourseNotFoundException(
    override val message: String = "코스를 찾을 수 없습니다."
) : ServerException(code = 404, message)