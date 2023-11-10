package com.quizit.backend.global.jwt

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class JwtAuthentication(
    val id: String,
    @JvmField
    val authorities: List<GrantedAuthority>
) : Authentication {
    override fun getAuthorities(): List<GrantedAuthority> = authorities

    override fun isAuthenticated(): Boolean = true

    override fun getName(): String? = null

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): Any? = null

    override fun setAuthenticated(isAuthenticated: Boolean) {
        throw UnsupportedOperationException("Cannot change the authenticated state of JwtAuthentication")
    }
}