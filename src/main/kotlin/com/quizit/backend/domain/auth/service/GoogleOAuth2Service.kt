package com.quizit.backend.domain.auth.service

import com.quizit.backend.domain.auth.adapter.client.GoogleClient
import com.quizit.backend.domain.user.service.UserService
import com.quizit.backend.global.util.component1
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI

@Service
class GoogleOAuth2Service(
    private val googleClient: GoogleClient,
    private val userService: UserService,
    @Value("\${uri.frontend}")
    private val frontendUri: String
) {
    fun revokeRedirect(code: String): Mono<ServerResponse> =
        googleClient.getTokenResponseByCodeAndRedirectUri(code, "$frontendUri/api/oauth2/redirect/google/revoke")
            .map { it["access_token"] as String }
            .flatMap {
                Mono.zip(
                    googleClient.getOAuth2UserByToken(it),
                    googleClient.revokeByToken(it)
                )
            }
            .flatMap { (oAuth2User) -> userService.deleteUserByEmailAndProvider(oAuth2User.email, oAuth2User.provider) }
            .then(Mono.defer {
                ServerResponse.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUri))
                    .build()
            })
}