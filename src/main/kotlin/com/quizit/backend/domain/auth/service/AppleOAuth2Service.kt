package com.quizit.backend.domain.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.quizit.backend.domain.auth.adapter.client.AppleClient
import com.quizit.backend.domain.auth.model.AppleOAuth2UserInfo
import com.quizit.backend.domain.auth.model.OAuth2UserInfo
import com.quizit.backend.domain.auth.model.RefreshToken
import com.quizit.backend.domain.auth.repository.TokenRepository
import com.quizit.backend.domain.user.dto.request.CreateUserRequest
import com.quizit.backend.domain.user.exception.UserNotFoundException
import com.quizit.backend.domain.user.model.enum.Provider
import com.quizit.backend.domain.user.service.UserService
import com.quizit.backend.global.jwt.JwtAuthentication
import com.quizit.backend.global.jwt.JwtProvider
import com.quizit.backend.global.util.component1
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorResume
import java.net.URI
import java.time.Duration

@Service
class AppleOAuth2Service(
    private val appleClient: AppleClient,
    private val tokenRepository: TokenRepository,
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val jwtProvider: JwtProvider,
    @Value("\${uri.frontend}")
    private val frontendUri: String,
    @Value("\${jwt.refreshTokenExpire}")
    private val refreshTokenExpire: Long,
) {
    fun loginRedirect(loginResponse: MultiValueMap<String, String>): Mono<ServerResponse> =
        Mono.justOrEmpty(loginResponse["user"]?.firstOrNull())
            .map {
                val user = objectMapper.readValue<Map<String, Any>>(it!!)
                val name = (user["name"] as Map<String, String>).run { get("lastName") + get("firstName") }
                val email = user["email"] as String

                AppleOAuth2UserInfo(
                    email = email,
                    name = name
                )
            }
            .switchIfEmpty(
                Mono.defer {
                    appleClient.getTokenResponseByCodeAndRedirectUri(
                        loginResponse["code"]!!.first(), "$frontendUri/api/auth/oauth2/redirect/apple"
                    ).flatMap { appleClient.getOAuth2UserByToken(it["id_token"] as String) }
                }
            )
            .flatMap { it.onAuthenticationSuccess() }

    fun revokeRedirect(loginResponse: MultiValueMap<String, String>): Mono<ServerResponse> =
        appleClient.getTokenResponseByCodeAndRedirectUri(
            loginResponse["code"]!!.first(), "$frontendUri/api/auth/oauth2/redirect/apple/revoke"
        ).flatMap {
            Mono.zip(
                appleClient.getOAuth2UserByToken(it["id_token"] as String),
                appleClient.revokeByToken(it["access_token"] as String)
            )
        }.flatMap { (oAuth2UserInfo) ->
            userService.deleteUserByEmailAndProvider(
                oAuth2UserInfo.email, oAuth2UserInfo.provider
            )
        }.then(
            ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create(frontendUri))
                .build()
        )

    private fun OAuth2UserInfo.onAuthenticationSuccess(): Mono<ServerResponse> {
        var isSignUp = false

        return userService.getUserByEmailAndProvider(email, provider)
            .onErrorResume(UserNotFoundException::class) {
                isSignUp = true

                userService.createUser(
                    CreateUserRequest(
                        email = email,
                        username = name!!,
                        image = (1..6).map { "https://quizit-storage.s3.ap-northeast-2.amazonaws.com/character$it.svg" }
                            .random(),
                        allowPush = true,
                        dailyTarget = 5,
                        provider = Provider.APPLE
                    )
                )
            }
            .flatMap {
                val jwtAuthentication = JwtAuthentication(
                    id = it.id,
                    authorities = listOf(SimpleGrantedAuthority(it.role.name))
                )
                val accessToken = jwtProvider.createAccessToken(jwtAuthentication)
                val refreshToken = jwtProvider.createRefreshToken(jwtAuthentication)

                tokenRepository.save(
                    RefreshToken(
                        userId = it.id,
                        content = refreshToken
                    )
                ).then(
                    ServerResponse.status(HttpStatus.FOUND)
                        .location(
                            URI("$frontendUri/login-redirection?isSignUp=$isSignUp&accessToken=${accessToken}")
                        )
                        .cookie(
                            ResponseCookie.from("refreshToken", refreshToken)
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(Duration.ofMinutes(refreshTokenExpire))
                                .build()
                        )
                        .build()
                )
            }
    }
}