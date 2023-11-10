package com.quizit.backend.domain.auth.handler

import com.quizit.backend.domain.auth.service.GoogleOAuth2Service
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.util.queryParamNotNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Handler
class GoogleOAuth2Handler(
    private val googleOAuth2Service: GoogleOAuth2Service,
    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    private val clientId: String,
    @Value("\${uri.frontend}")
    private val frontendUri: String
) {
    fun revoke(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.FOUND)
            .location(
                URI.create(
                    "https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&prompt=consent&response_type=code&client_id=$clientId&redirect_uri=$frontendUri/api/auth/oauth2/redirect/google/revoke&scope=profile%20email"
                )
            ).build()

    fun revokeRedirect(request: ServerRequest): Mono<ServerResponse> =
        googleOAuth2Service.revokeRedirect(request.queryParamNotNull("code"))

}