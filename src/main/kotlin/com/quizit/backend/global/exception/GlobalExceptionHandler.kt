package com.quizit.backend.global.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.quizit.backend.global.dto.ErrorResponse
import com.quizit.backend.global.util.getLogger
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Configuration
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {
    private val logger = getLogger()

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> =
        with(exchange.response) {
            val errorResponse = if (ex is ServerException) {
                ErrorResponse(code = ex.code, message = ex.message)
            } else {
                ErrorResponse(code = 500, message = "Internal Server Error")
            }

            logger.error { "${ex.message} at ${ex.stackTrace[0]}" }

            statusCode = HttpStatusCode.valueOf(errorResponse.code)
            headers.contentType = MediaType.APPLICATION_JSON

            writeWith(
                bufferFactory()
                    .wrap(objectMapper.writeValueAsBytes(errorResponse))
                    .toMono()
            )
        }
}