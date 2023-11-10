package com.quizit.backend.domain.user.service

import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.dto.request.CreateUserRequest
import com.quizit.backend.domain.user.dto.request.UpdateUserByIdRequest
import com.quizit.backend.domain.user.dto.response.UserResponse
import com.quizit.backend.domain.user.exception.PermissionDeniedException
import com.quizit.backend.domain.user.exception.UserAlreadyExistException
import com.quizit.backend.domain.user.exception.UserNotFoundException
import com.quizit.backend.domain.user.model.User
import com.quizit.backend.domain.user.model.enum.Provider
import com.quizit.backend.domain.user.model.enum.Role
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.global.jwt.JwtAuthentication
import com.quizit.backend.global.util.isAdmin
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class UserService(
    private val userRepository: UserRepository,
    private val quizRepository: QuizRepository
) {
    fun getRanking(): Flux<UserResponse> =
        userRepository.findAllOrderByCorrectQuizIdsSize()
            .map { UserResponse(it) }

    fun getRankingByCourseId(courseId: String): Flux<UserResponse> =
        quizRepository.findAllByCourseId(courseId)
            .map { it.id!! }
            .collectList()
            .flatMapMany { userRepository.findAllOrderByCorrectQuizIdsSizeInQuizIds(it) }
            .map { UserResponse(it) }

    fun getUserById(id: String): Mono<UserResponse> =
        userRepository.findById(id)
            .switchIfEmpty(Mono.error(UserNotFoundException()))
            .map { UserResponse(it) }

    fun getUserByAuthentication(authentication: JwtAuthentication): Mono<UserResponse> =
        userRepository.findById(authentication.id)
            .map { UserResponse(it) }

    fun getUserByEmail(email: String): Mono<UserResponse> =
        userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(UserNotFoundException()))
            .map { UserResponse(it) }

    fun getUserByEmailAndProvider(email: String, provider: Provider): Mono<UserResponse> =
        userRepository.findByEmailAndProvider(email, provider)
            .switchIfEmpty(Mono.error(UserNotFoundException()))
            .map { UserResponse(it) }

    fun createUser(request: CreateUserRequest): Mono<UserResponse> =
        with(request) {
            userRepository.findByEmailAndProvider(email, provider)
                .flatMap { Mono.error<User>(UserAlreadyExistException()) }
                .defaultIfEmpty(
                    User(
                        email = email,
                        username = username,
                        image = image,
                        level = 1,
                        role = Role.USER,
                        allowPush = allowPush,
                        dailyTarget = dailyTarget,
                        answerRate = 0.0,
                        provider = provider,
                        correctQuizIds = hashSetOf(),
                        incorrectQuizIds = hashSetOf(),
                        markedQuizIds = hashSetOf(),
                    )
                )
                .flatMap { userRepository.save(it) }
                .map { UserResponse(it) }
        }

    fun updateUserById(
        id: String, authentication: JwtAuthentication, request: UpdateUserByIdRequest
    ): Mono<UserResponse> =
        userRepository.findById(id)
            .switchIfEmpty(Mono.error(UserNotFoundException()))
            .filter { (authentication.id == it.id) || authentication.isAdmin() }
            .switchIfEmpty(Mono.error(PermissionDeniedException()))
            .doOnNext {
                request.apply {
                    it.username = username
                    it.image = image
                    it.allowPush = allowPush
                    it.dailyTarget = dailyTarget
                }
            }
            .flatMap { userRepository.save(it) }
            .map { UserResponse(it) }

    fun deleteUserById(id: String, authentication: JwtAuthentication): Mono<Void> =
        userRepository.findById(id)
            .switchIfEmpty(Mono.error(UserNotFoundException()))
            .filter { (authentication.id == it.id) || authentication.isAdmin() }
            .switchIfEmpty(Mono.error(PermissionDeniedException()))
            .flatMap {
                Mono.`when`(
                    userRepository.deleteById(id),
                    quizRepository.findAll()
                        .map {
                            it.apply {
                                likedUserIds.remove(id)
                                unlikedUserIds.remove(id)
                                markedUserIds.remove(id)
                            }
                        }
                        .collectList()
                        .flatMapMany { quizRepository.saveAll(it) }
                )
            }

    fun deleteUserByEmailAndProvider(email: String, provider: Provider): Mono<Void> =
        userRepository.findByEmailAndProvider(email, provider)
            .switchIfEmpty(Mono.error(UserNotFoundException()))
            .flatMap { user ->
                Mono.`when`(
                    userRepository.deleteById(user.id!!),
                    quizRepository.findAll()
                        .map {
                            it.apply {
                                likedUserIds.remove(id)
                                unlikedUserIds.remove(id)
                                markedUserIds.remove(id)
                            }
                        }
                        .collectList()
                        .flatMapMany { quizRepository.saveAll(it) }
                )
            }
}