package com.quizit.backend.domain.quiz.repository

import com.quizit.backend.domain.quiz.model.Quiz
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface QuizRepository : ReactiveMongoRepository<Quiz, String> {
    fun findAllByChapterId(chapterId: String): Flux<Quiz>

    fun findAllByWriterId(writerId: String): Flux<Quiz>

    fun findAllByIdIn(ids: List<String>): Flux<Quiz>

    fun findAllByQuestionContains(keyword: String): Flux<Quiz>

    fun findAllByCurriculumId(curriculumId: String): Flux<Quiz>
    
    fun findAllByCourseId(courseId: String): Flux<Quiz>

    @Query("{'chapterId': ?0,'answerRate': {'\$gte': ?1, '\$lte': ?2 } }")
    fun findAllByChapterIdAndAnswerRateBetween(
        chapterId: String, minAnswerRate: Double, maxAnswerRate: Double, pageable: Pageable
    ): Flux<Quiz>
}