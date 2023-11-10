package com.quizit.backend.domain.auth.router

import com.quizit.backend.domain.auth.handler.AuthenticationHandler
import com.quizit.backend.global.annotation.Router
import com.quizit.backend.global.util.logFilter
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Router
class AuthenticationRouter {
    @Bean
    fun authenticationRoutes(handler: AuthenticationHandler): RouterFunction<ServerResponse> =
        router {
            "/auth".nest {
                GET("/logout", handler::logout)
                POST("/refresh", handler::refresh)
            }
            filter(::logFilter)
        }
}