package com.quizit.backend.domain.quiz.handler

import com.quizit.backend.domain.quiz.dto.request.CheckAnswerRequest
import com.quizit.backend.domain.quiz.dto.request.CreateQuizRequest
import com.quizit.backend.domain.quiz.dto.request.UpdateQuizByIdRequest
import com.quizit.backend.domain.quiz.service.QuizService
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.util.authentication
import com.quizit.backend.global.util.component1
import com.quizit.backend.global.util.component2
import com.quizit.backend.global.util.queryParamNotNull
import org.springframework.data.domain.PageRequest
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Handler
class QuizHandler(
    private val quizService: QuizService
) {
    fun getQuizById(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(quizService.getQuizById(request.pathVariable("id")))

    fun getQuizzesByChapterId(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(quizService.getQuizzesByChapterId(request.pathVariable("id")))

    fun getQuizzesByChapterIdAndAnswerRateRange(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            ServerResponse.ok()
                .body(
                    quizService.getQuizzesByChapterIdAndAnswerRateRange(
                        pathVariable("id"),
                        queryParamNotNull<String>("range")
                            .split(",")
                            .map { it.toDouble() }
                            .toSet(),
                        PageRequest.of(
                            queryParamNotNull<Int>("page"),
                            queryParamNotNull<Int>("size")
                        )
                    )
                )
        }

    fun getQuizzesByCourseId(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(quizService.getQuizzesByCourseId(request.pathVariable("id")))

    fun getQuizzesByWriterId(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(quizService.getQuizzesByWriterId(request.pathVariable("id")))

    fun getQuizzesByQuestionContains(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(quizService.getQuizzesByQuestionContains(request.queryParamNotNull("question")))

    fun getMarkedQuizzes(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(quizService.getMarkedQuizzes(request.pathVariable("id")))

    fun createQuiz(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            Mono.zip(authentication(), bodyToMono<CreateQuizRequest>())
                .flatMap { (authentication, request) ->
                    ServerResponse.ok()
                        .body(quizService.createQuiz(authentication.id, request))
                }
        }

    fun updateQuizById(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            Mono.zip(authentication(), bodyToMono<UpdateQuizByIdRequest>())
                .flatMap { (authentication, request) ->
                    ServerResponse.ok()
                        .body(quizService.updateQuizById(pathVariable("id"), authentication, request))
                }
        }

    fun deleteQuizById(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            authentication()
                .flatMap {
                    ServerResponse.ok()
                        .body(quizService.deleteQuizById(pathVariable("id"), it))
                }
        }

    fun checkAnswer(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            Mono.zip(authentication(), bodyToMono<CheckAnswerRequest>())
                .flatMap { (authentication, request) ->
                    ServerResponse.ok()
                        .body(quizService.checkAnswer(pathVariable("id"), authentication.id, request))
                }
        }

    fun markQuiz(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            authentication()
                .flatMap {
                    ServerResponse.ok()
                        .body(quizService.markQuiz(pathVariable("id"), it.id))
                }
        }

    fun evaluateQuiz(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            authentication()
                .flatMap {
                    ServerResponse.ok()
                        .body(quizService.evaluateQuiz(pathVariable("id"), it.id, queryParamNotNull<Boolean>("isLike")))
                }
        }
}