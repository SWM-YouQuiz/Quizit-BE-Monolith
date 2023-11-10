package com.quizit.backend.domain.chapter.repository

import com.quizit.backend.domain.chapter.model.Chapter
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface ChapterRepository : ReactiveMongoRepository<Chapter, String> {
    fun findAllByCourseIdOrderByIndex(courseId: String): Flux<Chapter>
}