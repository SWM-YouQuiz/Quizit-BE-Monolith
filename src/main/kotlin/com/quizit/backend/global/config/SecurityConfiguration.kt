package com.quizit.backend.global.config

import com.quizit.backend.domain.auth.OAuth2LoginSuccessHandler
import com.quizit.backend.domain.user.model.enum.Role
import com.quizit.backend.global.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@EnableWebFluxSecurity
@Configuration
class SecurityConfiguration {
    @Bean
    fun filterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    ): SecurityWebFilterChain =
        with(http) {
            csrf { it.disable() }
            formLogin { it.disable() }
            httpBasic { it.disable() }
            logout { it.disable() }
            requestCache { it.disable() }
            securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            exceptionHandling { it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) }
            authorizeExchange {
                it.pathMatchers("/admin/**")
                    .hasAuthority(Role.ADMIN.name)
                    .pathMatchers(
                        "/actuator/**",
                        "/oauth2/**",
                        "/login/**",
                        "/auth/refresh"
                    )
                    .permitAll()
                    .anyExchange()
                    .permitAll()
            }
            oauth2Login { it.authenticationSuccessHandler(oAuth2LoginSuccessHandler) }
            addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHORIZATION)
            build()
        }
}