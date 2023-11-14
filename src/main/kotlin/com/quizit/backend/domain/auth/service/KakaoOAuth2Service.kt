package com.quizit.backend.domain.auth.service

import com.quizit.backend.domain.auth.adapter.client.KakaoClient
import com.quizit.backend.domain.user.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Service
class KakaoOAuth2Service(
    private val kakaoClient: KakaoClient,
    private val userService: UserService,
    @Value("\${uri.frontend}")
    private val frontendUri: String
) {
    fun revokeRedirect(code: String): Mono<ServerResponse> =
        kakaoClient.getTokenResponseByCodeAndRedirectUri(code, "$frontendUri/api/oauth2/redirect/kakao/revoke")
            .map { it["access_token"] as String }
            .flatMap { token ->
                kakaoClient.getOAuth2UserByToken(token)
                    .cache()
                    .let {
                        Mono.`when`(it, kakaoClient.revokeByToken(token))
                            .then(it)
                    }
            }
            .flatMap { userService.deleteUserByEmailAndProvider(it.email, it.provider) }
            .then(Mono.defer {
                ServerResponse.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUri))
                    .build()
            })
}