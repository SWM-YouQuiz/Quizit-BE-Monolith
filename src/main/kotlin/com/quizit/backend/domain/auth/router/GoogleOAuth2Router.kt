package com.quizit.backend.domain.auth.router

import com.quizit.backend.domain.auth.handler.GoogleOAuth2Handler
import com.quizit.backend.global.annotation.Router
import com.quizit.backend.global.util.logFilter
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Router
class GoogleOAuth2Router {
    @Bean
    fun googleOAuth2Routes(handler: GoogleOAuth2Handler): RouterFunction<ServerResponse> =
        router {
            "/oauth2".nest {
                GET("/revoke/google", handler::revoke)
                GET("/redirect/google/revoke", handler::revokeRedirect)
            }
            filter(::logFilter)
        }
}