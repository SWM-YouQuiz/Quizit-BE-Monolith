package com.quizit.backend.domain.chapter.router

import com.quizit.backend.domain.chapter.handler.ChapterHandler
import com.quizit.backend.global.annotation.Router
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Router
class ChapterRouter {
    @Bean
    fun chapterRoutes(handler: ChapterHandler): RouterFunction<ServerResponse> =
        router {
            "/chapter".nest {
                GET("/{id}", handler::getChapterById)
                GET("/course/{id}", handler::getChaptersByCourseId)
                GET("/{id}/progress", handler::getProgressById)
            }
            "/admin/chapter".nest {
                POST("", handler::createChapter)
                PUT("/{id}", handler::updateChapterById)
                DELETE("/{id}", handler::deleteChapterById)
            }
        }
}