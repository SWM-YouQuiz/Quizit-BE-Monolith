package com.quizit.backend.domain.auth.model

import com.quizit.backend.domain.user.model.enum.Provider
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

sealed class OAuth2UserInfo(
    val email: String,
    val provider: Provider,
    private val name: String?,
    private val attributes: Map<String, *>
) : OAuth2User {
    override fun getAttributes(): Map<String, *> = attributes

    override fun getName(): String? = name

    override fun getAuthorities(): List<GrantedAuthority>? = null
}

class GoogleOAuth2UserInfo(
    attributes: Map<String, *>
) : OAuth2UserInfo(
    email = attributes["email"] as String,
    provider = Provider.GOOGLE,
    name = attributes["name"] as String,
    attributes = attributes
)

class AppleOAuth2UserInfo(
    email: String,
    name: String?
) : OAuth2UserInfo(
    email = email,
    provider = Provider.APPLE,
    name = name,
    attributes = emptyMap<String, Any>()
)

class KakaoOAuth2UserInfo(
    attributes: Map<String, *>
) : OAuth2UserInfo(
    email = (attributes["kakao_account"] as Map<*, *>)["email"] as String,
    provider = Provider.KAKAO,
    name = (attributes["properties"] as Map<*, *>)["nickname"] as String,
    attributes = attributes
)