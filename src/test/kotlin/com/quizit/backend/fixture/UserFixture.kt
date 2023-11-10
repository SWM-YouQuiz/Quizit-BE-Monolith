package com.quizit.backend.fixture

import com.quizit.backend.domain.user.dto.request.CreateUserRequest
import com.quizit.backend.domain.user.dto.request.UpdateUserByIdRequest
import com.quizit.backend.domain.user.dto.response.UserResponse
import com.quizit.backend.domain.user.model.User
import com.quizit.backend.domain.user.model.enum.Provider
import com.quizit.backend.domain.user.model.enum.Role
import java.time.LocalDateTime

const val EMAIL = "email"
const val USERNAME = "username"
const val IMAGE = "image"
const val LEVEL = 2
val ROLE = Role.USER
const val ALLOW_PUSH = true
const val DAILY_TARGET = 10
val PROVIDER = Provider.GOOGLE
val CORRECT_QUIZ_IDS = hashSetOf("1")
val INCORRECT_QUIZ_IDS = hashSetOf("1")
val MARKED_QUIZ_IDS = hashSetOf("1")

fun createCreateUserRequest(
    email: String = EMAIL,
    username: String = USERNAME,
    image: String = IMAGE,
    allowPush: Boolean = ALLOW_PUSH,
    dailyTarget: Int = DAILY_TARGET,
    provider: Provider = PROVIDER
): CreateUserRequest =
    CreateUserRequest(
        email = email,
        username = username,
        image = image,
        allowPush = allowPush,
        dailyTarget = dailyTarget,
        provider = provider
    )

fun createUserResponse(
    id: String = ID,
    email: String = EMAIL,
    username: String = USERNAME,
    image: String = IMAGE,
    level: Int = LEVEL,
    role: Role = ROLE,
    allowPush: Boolean = ALLOW_PUSH,
    dailyTarget: Int = DAILY_TARGET,
    answerRate: Double = ANSWER_RATE,
    provider: Provider = PROVIDER,
    createdDate: LocalDateTime = CREATED_DATE,
    correctQuizIds: HashSet<String> = CORRECT_QUIZ_IDS,
    incorrectQuizIds: HashSet<String> = INCORRECT_QUIZ_IDS,
    markedQuizIds: HashSet<String> = MARKED_QUIZ_IDS,
): UserResponse =
    UserResponse(
        id = id,
        email = email,
        username = username,
        image = image,
        level = level,
        role = role,
        allowPush = allowPush,
        dailyTarget = dailyTarget,
        answerRate = answerRate,
        provider = provider,
        createdDate = createdDate,
        correctQuizIds = correctQuizIds,
        incorrectQuizIds = incorrectQuizIds,
        markedQuizIds = markedQuizIds
    )

fun createUpdateUserByIdRequest(
    username: String = USERNAME,
    image: String = IMAGE,
    allowPush: Boolean = ALLOW_PUSH,
    dailyTarget: Int = DAILY_TARGET
): UpdateUserByIdRequest =
    UpdateUserByIdRequest(
        username = username,
        image = image,
        allowPush = allowPush,
        dailyTarget = dailyTarget
    )

fun createUser(
    id: String = ID,
    email: String = EMAIL,
    username: String = USERNAME,
    image: String = IMAGE,
    level: Int = LEVEL,
    role: Role = ROLE,
    allowPush: Boolean = ALLOW_PUSH,
    dailyTarget: Int = DAILY_TARGET,
    answerRate: Double = ANSWER_RATE,
    provider: Provider = PROVIDER,
    createdDate: LocalDateTime = CREATED_DATE,
    correctQuizIds: HashSet<String> = CORRECT_QUIZ_IDS.toHashSet(),
    incorrectQuizIds: HashSet<String> = INCORRECT_QUIZ_IDS.toHashSet(),
    markedQuizIds: HashSet<String> = MARKED_QUIZ_IDS.toHashSet(),
): User = User(
    id = id,
    email = email,
    username = username,
    image = image,
    level = level,
    role = role,
    allowPush = allowPush,
    dailyTarget = dailyTarget,
    answerRate = answerRate,
    provider = provider,
    createdDate = createdDate,
    correctQuizIds = correctQuizIds,
    incorrectQuizIds = incorrectQuizIds,
    markedQuizIds = markedQuizIds,
)