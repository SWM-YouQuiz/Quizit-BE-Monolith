package com.quizit.backend.fixture

import com.quizit.backend.domain.auth.dto.response.RefreshResponse

fun createRefreshResponse(
    accessToken: String = ACCESS_TOKEN,
): RefreshResponse =
    RefreshResponse(
        accessToken = accessToken,
    )