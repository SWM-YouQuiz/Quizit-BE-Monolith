package com.quizit.backend.controller

import com.ninjasquad.springmockk.MockkBean
import com.quizit.backend.domain.quiz.dto.response.CheckAnswerResponse
import com.quizit.backend.domain.quiz.dto.response.QuizResponse
import com.quizit.backend.domain.quiz.exception.QuizNotFoundException
import com.quizit.backend.domain.quiz.handler.QuizHandler
import com.quizit.backend.domain.quiz.router.QuizRouter
import com.quizit.backend.domain.quiz.service.QuizService
import com.quizit.backend.domain.user.exception.PermissionDeniedException
import com.quizit.backend.fixture.*
import com.quizit.backend.global.dto.ErrorResponse
import com.quizit.backend.util.*
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(QuizRouter::class, QuizHandler::class)
class QuizControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var quizService: QuizService

    private val createQuizRequestFields = listOf(
        "question" desc "지문",
        "answer" desc "정답",
        "solution" desc "풀이",
        "chapterId" desc "챕터 식별자",
        "options" desc "선지"
    )

    private val updateQuizByIdRequestFields = listOf(
        "question" desc "지문",
        "answer" desc "정답",
        "solution" desc "풀이",
        "chapterId" desc "챕터 식별자",
        "options" desc "선지",
    )

    private val checkAnswerRequestFields = listOf(
        "answer" desc "정답"
    )

    private val quizResponseFields = listOf(
        "id" desc "식별자",
        "question" desc "지문",
        "writerId" desc "작성자 식별자",
        "chapterId" desc "챕터 식별자",
        "answerRate" desc "정답률",
        "options" desc "선지",
        "correctCount" desc "정답 횟수",
        "incorrectCount" desc "오답 횟수",
        "markedUserIds" desc "저장한 유저 리스트",
        "likedUserIds" desc "좋아요한 유저 리스트",
        "unlikedUserIds" desc "싫어요한 유저 리스트",
        "createdDate" desc "생성 날짜",
    )

    private val checkAnswerResponseFields = listOf(
        "answer" desc "정답",
        "solution" desc "해설"
    )

    init {
        describe("getQuizById()는") {
            context("퀴즈가 존재하는 경우") {
                every { quizService.getQuizById(any()) } returns createQuizResponse()
                withMockUser()

                it("상태 코드 200과 quizResponse를 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<QuizResponse>()
                        .document(
                            "식별자를 통한 퀴즈 단일 조회 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(quizResponseFields)
                        )
                }
            }

            context("퀴즈가 존재하지 않는 경우") {
                every { quizService.getQuizById(any()) } throws QuizNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 퀴즈 단일 조회 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("getQuizzesByChapterId()는") {
            context("챕터와 각각의 챕터에 속하는 퀴즈들이 존재하는 경우") {
                every { quizService.getQuizzesByChapterId(any()) } returns Flux.just(createQuizResponse())
                withMockUser()

                it("상태 코드 200과 quizResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/chapter/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<QuizResponse>>()
                        .document(
                            "챕터 식별자를 통한 퀴즈 전체 조회 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(quizResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getQuizzesByChapterIdAndAnswerRateRange()는") {
            context("챕터와 각각의 챕터에 속하는 퀴즈들이 존재하는 경우") {
                every {
                    quizService.getQuizzesByChapterIdAndAnswerRateRange(any(), any(), any())
                } returns listOf(createQuizResponse())
                withMockUser()

                it("상태 코드 200과 quizResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/chapter/{id}?page={page}&size={size}&range={range}", ID, 0, 1, "0,1")
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<QuizResponse>>()
                        .document(
                            "챕터 식별자를 통한 퀴즈 필터링 조회 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            queryParameters(
                                "page" paramDesc "페이지 번호",
                                "size" paramDesc "페이지 크기",
                                "range" paramDesc "정답률 범위"
                            ),
                            responseFields(quizResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getQuizzesByCourseId()는") {
            context("코스와 각각의 코스에 속하는 퀴즈들이 존재하는 경우") {
                every { quizService.getQuizzesByCourseId(any()) } returns listOf(createQuizResponse())
                withMockUser()

                it("상태 코드 200과 quizResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/course/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<QuizResponse>>()
                        .document(
                            "코스 식별자를 통한 퀴즈 전체 조회 성공(200)",
                            pathParameters("id" paramDesc "코스 식별자"),
                            responseFields(quizResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getQuizzesByWriterId()는") {
            context("유저가 작성한 퀴즈가 존재하는 경우") {
                every { quizService.getQuizzesByWriterId(any()) } returns listOf(createQuizResponse())
                withMockUser()

                it("상태 코드 200과 quizResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/writer/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<QuizResponse>>()
                        .document(
                            "유저가 작성한 퀴즈 전체 조회 성공(200)",
                            pathParameters("id" paramDesc "유저 식별자"),
                            responseFields(quizResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getQuizzesByQuestionContains()는") {
            context("주어진 키워드를 문제 지문에 포함하는 퀴즈가 존재하는 경우") {
                every { quizService.getQuizzesByQuestionContains(any()) } returns listOf(createQuizResponse())
                withMockUser()

                it("상태 코드 200과 quizResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/search?question={question}", QUESTION)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<QuizResponse>>()
                        .document(
                            "키워드를 문제 지문에 포함하는 퀴즈 전체 조회 성공(200)",
                            queryParameters("question" paramDesc "지문"),
                            responseFields(quizResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getMarkedQuizzes()는") {
            context("유저가 저장한 퀴즈가 존재하는 경우") {
                every { quizService.getMarkedQuizzes(any()) } returns listOf(createQuizResponse())
                withMockUser()

                it("상태 코드 200과 quizResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/marked-user/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<QuizResponse>>()
                        .document(
                            "유저가 저장한 퀴즈 전체 조회 성공(200)",
                            responseFields(quizResponseFields.toListFields())
                        )
                }
            }
        }

        describe("createQuiz()는") {
            context("유저가 퀴즈를 작성해서 제출하는 경우") {
                every { quizService.createQuiz(any(), any()) } returns createQuizResponse()
                withMockUser()

                it("상태 코드 200과 quizResponse를 반환한다.") {
                    webClient
                        .post()
                        .uri("/quiz")
                        .bodyValue(createCreateQuizRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<QuizResponse>()
                        .document(
                            "퀴즈 생성 성공(200)",
                            requestFields(createQuizRequestFields),
                            responseFields(quizResponseFields)
                        )
                }
            }
        }

        describe("updateQuizById()는") {
            context("유저가 퀴즈를 수정해서 제출하는 경우") {
                every { quizService.updateQuizById(any(), any(), any()) } returns createQuizResponse()
                withMockUser()

                it("상태 코드 200과 quizResponse를 반환한다.") {
                    webClient
                        .put()
                        .uri("/quiz/{id}", ID)
                        .bodyValue(createUpdateQuizByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<QuizResponse>()
                        .document(
                            "퀴즈 수정 성공(200)",
                            requestFields(updateQuizByIdRequestFields),
                            responseFields(quizResponseFields)
                        )
                }
            }

            context("퀴즈가 존재하지 않는 경우") {
                every { quizService.updateQuizById(any(), any(), any()) } throws QuizNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .put()
                        .uri("/quiz/{id}", ID)
                        .bodyValue(createUpdateQuizByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "퀴즈 수정 실패(404)",
                            requestFields(updateQuizByIdRequestFields),
                            responseFields(errorResponseFields)
                        )
                }
            }

            context("유저가 다른 유저의 퀴즈를 수정해서 제출하는 경우") {
                every { quizService.updateQuizById(any(), any(), any()) } throws PermissionDeniedException()
                withMockUser()

                it("상태 코드 403을 반환한다.") {
                    webClient
                        .put()
                        .uri("/quiz/{id}", ID)
                        .bodyValue(createUpdateQuizByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isForbidden
                        .expectBody<ErrorResponse>()
                        .document(
                            "퀴즈 수정 실패(403)",
                            requestFields(updateQuizByIdRequestFields),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("deleteQuizById()는") {
            context("유저가 퀴즈를 삭제하는 경우") {
                every { quizService.deleteQuizById(any(), any()) } returns empty()
                withMockUser()

                it("상태 코드 200을 반환한다.") {
                    webClient
                        .delete()
                        .uri("/quiz/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody()
                        .document(
                            "퀴즈 삭제 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                        )
                }
            }

            context("퀴즈가 존재하지 않는 경우") {
                every { quizService.deleteQuizById(any(), any()) } throws QuizNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .delete()
                        .uri("/quiz/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "퀴즈 삭제 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }

            context("유저가 다른 유저의 퀴즈를 삭제하는 경우") {
                every { quizService.deleteQuizById(any(), any()) } throws PermissionDeniedException()
                withMockUser()

                it("상태 코드 403을 반환한다.") {
                    webClient
                        .delete()
                        .uri("/quiz/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isForbidden
                        .expectBody<ErrorResponse>()
                        .document(
                            "퀴즈 삭제 실패(403)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("checkQuiz()는") {
            context("유저가 퀴즈 답을 제출한 경우") {
                every { quizService.checkAnswer(any(), any(), any()) } returns createCheckAnswerResponse()
                withMockUser()

                it("상태 코드 200과 정답 여부가 담긴 checkAnswerResponse를 반환한다.") {
                    webClient
                        .post()
                        .uri("/quiz/{id}/check", ID)
                        .bodyValue(createCheckAnswerRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CheckAnswerResponse>()
                        .document(
                            "정답 확인 성공(200)",
                            requestFields(checkAnswerRequestFields),
                            responseFields(checkAnswerResponseFields)
                        )
                }
            }

            context("퀴즈가 존재하지 않는 경우") {
                every { quizService.checkAnswer(any(), any(), any()) } throws QuizNotFoundException()
                withMockUser()

                it("상태 코드 404과 에러를 반환한다.") {
                    webClient
                        .post()
                        .uri("/quiz/{id}/check", ID)
                        .bodyValue(createCheckAnswerRequest())
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "정답 확인 실패(404)",
                            requestFields(checkAnswerRequestFields),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("markQuiz()는") {
            context("주어진 퀴즈 식별자에 대한 퀴즈가 존재하는 경우") {
                every { quizService.markQuiz(any(), any()) } returns createQuizResponse()
                withMockUser()

                it("상태 코드 200을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/{id}/mark", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<QuizResponse>()
                        .document(
                            "퀴즈 저장 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(quizResponseFields)
                        )
                }
            }

            context("주어진 퀴즈 식별자에 대한 퀴즈가 존재하지 않는 경우") {
                every { quizService.markQuiz(any(), any()) } throws QuizNotFoundException()
                withMockUser()

                it("상태 코드 404과 에러를 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/{id}/mark", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "퀴즈 저장 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("evaluateQuiz()는") {
            context("주어진 퀴즈 식별자에 대한 퀴즈가 존재하는 경우") {
                every { quizService.evaluateQuiz(any(), any(), any()) } returns Mono.just(createQuizResponse())
                withMockUser()

                it("상태 코드 200을 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/{id}/evaluate?isLike={isLike}", ID, true)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<QuizResponse>()
                        .document(
                            "퀴즈 평가 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            queryParameters("isLike" paramDesc "좋아요 여부"),
                            responseFields(quizResponseFields)
                        )
                }
            }

            context("주어진 퀴즈 식별자에 대한 퀴즈가 존재하지 않는 경우") {
                every { quizService.evaluateQuiz(any(), any(), any()) } throws QuizNotFoundException()
                withMockUser()

                it("상태 코드 404과 에러를 반환한다.") {
                    webClient
                        .get()
                        .uri("/quiz/{id}/evaluate?isLike={isLike}", ID, true)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "퀴즈 평가 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            queryParameters("isLike" paramDesc "좋아요 여부"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }
    }
}