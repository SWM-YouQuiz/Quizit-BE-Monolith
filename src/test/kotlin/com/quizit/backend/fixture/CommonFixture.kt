package com.quizit.backend.fixture

import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

const val ID = "test"
val CREATED_DATE = LocalDateTime.now()
val PAGEABLE = PageRequest.of(0, 3)