package com.quizit.backend.domain.course.repository

import com.quizit.backend.domain.course.model.Course
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CourseRepository : ReactiveMongoRepository<Course, String> {
    fun findAllByCurriculumId(curriculumId: String): Flux<Course>
}