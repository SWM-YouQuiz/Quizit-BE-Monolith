package com.quizit.backend.domain.chapter.exception

import com.quizit.backend.global.exception.ServerException

data class ChapterNotFoundException(
    override val message: String = "챕터를 찾을 수 없습니다."
) : ServerException(code = 404, message)