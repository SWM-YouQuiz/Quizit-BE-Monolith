package com.quizit.backend.domain.auth.handler

import com.quizit.backend.domain.auth.exception.InvalidTokenException
import com.quizit.backend.domain.auth.service.AuthenticationService
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.util.authentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.time.Duration

@Handler
class AuthenticationHandler(
    private val authenticationService: AuthenticationService,
    @Value("\${jwt.refreshTokenExpire}")
    private val refreshTokenExpire: Long,
) {
    fun logout(request: ServerRequest): Mono<ServerResponse> =
        request.authentication()
            .flatMap {
                ServerResponse.ok()
                    .body(authenticationService.logout(it.id))
            }

    fun refresh(request: ServerRequest): Mono<ServerResponse> =
        request.cookies()
            .getFirst("refreshToken")
            ?.value
            ?.let {
                authenticationService.refresh(it)
                    .flatMap { (response, refreshToken) ->
                        ServerResponse.ok()
                            .cookie(
                                ResponseCookie.from("refreshToken", refreshToken)
                                    .path("/")
                                    .httpOnly(true)
                                    .secure(true)
                                    .maxAge(Duration.ofMinutes(refreshTokenExpire))
                                    .build()
                            )
                            .bodyValue(response)
                    }
            } ?: Mono.error(InvalidTokenException())
}