package com.quizit.backend.domain.auth.service

import com.quizit.backend.domain.auth.dto.response.RefreshResponse
import com.quizit.backend.domain.auth.exception.InvalidAccessException
import com.quizit.backend.domain.auth.exception.InvalidTokenException
import com.quizit.backend.domain.auth.exception.TokenNotFoundException
import com.quizit.backend.domain.auth.model.RefreshToken
import com.quizit.backend.domain.auth.repository.TokenRepository
import com.quizit.backend.global.jwt.JwtProvider
import io.jsonwebtoken.JwtException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthenticationService(
    private val tokenRepository: TokenRepository,
    private val jwtProvider: JwtProvider,
) {
    fun logout(userId: String): Mono<Void> =
        tokenRepository.deleteByUserId(userId)
            .then()

    fun refresh(token: String): Mono<Pair<RefreshResponse, String>> =
        with(
            try {
                jwtProvider.getAuthentication(token)
            } catch (ex: JwtException) {
                throw InvalidTokenException()
            }
        ) {
            tokenRepository.findByUserId(id)
                .switchIfEmpty(Mono.error(TokenNotFoundException()))
                .filter { token == it.content }
                .switchIfEmpty(
                    Mono.defer {
                        tokenRepository.deleteByUserId(id)
                            .then(Mono.error(InvalidAccessException()))
                    }
                )
                .flatMap {
                    val accessToken = jwtProvider.createAccessToken(this)
                    val refreshToken = jwtProvider.createRefreshToken(this)

                    tokenRepository.save(
                        RefreshToken(
                            userId = id,
                            content = refreshToken
                        )
                    ).thenReturn(
                        Pair(
                            RefreshResponse(accessToken = accessToken),
                            refreshToken
                        )
                    )
                }
        }
}