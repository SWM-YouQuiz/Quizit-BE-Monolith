package com.quizit.backend.service

import com.quizit.backend.domain.auth.exception.InvalidTokenException
import com.quizit.backend.domain.auth.exception.TokenNotFoundException
import com.quizit.backend.domain.auth.repository.TokenRepository
import com.quizit.backend.domain.auth.service.AuthenticationService
import com.quizit.backend.fixture.ID
import com.quizit.backend.fixture.REFRESH_TOKEN
import com.quizit.backend.fixture.createToken
import com.quizit.backend.fixture.jwtProvider
import com.quizit.backend.util.empty
import com.quizit.backend.util.getResult
import com.quizit.backend.util.returns
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.kotlin.test.expectError

class AuthenticationServiceTest : BehaviorSpec() {
    private val tokenRepository = mockk<TokenRepository>()

    private val authenticationService =
        AuthenticationService(
            tokenRepository = tokenRepository,
            jwtProvider = jwtProvider,
        )

    init {
        Given("유저가 로그인 상태인 경우") {
            every { tokenRepository.findByUserId(any()) } returns createToken()
            every { tokenRepository.save(any()) } returns true
            every { tokenRepository.deleteByUserId(any()) } returns true

            When("로그아웃을 시도하면") {
                val result = authenticationService.logout(ID)
                    .getResult()

                Then("해당 유저의 리프레쉬 토큰이 삭제된다.") {
                    result.expectSubscription()
                        .verifyComplete()
                }
            }

            When("유효한 리프레쉬 토큰으로 로그인 유지를 시도하면") {
                val result = authenticationService.refresh(REFRESH_TOKEN)
                    .getResult()

                Then("해당 유저에 대한 액세스 토큰과 리프레쉬 토큰이 발급된다.") {
                    result.expectSubscription()
                        .assertNext { (response, refreshToken) ->
                            jwtProvider.getAuthentication(response.accessToken).id shouldBe ID
                            jwtProvider.getAuthentication(refreshToken).id shouldBe ID
                        }
                        .verifyComplete()
                }
            }

            When("유효하지 않은 리프레쉬 토큰으로 로그인 유지를 시도하면") {
                Then("예외가 발생한다.") {
                    shouldThrow<InvalidTokenException> {
                        authenticationService.refresh("invalid_token")
                            .subscribe()
                    }
                }
            }
        }

        Given("로그인 상태가 아닌 경우") {
            every { tokenRepository.findByUserId(any()) } returns empty()

            When("리프레쉬 토큰으로 로그인 유지를 시도하면") {
                val result = authenticationService.refresh(REFRESH_TOKEN)
                    .getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<TokenNotFoundException>()
                        .verify()
                }
            }
        }
    }
}