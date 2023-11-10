package com.quizit.backend.domain.auth.router

import com.quizit.backend.domain.auth.handler.KakaoOAuth2Handler
import com.quizit.backend.global.annotation.Router
import com.quizit.backend.global.util.logFilter
import com.quizit.backend.global.util.queryParams
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Router
class KakaoOAuth2Router {
    @Bean
    fun kakaoOAuth2Routes(handler: KakaoOAuth2Handler): RouterFunction<ServerResponse> =
        router {
            "/oauth2".nest {
                GET("/revoke/kakao", handler::revoke)
                GET("/redirect/kakao/revoke", queryParams("code"), handler::revokeRedirect)
            }
            filter(::logFilter)
        }
}