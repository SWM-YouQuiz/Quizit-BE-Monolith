package com.quizit.backend.domain.auth.handler

import com.quizit.backend.domain.auth.service.AppleOAuth2Service
import com.quizit.backend.global.annotation.Handler
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Handler
class AppleOAuth2Handler(
    private val appleOAuth2Service: AppleOAuth2Service,
    private val appleOAuth2Provider: com.quizit.backend.domain.auth.AppleOAuth2Provider,
    @Value("\${uri.frontend}")
    private val frontendUri: String
) {
    fun revoke(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.FOUND)
            .location(
                URI.create(
                    "https://appleid.apple.com/auth/authorize?response_mode=form_post&response_type=code&client_id=${appleOAuth2Provider.clientId}&scope=name%20email&&redirect_uri=$frontendUri/api/auth/oauth2/redirect/apple/revoke"
                )
            ).build()

    fun loginRedirect(request: ServerRequest): Mono<ServerResponse> =
        request.formData()
            .flatMap { appleOAuth2Service.loginRedirect(it) }

    fun revokeRedirect(request: ServerRequest): Mono<ServerResponse> =
        request.formData()
            .flatMap { appleOAuth2Service.revokeRedirect(it) }
}