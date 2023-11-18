package com.quizit.backend.global.config

import com.quizit.backend.global.util.getLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration {
    private val logger = getLogger()

    @Bean
    fun webClient(): WebClient =
        WebClient.builder()
            .filter { request, next ->
                logger.info { "HTTP ${request.method()} ${request.url().path}" }

                next.exchange(request)
                    .doOnNext { logger.info { "HTTP ${it.statusCode()}" } }
            }
            .build()
}