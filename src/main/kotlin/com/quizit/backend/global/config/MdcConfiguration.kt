package com.quizit.backend.global.config

import com.quizit.backend.global.mdc.MdcContextLifter
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators

@Configuration
class MdcConfiguration {
    private val mdcContextKey = MdcConfiguration::class.java.name

    @PostConstruct
    fun contextOperatorHook() {
        Hooks.onEachOperator(mdcContextKey, Operators.lift { _, subscriber -> MdcContextLifter(subscriber) })
    }

    @PreDestroy
    fun cleanupHook() {
        Hooks.resetOnEachOperator(mdcContextKey)
    }
}