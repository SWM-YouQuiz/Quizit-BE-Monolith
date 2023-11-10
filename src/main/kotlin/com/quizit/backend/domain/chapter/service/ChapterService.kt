package com.quizit.backend.domain.chapter.service

import com.quizit.backend.domain.chapter.dto.request.CreateChapterRequest
import com.quizit.backend.domain.chapter.dto.request.UpdateChapterByIdRequest
import com.quizit.backend.domain.chapter.dto.response.ChapterResponse
import com.quizit.backend.domain.chapter.exception.ChapterNotFoundException
import com.quizit.backend.domain.chapter.model.Chapter
import com.quizit.backend.domain.chapter.repository.ChapterRepository
import com.quizit.backend.domain.quiz.dto.response.GetProgressByIdResponse
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.global.util.component1
import com.quizit.backend.global.util.component2
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ChapterService(
    private val chapterRepository: ChapterRepository,
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository
) {
    fun getChapterById(id: String): Mono<ChapterResponse> =
        chapterRepository.findById(id)
            .switchIfEmpty(Mono.error(ChapterNotFoundException()))
            .map { ChapterResponse(it) }

    fun getChaptersByCourseId(courseId: String): Flux<ChapterResponse> =
        chapterRepository.findAllByCourseIdOrderByIndex(courseId)
            .map { ChapterResponse(it) }

    fun getProgressById(id: String, userId: String): Mono<GetProgressByIdResponse> =
        quizRepository.findAllByChapterId(id)
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

    fun createChapter(request: CreateChapterRequest): Mono<ChapterResponse> =
        with(request) {
            chapterRepository.save(
                Chapter(
                    description = description,
                    document = document,
                    courseId = courseId,
                    image = image,
                    index = index
                )
            ).map { ChapterResponse(it) }
        }

    fun updateChapterById(id: String, request: UpdateChapterByIdRequest): Mono<ChapterResponse> =
        chapterRepository.findById(id)
            .switchIfEmpty(Mono.error(ChapterNotFoundException()))
            .doOnNext {
                request.apply {
                    it.description = description
                    it.document = document
                    it.image = image
                    it.index = index
                }
            }
            .flatMap { chapterRepository.save(it) }
            .map { ChapterResponse(it) }

    fun deleteChapterById(id: String): Mono<Void> =
        chapterRepository.findById(id)
            .switchIfEmpty(Mono.error(ChapterNotFoundException()))
            .flatMap { chapterRepository.deleteById(id) }
}