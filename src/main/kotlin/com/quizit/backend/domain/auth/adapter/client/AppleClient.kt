package com.quizit.backend.domain.auth.adapter.client

import com.quizit.backend.domain.auth.model.AppleOAuth2UserInfo
import com.quizit.backend.global.annotation.Client
import com.quizit.backend.global.util.multiValueMapOf
import io.jsonwebtoken.Jwts
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Client
class AppleClient(
    private val webClient: WebClient,
    private val appleOAuth2Provider: com.quizit.backend.domain.auth.AppleOAuth2Provider,
) {
    fun getOAuth2UserByToken(token: String): Mono<AppleOAuth2UserInfo> =
        webClient.get()
            .uri("https://appleid.apple.com/auth/keys")
            .retrieve()
            .bodyToMono<Map<String, List<Map<String, String>>>>()
            .map {
                Jwts.parserBuilder()
                    .setSigningKey(appleOAuth2Provider.createPublicKey(token, it["keys"]!!))
                    .build()
                    .parseClaimsJws(token)
                    .body
            }
            .map {
                AppleOAuth2UserInfo(
                    email = it["email"] as String,
                    name = null
                )
            }

    fun revokeByToken(token: String): Mono<Void> =
        webClient.post()
            .uri("https://appleid.apple.com/auth/revoke")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                multiValueMapOf(
                    "client_id" to appleOAuth2Provider.clientId,
                    "client_secret" to appleOAuth2Provider.createClientSecret(),
                    "token" to token,
                    "token_type" to "access_token"
                )
            )
            .retrieve()
            .bodyToMono()

    fun getTokenResponseByCodeAndRedirectUri(code: String, redirectUri: String): Mono<Map<String, Any>> =
        webClient.post()
            .uri("https://appleid.apple.com/auth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                multiValueMapOf(
                    "client_id" to appleOAuth2Provider.clientId,
                    "client_secret" to appleOAuth2Provider.createClientSecret(),
                    "code" to code,
                    "redirect_uri" to redirectUri,
                    "grant_type" to "authorization_code"
                )
            )
            .retrieve()
            .bodyToMono<Map<String, Any>>()
}