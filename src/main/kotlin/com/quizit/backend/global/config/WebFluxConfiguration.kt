package com.quizit.backend.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebFluxConfiguration(
    @Value("\${uri.frontend}")
    private val frontendUri: String
) : WebFluxConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .apply {
                allowedOriginPatterns(frontendUri, "https://appleid.apple.com")
                allowCredentials(true)
            }
    }
}