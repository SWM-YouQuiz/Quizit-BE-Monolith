package com.quizit.backend.domain.user.router

import com.quizit.backend.domain.user.handler.UserHandler
import com.quizit.backend.global.annotation.Router
import com.quizit.backend.global.util.logFilter
import com.quizit.backend.global.util.queryParams
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Router
class UserRouter {
    @Bean
    fun userRoutes(handler: UserHandler): RouterFunction<ServerResponse> =
        router {
            "/user".nest {
                GET("/ranking", handler::getRanking)
                GET("/ranking/course/{id}", handler::getRankingByCourseId)
                GET("/authentication", handler::getUserByAuthentication)
                GET("/{id}", handler::getUserById)
                GET("/email/{email}", queryParams("provider"), handler::getUserByEmailAndProvider)
                GET("/email/{email}", handler::getUserByEmail)
                POST("", handler::createUser)
                PUT("/{id}", handler::updateUserById)
                DELETE("/{id}", handler::deleteUserById)
            }
            filter(::logFilter)
        }
}