package com.quizit.backend.global.annotation

import org.springframework.context.annotation.Configuration

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Configuration
annotation class Router