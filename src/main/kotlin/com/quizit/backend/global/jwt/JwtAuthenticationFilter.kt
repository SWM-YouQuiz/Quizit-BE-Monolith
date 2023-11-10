package com.quizit.backend.global.jwt

import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?.run {
                if (startsWith("Bearer ") and jwtProvider.validate(substring(7))) {
                    val authentication = jwtProvider.getAuthentication(substring(7))

                    return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                }
            }

        return chain.filter(exchange)
    }
}