package com.quizit.backend.domain.chapter.handler

import com.quizit.backend.domain.chapter.dto.request.CreateChapterRequest
import com.quizit.backend.domain.chapter.dto.request.UpdateChapterByIdRequest
import com.quizit.backend.domain.chapter.service.ChapterService
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.util.authentication
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Handler
class ChapterHandler(
    private val chapterService: ChapterService
) {
    fun getChapterById(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(chapterService.getChapterById(request.pathVariable("id")))

    fun getChaptersByCourseId(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(chapterService.getChaptersByCourseId(request.pathVariable("id")))

    fun getProgressById(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            authentication()
                .flatMap {
                    ServerResponse.ok()
                        .body(chapterService.getProgressById(pathVariable("id"), it.id))
                }
        }

    fun createChapter(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<CreateChapterRequest>()
            .flatMap {
                ServerResponse.ok()
                    .body(chapterService.createChapter(it))
            }

    fun updateChapterById(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            bodyToMono<UpdateChapterByIdRequest>()
                .flatMap {
                    ServerResponse.ok()
                        .body(chapterService.updateChapterById(pathVariable("id"), it))
                }
        }

    fun deleteChapterById(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(chapterService.deleteChapterById(request.pathVariable("id")))
}