package com.quizit.backend.service

import com.quizit.backend.domain.quiz.model.Quiz
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.dto.response.UserResponse
import com.quizit.backend.domain.user.exception.PermissionDeniedException
import com.quizit.backend.domain.user.exception.UserAlreadyExistException
import com.quizit.backend.domain.user.exception.UserNotFoundException
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.domain.user.service.UserService
import com.quizit.backend.fixture.*
import com.quizit.backend.util.empty
import com.quizit.backend.util.getResult
import com.quizit.backend.util.returns
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldNotBeEqual
import io.mockk.every
import io.mockk.mockk
import reactor.kotlin.test.expectError

class UserServiceTest : BehaviorSpec() {
    private val userRepository = mockk<UserRepository>()

    private val quizRepository = mockk<QuizRepository>()

    private val userService = UserService(
        userRepository = userRepository,
        quizRepository = quizRepository,
    )

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerTest

    init {
        Given("유저가 존재하는 경우") {
            val user = createUser()
                .also {
                    every { userRepository.findAll() } returns listOf(it)
                    every { userRepository.findAllOrderByCorrectQuizIdsSize() } returns listOf(it)
                    every { userRepository.findAllOrderByCorrectQuizIdsSizeInQuizIds(any()) } returns listOf(it)
                    every { userRepository.findById(any<String>()) } returns it
                    every { userRepository.findByEmail(any()) } returns it
                    every { userRepository.findByEmailAndProvider(any(), any()) } returns it
                    every { userRepository.deleteById(any<String>()) } returns empty()
                }
            createQuiz()
                .also {
                    every { quizRepository.findAll() } returns listOf(it)
                    every { quizRepository.saveAll(any<List<Quiz>>()) } returns listOf(it)
                    every { quizRepository.findAllByCourseId(any()) } returns listOf(it)
                }
            val userResponse = UserResponse(user)

            When("랭킹 조회를 시도하면") {
                val results = listOf(
                    userService.getRanking()
                        .getResult(),
                    userService.getRankingByCourseId(ID)
                        .getResult()
                )

                Then("모든 유저에 대한 랭킹이 조회된다.") {
                    results.map {
                        it.expectSubscription()
                            .expectNext(userResponse)
                            .verifyComplete()
                    }
                }
            }

            When("식별자를 통해 유저 조회를 시도하면") {
                val result = userService.getUserById(ID)
                    .getResult()

                Then("식별자에 맞는 유저가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(userResponse)
                        .verifyComplete()
                }
            }

            When("인증 객체를 통해 유저 조회를 시도하면") {
                val result = userService.getUserByAuthentication(createJwtAuthentication())
                    .getResult()

                Then("인증 객체의 식별자에 맞는 유저가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(userResponse)
                        .verifyComplete()
                }
            }

            When("이메일을 통해 유저 조회를 시도하면") {
                val result = userService.getUserByEmail(EMAIL)
                    .getResult()

                Then("이메일에 맞는 유저가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(userResponse)
                        .verifyComplete()
                }
            }


            When("이메일과 OAuth 제공자를 통해 유저 조회를 시도하면") {
                val result = userService.getUserByEmailAndProvider(EMAIL, PROVIDER)
                    .getResult()

                Then("이메일과 OAuth 제공자에 맞는 유저가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(userResponse)
                        .verifyComplete()
                }
            }

            When("프로필을 수정하면") {
                val updateUserByIdRequest = createUpdateUserByIdRequest(username = "updated_username")
                    .also {
                        every { userRepository.save(any()) } returns createUser(username = it.username)
                    }
                val result = userService.updateUserById(ID, createJwtAuthentication(), updateUserByIdRequest)
                    .getResult()

                Then("유저 정보가 변경된다.") {
                    result.expectSubscription()
                        .assertNext { it shouldNotBeEqual userResponse }
                        .verifyComplete()
                }
            }

            When("회원 탈퇴를 하면") {
                val results = listOf(
                    userService.deleteUserById(ID, createJwtAuthentication())
                        .getResult(),
                    userService.deleteUserByEmailAndProvider(EMAIL, PROVIDER)
                        .getResult()
                )

                Then("유저가 삭제된다.") {
                    results.map {
                        it.expectSubscription()
                            .verifyComplete()
                    }
                }
            }
        }

        Given("유저가 존재하지 않는 경우") {
            createUser()
                .apply {
                    every { userRepository.findById(any<String>()) } returns empty()
                    every { userRepository.findByEmail(any()) } returns empty()
                }

            When("식별자를 통해 유저 조회를 시도하면") {
                val result = userService.getUserById(ID)
                    .getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<UserNotFoundException>()
                        .verify()
                }
            }

            When("이메일을 통해 유저 조회를 시도하면") {
                val result = userService.getUserByEmail(EMAIL)
                    .getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<UserNotFoundException>()
                        .verify()
                }
            }

            When("회원 탈퇴를 시도하면") {
                val result = userService.deleteUserById(ID, createJwtAuthentication())
                    .getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<UserNotFoundException>()
                        .verify()
                }
            }
        }

        Given("해당 이메일을 가진 유저가 없는 경우") {
            val user = createUser()
                .also {
                    every { userRepository.save(any()) } returns it
                    every { userRepository.findByEmailAndProvider(any(), any()) } returns empty()
                }
            val userResponse = UserResponse(user)

            When("유저가 회원가입을 시도하면") {
                val result = userService.createUser(createCreateUserRequest())
                    .getResult()

                Then("유저가 생성된다.") {
                    result.expectSubscription()
                        .expectNext(userResponse)
                        .verifyComplete()
                }
            }
        }

        Given("해당 이메일을 가진 유저가 이미 존재하는 경우") {
            createUser()
                .also {
                    every { userRepository.findByEmailAndProvider(any(), any()) } returns it
                }

            When("유저가 회원가입을 시도하면") {
                val result = userService.createUser(createCreateUserRequest())
                    .getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<UserAlreadyExistException>()
                        .verify()
                }
            }
        }

        Given("권한이 없는 경우") {
            createUser()
                .also {
                    every { userRepository.findById(any<String>()) } returns it
                }

            When("프로필을 수정하면") {
                val result = userService.updateUserById(
                    ID, createJwtAuthentication(id = "invalid_id"), createUpdateUserByIdRequest()
                ).getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<PermissionDeniedException>()
                        .verify()
                }
            }

            When("회원 탈퇴를 시도하면") {
                val result = userService.deleteUserById(ID, createJwtAuthentication(id = "invalid_id"))
                    .getResult()

                Then("예외가 발생한다.") {
                    result.expectSubscription()
                        .expectError<PermissionDeniedException>()
                        .verify()
                }
            }
        }
    }
}