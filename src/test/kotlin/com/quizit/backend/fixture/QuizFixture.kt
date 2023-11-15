package com.quizit.backend.fixture

import com.quizit.backend.domain.quiz.dto.request.CheckAnswerRequest
import com.quizit.backend.domain.quiz.dto.request.CreateQuizRequest
import com.quizit.backend.domain.quiz.dto.request.UpdateQuizByIdRequest
import com.quizit.backend.domain.quiz.dto.response.CheckAnswerResponse
import com.quizit.backend.domain.quiz.dto.response.GetAnswerByIdResponse
import com.quizit.backend.domain.quiz.dto.response.GetProgressByIdResponse
import com.quizit.backend.domain.quiz.dto.response.QuizResponse
import com.quizit.backend.domain.quiz.model.Quiz
import java.time.LocalDateTime

const val QUESTION = "question"
const val ANSWER = 1
const val SOLUTION = "solution"
val OPTIONS = (0..4).map { "$it" }
const val ANSWER_RATE = 50.0
const val CORRECT_COUNT = 10L
const val INCORRECT_COUNT = 10L
val MARKED_USER_IDS = hashSetOf("1")
val LIKED_USER_IDS = hashSetOf("1")
val UNLIKED_USER_IDS = hashSetOf("1")
const val TOTAL = 1
const val SOLVED = 0

fun createCreateQuizRequest(
    question: String = QUESTION,
    answer: Int = ANSWER,
    solution: String = SOLUTION,
    chapterId: String = ID,
    options: List<String> = OPTIONS,
): CreateQuizRequest =
    CreateQuizRequest(
        question = question,
        answer = answer,
        solution = solution,
        chapterId = chapterId,
        options = options
    )

fun createUpdateQuizByIdRequest(
    question: String = QUESTION,
    answer: Int = ANSWER,
    solution: String = SOLUTION,
    chapterId: String = ID,
    options: List<String> = OPTIONS,
): UpdateQuizByIdRequest =
    UpdateQuizByIdRequest(
        question = question,
        answer = answer,
        solution = solution,
        chapterId = chapterId,
        options = options
    )

fun createCheckAnswerRequest(
    answer: Int = ANSWER
): CheckAnswerRequest =
    CheckAnswerRequest(answer)

fun createGetProgressByIdResponse(
    total: Int = TOTAL,
    solved: Int = SOLVED
): GetProgressByIdResponse =
    GetProgressByIdResponse(
        total = total,
        solved = solved
    )

fun createGetAnswerByIdResponse(
    answer: Int = ANSWER
): GetAnswerByIdResponse =
    GetAnswerByIdResponse(answer)

fun createCheckAnswerResponse(
    answer: Int = ANSWER,
    solution: String = SOLUTION
): CheckAnswerResponse =
    CheckAnswerResponse(
        answer = answer,
        solution = solution
    )

fun createQuizResponse(
    id: String = ID,
    question: String = QUESTION,
    writerId: String = ID,
    chapterId: String = ID,
    options: List<String> = OPTIONS,
    answerRate: Double = ANSWER_RATE,
    correctCount: Long = CORRECT_COUNT,
    incorrectCount: Long = INCORRECT_COUNT,
    markedUserIds: HashSet<String> = MARKED_USER_IDS,
    likedUserIds: HashSet<String> = LIKED_USER_IDS,
    unlikedUserIds: HashSet<String> = UNLIKED_USER_IDS,
    createdDate: LocalDateTime = CREATED_DATE
): QuizResponse =
    QuizResponse(
        id = id,
        question = question,
        writerId = writerId,
        chapterId = chapterId,
        options = options,
        answerRate = answerRate,
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        markedUserIds = markedUserIds,
        likedUserIds = likedUserIds,
        unlikedUserIds = unlikedUserIds,
        createdDate = createdDate
    )

fun createQuiz(
    id: String = ID,
    question: String = QUESTION,
    answer: Int = ANSWER,
    solution: String = SOLUTION,
    writerId: String = ID,
    chapterId: String = ID,
    courseId: String = ID,
    curriculumId: String = ID,
    options: List<String> = OPTIONS,
    answerRate: Double = ANSWER_RATE,
    correctCount: Long = CORRECT_COUNT,
    incorrectCount: Long = INCORRECT_COUNT,
    createdDate: LocalDateTime = CREATED_DATE,
    markedUserIds: HashSet<String> = MARKED_USER_IDS.toHashSet(),
    likedUserIds: HashSet<String> = LIKED_USER_IDS.toHashSet(),
    unlikedUserIds: HashSet<String> = UNLIKED_USER_IDS.toHashSet(),
): Quiz = Quiz(
    id = id,
    question = question,
    answer = answer,
    solution = solution,
    writerId = writerId,
    chapterId = chapterId,
    courseId = courseId,
    curriculumId = curriculumId,
    options = options,
    answerRate = answerRate,
    correctCount = correctCount,
    createdDate = createdDate,
    markedUserIds = markedUserIds,
    incorrectCount = incorrectCount,
    likedUserIds = likedUserIds,
    unlikedUserIds = unlikedUserIds,
)