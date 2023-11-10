package com.quizit.backend.domain.course.service

import com.quizit.backend.domain.course.dto.request.CreateCourseRequest
import com.quizit.backend.domain.course.dto.request.UpdateCourseByIdRequest
import com.quizit.backend.domain.course.dto.response.CourseResponse
import com.quizit.backend.domain.course.exception.CourseNotFoundException
import com.quizit.backend.domain.course.model.Course
import com.quizit.backend.domain.course.repository.CourseRepository
import com.quizit.backend.domain.quiz.dto.response.GetProgressByIdResponse
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.global.util.component1
import com.quizit.backend.global.util.component2
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository
) {
    fun getCourseById(id: String): Mono<CourseResponse> =
        courseRepository.findById(id)
            .switchIfEmpty(Mono.error(CourseNotFoundException()))
            .map { CourseResponse(it) }

    fun getCoursesByCurriculumId(chapterId: String): Flux<CourseResponse> =
        courseRepository.findAllByCurriculumId(chapterId)
            .map { CourseResponse(it) }

    fun getProgressById(id: String, userId: String): Mono<GetProgressByIdResponse> =
        quizRepository.findAllByCourseId(id)
            .map { it.id!! }
            .collectList()
            .zipWith(userRepository.findById(userId))
            .map { (quizIds, user) ->
                Pair(
                    (user.correctQuizIds + user.incorrectQuizIds).count { it in quizIds }, quizIds.count()
                )
            }
            .map { (solved, total) ->
                GetProgressByIdResponse(
                    total = total,
                    solved = solved
                )
            }

    fun createCourse(request: CreateCourseRequest): Mono<CourseResponse> =
        with(request) {
            courseRepository.save(
                Course(
                    title = title,
                    image = image,
                    curriculumId = curriculumId
                )
            ).map { CourseResponse(it) }
        }

    fun updateCourseById(id: String, request: UpdateCourseByIdRequest): Mono<CourseResponse> =
        courseRepository.findById(id)
            .switchIfEmpty(Mono.error(CourseNotFoundException()))
            .doOnNext {
                request.apply {
                    it.title = title
                    it.image = image
                }
            }
            .flatMap { courseRepository.save(it) }
            .map { CourseResponse(it) }

    fun deleteCourseById(id: String): Mono<Void> =
        courseRepository.findById(id)
            .switchIfEmpty(Mono.error(CourseNotFoundException()))
            .flatMap { courseRepository.deleteById(id) }
}