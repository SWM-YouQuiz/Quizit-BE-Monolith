package com.quizit.backend.domain.quiz.service

import com.quizit.backend.domain.chapter.repository.ChapterRepository
import com.quizit.backend.domain.course.repository.CourseRepository
import com.quizit.backend.domain.quiz.dto.request.CheckAnswerRequest
import com.quizit.backend.domain.quiz.dto.request.CreateQuizRequest
import com.quizit.backend.domain.quiz.dto.request.UpdateQuizByIdRequest
import com.quizit.backend.domain.quiz.dto.response.CheckAnswerResponse
import com.quizit.backend.domain.quiz.dto.response.GetAnswerByIdResponse
import com.quizit.backend.domain.quiz.dto.response.QuizResponse
import com.quizit.backend.domain.quiz.exception.QuizNotFoundException
import com.quizit.backend.domain.quiz.model.Quiz
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.exception.PermissionDeniedException
import com.quizit.backend.domain.user.exception.UserNotFoundException
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.global.jwt.JwtAuthentication
import com.quizit.backend.global.util.component1
import com.quizit.backend.global.util.component2
import com.quizit.backend.global.util.isAdmin
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val chapterRepository: ChapterRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
) {
    fun getQuizById(id: String): Mono<QuizResponse> =
        quizRepository.findById(id)
            .switchIfEmpty(Mono.error(QuizNotFoundException()))
            .map { QuizResponse(it) }

    fun getAnswerById(id: String): Mono<GetAnswerByIdResponse> =
        quizRepository.findById(id)
            .switchIfEmpty(Mono.error(QuizNotFoundException()))
            .map { GetAnswerByIdResponse(it) }

    fun getQuizzesByChapterId(chapterId: String): Flux<QuizResponse> =
        quizRepository.findAllByChapterId(chapterId)
            .map { QuizResponse(it) }

    fun getQuizzesByChapterIdAndAnswerRateRange(
        chapterId: String, answerRateRange: Set<Double>, pageable: Pageable
    ): Flux<QuizResponse> =
        with(answerRateRange) {
            quizRepository.findAllByChapterIdAndAnswerRateBetween(chapterId, min(), max(), pageable)
                .map { QuizResponse(it) }
        }

    fun getQuizzesByCourseId(courseId: String): Flux<QuizResponse> =
        quizRepository.findAllByCourseId(courseId)
            .map { QuizResponse(it) }

    fun getQuizzesByWriterId(writerId: String): Flux<QuizResponse> =
        quizRepository.findAllByWriterId(writerId)
            .map { QuizResponse(it) }

    fun getQuizzesByQuestionContains(keyword: String): Flux<QuizResponse> =
        quizRepository.findAllByQuestionContains(keyword)
            .map { QuizResponse(it) }

    fun getMarkedQuizzes(userId: String): Flux<QuizResponse> =
        userRepository.findById(userId)
            .flatMapMany { quizRepository.findAllByIdIn(it.markedQuizIds.toList()) }
            .map { QuizResponse(it) }

    fun createQuiz(userId: String, request: CreateQuizRequest): Mono<QuizResponse> =
        with(request) {
            chapterRepository.findById(chapterId)
                .flatMap { courseRepository.findById(it.courseId) }
                .flatMap {
                    quizRepository.save(
                        Quiz(
                            question = question,
                            answer = answer,
                            solution = solution,
                            writerId = userId,
                            chapterId = chapterId,
                            courseId = it.id!!,
                            curriculumId = it.curriculumId,
                            options = options,
                            answerRate = 0.0,
                            correctCount = 0,
                            incorrectCount = 0,
                            markedUserIds = hashSetOf(),
                            likedUserIds = hashSetOf(),
                            unlikedUserIds = hashSetOf()
                        )
                    )
                }
                .map { QuizResponse(it) }
        }

    fun updateQuizById(
        id: String, authentication: JwtAuthentication, request: UpdateQuizByIdRequest
    ): Mono<QuizResponse> =
        quizRepository.findById(id)
            .switchIfEmpty(Mono.error(QuizNotFoundException()))
            .filter { (authentication.id == it.writerId) || authentication.isAdmin() }
            .switchIfEmpty(Mono.error(PermissionDeniedException()))
            .doOnNext {
                request.apply {
                    it.question = question
                    it.answer = answer
                    it.solution = solution
                    it.options = options
                }
            }
            .flatMap { quizRepository.save(it) }
            .map { QuizResponse(it) }

    fun deleteQuizById(id: String, authentication: JwtAuthentication): Mono<Void> =
        quizRepository.findById(id)
            .switchIfEmpty(Mono.error(QuizNotFoundException()))
            .filter { (authentication.id == it.writerId) || authentication.isAdmin() }
            .switchIfEmpty(Mono.error(PermissionDeniedException()))
            .flatMap {
                Mono.`when`(
                    quizRepository.deleteById(id),
                    userRepository.findAll()
                        .doOnNext {
                            it.apply {
                                correctQuizIds.remove(id)
                                incorrectQuizIds.remove(id)
                                markedQuizIds.remove(id)
                            }
                        }
                        .collectList()
                        .flatMapMany { userRepository.saveAll(it) }
                )
            }

    fun checkAnswer(id: String, userId: String, request: CheckAnswerRequest): Mono<CheckAnswerResponse> =
        quizRepository.findById(id)
            .switchIfEmpty(Mono.error(QuizNotFoundException()))
            .cache()
            .run {
                Mono.zip(
                    this,
                    userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(UserNotFoundException()))
                ).doOnNext { (quiz, user) ->
                    if (request.answer == quiz.answer) {
                        quiz.correctAnswer()
                        user.correctAnswer(id)
                    } else {
                        quiz.incorrectAnswer()
                        user.incorrectAnswer(id)
                    }
                }.flatMap { (quiz, user) -> Mono.zip(quizRepository.save(quiz), userRepository.save(user)) }
                    .then(map {
                        CheckAnswerResponse(
                            answer = it.answer,
                            solution = it.solution
                        )
                    })
            }

    fun markQuiz(id: String, userId: String): Mono<QuizResponse> =
        Mono.zip(
            quizRepository.findById(id)
                .switchIfEmpty(Mono.error(QuizNotFoundException())),
            userRepository.findById(userId)
                .switchIfEmpty(Mono.error(UserNotFoundException()))
        ).doOnNext { (quiz, user) ->
            if (userId in quiz.markedUserIds) {
                quiz.unmark(userId)
                user.unmarkQuiz(id)
            } else {
                quiz.mark(userId)
                user.markQuiz(id)
            }
        }.flatMap { (quiz, user) -> Mono.zip(quizRepository.save(quiz), userRepository.save(user)) }
            .map { (quiz) -> QuizResponse(quiz) }

    fun evaluateQuiz(id: String, userId: String, isLike: Boolean): Mono<QuizResponse> =
        quizRepository.findById(id)
            .switchIfEmpty(Mono.error(QuizNotFoundException()))
            .doOnNext {
                if (isLike) {
                    if (userId in it.likedUserIds) {
                        it.likedUserIds.remove(userId)
                    } else {
                        it.unlikedUserIds.remove(userId)
                        it.like(userId)
                    }
                } else {
                    if (userId in it.unlikedUserIds) {
                        it.unlikedUserIds.remove(userId)
                    } else {
                        it.likedUserIds.remove(userId)
                        it.unlike(userId)
                    }
                }
            }
            .flatMap { quizRepository.save(it) }
            .map { QuizResponse(it) }

}