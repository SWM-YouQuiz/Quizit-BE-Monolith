package com.quizit.backend.domain.curriculum.service

import com.quizit.backend.domain.curriculum.dto.request.CreateCurriculumRequest
import com.quizit.backend.domain.curriculum.dto.request.UpdateCurriculumByIdRequest
import com.quizit.backend.domain.curriculum.dto.response.CurriculumResponse
import com.quizit.backend.domain.curriculum.exception.CurriculumNotFoundException
import com.quizit.backend.domain.curriculum.model.Curriculum
import com.quizit.backend.domain.curriculum.repository.CurriculumRepository
import com.quizit.backend.domain.quiz.dto.response.GetProgressByIdResponse
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.global.util.component1
import com.quizit.backend.global.util.component2
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CurriculumService(
    private val curriculumRepository: CurriculumRepository,
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository
) {
    fun getCurriculumById(id: String): Mono<CurriculumResponse> =
        curriculumRepository.findById(id)
            .switchIfEmpty(Mono.error(CurriculumNotFoundException()))
            .map { CurriculumResponse(it) }

    fun getCurriculums(): Flux<CurriculumResponse> =
        curriculumRepository.findAll()
            .map { CurriculumResponse(it) }

    fun getProgressById(id: String, userId: String): Mono<GetProgressByIdResponse> =
        quizRepository.findAllByCurriculumId(id)
            .switchIfEmpty(Mono.error(CurriculumNotFoundException()))
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

    fun createCurriculum(request: CreateCurriculumRequest): Mono<CurriculumResponse> =
        with(request) {
            curriculumRepository.save(
                Curriculum(
                    title = title,
                    image = image
                )
            ).map { CurriculumResponse(it) }
        }

    fun updateCurriculumById(id: String, request: UpdateCurriculumByIdRequest): Mono<CurriculumResponse> =
        curriculumRepository.findById(id)
            .switchIfEmpty(Mono.error(CurriculumNotFoundException()))
            .doOnNext {
                request.apply {
                    it.title = title
                    it.image = image
                }
            }
            .flatMap { curriculumRepository.save(it) }
            .map { CurriculumResponse(it) }

    fun deleteCurriculumById(id: String): Mono<Void> =
        curriculumRepository.findById(id)
            .switchIfEmpty(Mono.error(CurriculumNotFoundException()))
            .flatMap { curriculumRepository.deleteById(id) }
}