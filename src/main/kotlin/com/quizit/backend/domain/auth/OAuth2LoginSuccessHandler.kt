package com.quizit.backend.domain.auth

import com.quizit.backend.domain.auth.model.OAuth2UserInfo
import com.quizit.backend.domain.auth.model.RefreshToken
import com.quizit.backend.domain.auth.repository.TokenRepository
import com.quizit.backend.domain.user.dto.request.CreateUserRequest
import com.quizit.backend.domain.user.exception.UserNotFoundException
import com.quizit.backend.domain.user.service.UserService
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.jwt.JwtAuthentication
import com.quizit.backend.global.jwt.JwtProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorResume
import java.net.URI
import java.time.Duration

@Handler
class OAuth2LoginSuccessHandler(
    private val tokenRepository: TokenRepository,
    private val userService: UserService,
    private val jwtProvider: JwtProvider,
    @Value("\${uri.frontend}")
    private val frontendUri: String,
    @Value("\${jwt.refreshTokenExpire}")
    private val expire: Long,
) : ServerAuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange, authentication: Authentication
    ): Mono<Void> {
        val oAuth2User = authentication.principal as OAuth2UserInfo
        var isSignUp = false

        return userService.getUserByEmailAndProvider(oAuth2User.email, oAuth2User.provider)
            .onErrorResume(UserNotFoundException::class) {
                isSignUp = true

                userService.createUser(
                    CreateUserRequest(
                        email = oAuth2User.email,
                        username = oAuth2User.name!!,
                        image = (1..6).map { "https://quizit-storage.s3.ap-northeast-2.amazonaws.com/character$it.svg" }
                            .random(),
                        allowPush = true,
                        dailyTarget = 5,
                        provider = oAuth2User.provider
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
                    webFilterExchange.exchange.response.apply {
                        statusCode = HttpStatus.FOUND
                        headers.location =
                            URI("$frontendUri/login-redirection?isSignUp=$isSignUp&accessToken=${accessToken}")
                        cookies.set(
                            "refreshToken", ResponseCookie.from("refreshToken", refreshToken)
                                .path("/")
                                .httpOnly(true)
                                .secure(true)
                                .maxAge(Duration.ofMinutes(expire))
                                .build()
                        )
                    }.setComplete()
                )
            }
    }
}