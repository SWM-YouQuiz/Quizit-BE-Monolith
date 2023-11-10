package com.quizit.backend.domain.auth.router

import com.quizit.backend.domain.auth.handler.AppleOAuth2Handler
import com.quizit.backend.global.annotation.Router
import com.quizit.backend.global.util.logFilter
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Router
class AppleOAuth2Router {
    @Bean
    fun appleOAuth2Routes(handler: AppleOAuth2Handler): RouterFunction<ServerResponse> =
        router {
            "/oauth2".nest {
                GET("/revoke/apple", handler::revoke)
                POST("/redirect/apple", handler::loginRedirect)
                POST("/redirect/apple/revoke", handler::revokeRedirect)
            }
            filter(::logFilter)
        }
}