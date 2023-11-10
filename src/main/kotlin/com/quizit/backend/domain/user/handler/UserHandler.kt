package com.quizit.backend.domain.user.handler

import com.quizit.backend.domain.user.dto.request.CreateUserRequest
import com.quizit.backend.domain.user.dto.request.UpdateUserByIdRequest
import com.quizit.backend.domain.user.model.enum.Provider
import com.quizit.backend.domain.user.service.UserService
import com.quizit.backend.global.annotation.Handler
import com.quizit.backend.global.util.authentication
import com.quizit.backend.global.util.component1
import com.quizit.backend.global.util.component2
import com.quizit.backend.global.util.queryParamNotNull
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Handler
class UserHandler(
    private val userService: UserService
) {
    fun getRanking(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(userService.getRanking())

    fun getRankingByCourseId(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(userService.getRankingByCourseId(request.pathVariable("id")))

    fun getUserById(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(userService.getUserById(request.pathVariable("id")))

    fun getUserByAuthentication(request: ServerRequest): Mono<ServerResponse> =
        request.authentication()
            .flatMap {
                ServerResponse.ok()
                    .body(userService.getUserByAuthentication(it))
            }

    fun getUserByEmail(request: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok()
            .body(userService.getUserByEmail(request.pathVariable("email")))

    fun getUserByEmailAndProvider(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            ServerResponse.ok()
                .body(
                    userService.getUserByEmailAndProvider(
                        pathVariable("email"), Provider.valueOf(queryParamNotNull<String>("provider").uppercase())
                    )
                )
        }

    fun createUser(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            bodyToMono<CreateUserRequest>()
                .flatMap {
                    ServerResponse.ok()
                        .body(userService.createUser(it))
                }
        }

    fun updateUserById(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            Mono.zip(authentication(), bodyToMono<UpdateUserByIdRequest>())
                .flatMap { (authentication, request) ->
                    ServerResponse.ok()
                        .body(userService.updateUserById(pathVariable("id"), authentication, request))
                }
        }

    fun deleteUserById(request: ServerRequest): Mono<ServerResponse> =
        with(request) {
            authentication()
                .flatMap {
                    ServerResponse.ok()
                        .body(userService.deleteUserById(pathVariable("id"), it))
                }
        }
}