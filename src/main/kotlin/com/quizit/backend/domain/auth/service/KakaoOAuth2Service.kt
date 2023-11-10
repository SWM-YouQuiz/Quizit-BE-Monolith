package com.quizit.backend.domain.auth.service

import com.quizit.backend.domain.auth.adapter.client.KakaoClient
import com.quizit.backend.domain.user.service.UserService
import com.quizit.backend.global.util.component1
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
        kakaoClient.getTokenResponseByCodeAndRedirectUri(code, "$frontendUri/api/auth/oauth2/redirect/kakao/revoke")
            .map { it["access_token"] as String }
            .flatMap {
                Mono.zip(
                    kakaoClient.getOAuth2UserByToken(it),
                    kakaoClient.revokeByToken(it)
                )
            }
            .flatMap { (oAuth2User) -> userService.deleteUserByEmailAndProvider(oAuth2User.email, oAuth2User.provider) }
            .then(Mono.defer {
                ServerResponse.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUri))
                    .build()
            })
}