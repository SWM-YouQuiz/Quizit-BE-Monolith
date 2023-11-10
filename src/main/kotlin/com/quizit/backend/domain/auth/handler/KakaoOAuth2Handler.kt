package com.quizit.backend.domain.auth.handler

import com.quizit.backend.domain.auth.service.KakaoOAuth2Service
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.util.queryParamNotNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Handler
class KakaoOAuth2Handler(
    private val kakaoOAuth2Service: KakaoOAuth2Service,
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}")
    private val clientId: String,
    @Value("\${uri.frontend}")
    private val frontendUri: String
) {
    fun revoke(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.FOUND)
            .location(
                URI.create(
                    "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=$clientId&scope=profile_nickname%20account_email&redirect_uri=$frontendUri/api/auth/oauth2/redirect/kakao/revoke"
                )
            ).build()

    fun revokeRedirect(request: ServerRequest): Mono<ServerResponse> =
        kakaoOAuth2Service.revokeRedirect(request.queryParamNotNull("code"))
}