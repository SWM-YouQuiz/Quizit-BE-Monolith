package com.quizit.backend.controller

import com.ninjasquad.springmockk.MockkBean
import com.quizit.backend.domain.curriculum.dto.response.CurriculumResponse
import com.quizit.backend.domain.curriculum.exception.CurriculumNotFoundException
import com.quizit.backend.domain.curriculum.handler.CurriculumHandler
import com.quizit.backend.domain.curriculum.router.CurriculumRouter
import com.quizit.backend.domain.curriculum.service.CurriculumService
import com.quizit.backend.domain.quiz.dto.response.GetProgressByIdResponse
import com.quizit.backend.fixture.*
import com.quizit.backend.global.dto.ErrorResponse
import com.quizit.backend.util.*
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.reactive.server.expectBody

@WebFluxTest(CurriculumRouter::class, CurriculumHandler::class)
class CurriculumControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var curriculumService: CurriculumService

    private val createCurriculumRequestFields = listOf(
        "title" desc "제목",
        "image" desc "이미지"
    )

    private val updateCurriculumByIdRequestFields = listOf(
        "title" desc "제목",
        "image" desc "이미지"
    )

    private val curriculumResponseFields = listOf(
        "id" desc "식별자",
        "title" desc "제목",
        "image" desc "이미지"
    )

    private val getProgressByIdResponseFields = listOf(
        "total" desc "총 퀴즈 수",
        "solved" desc "푼 퀴즈 수"
    )

    init {
        describe("getCurriculumById()는") {
            context("커리큘럼이 존재하는 경우") {
                every { curriculumService.getCurriculumById(any()) } returns createCurriculumResponse()
                withMockUser()

                it("상태 코드 200과 curriculumResponse를 반환한다.") {
                    webClient
                        .get()
                        .uri("/curriculum/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CurriculumResponse>()
                        .document(
                            "식별자를 통한 커리큘럼 단일 조회 성공(200)",
                            responseFields(curriculumResponseFields)
                        )
                }
            }

            context("커리큘럼이 존재하지 않는 경우") {
                every { curriculumService.getCurriculumById(any()) } throws CurriculumNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/curriculum/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 커리큘럼 단일 조회 실패(404)",
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("getCurriculums()는") {
            context("커리큘럼들이 존재하는 경우") {
                every { curriculumService.getCurriculums() } returns listOf(createCurriculumResponse())
                withMockUser()

                it("상태 코드 200과 curriculumResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/curriculum")
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<CurriculumResponse>>()
                        .document(
                            "커리큘럼 전체 조회 성공(200)",
                            responseFields(curriculumResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getProgressById()는") {
            context("커리큘럼이 존재하는 경우") {
                every { curriculumService.getProgressById(ID, ID) } returns createGetProgressByIdResponse()
                withMockUser()

                it("상태 코드 200과 getProgressByIdResponse를 반환한다.") {
                    webClient
                        .get()
                        .uri("/curriculum/{id}/progress", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<GetProgressByIdResponse>()
                        .document(
                            "식별자를 통한 커리큘럼 진척도 조회 성공(200)",
                            responseFields(getProgressByIdResponseFields)
                        )
                }
            }

            context("커리큘럼이 존재하지 않는 경우") {
                every { curriculumService.getProgressById(ID, ID) } throws CurriculumNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/curriculum/{id}/progress", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 커리큘럼 진척도 조회 실패(404)",
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("createCurriculum()는") {
            context("어드민이 챕터를 작성해서 제출하는 경우") {
                every { curriculumService.createCurriculum(any()) } returns createCurriculumResponse()
                withMockAdmin()

                it("상태 코드 200과 curriculumResponse를 반환한다.") {
                    webClient
                        .post()
                        .uri("/admin/curriculum")
                        .bodyValue(createCreateCurriculumRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CurriculumResponse>()
                        .document(
                            "커리큘럼 생성 성공(200)",
                            requestFields(createCurriculumRequestFields),
                            responseFields(curriculumResponseFields)
                        )
                }
            }
        }

        describe("updateCurriculumById()는") {
            context("어드민이 커리큘럼을 수정해서 제출하는 경우") {
                every { curriculumService.updateCurriculumById(any(), any()) } returns createCurriculumResponse()
                withMockAdmin()

                it("상태 코드 200과 curriculumResponse를 반환한다.") {
                    webClient
                        .put()
                        .uri("/admin/curriculum/{id}", ID)
                        .bodyValue(createUpdateCurriculumByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CurriculumResponse>()
                        .document(
                            "커리큘럼 수정 성공(200)",
                            requestFields(updateCurriculumByIdRequestFields),
                            responseFields(curriculumResponseFields)
                        )
                }
            }

            context("커리큘럼이 존재하지 않는 경우") {
                every { curriculumService.updateCurriculumById(any(), any()) } throws CurriculumNotFoundException()
                withMockAdmin()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .put()
                        .uri("/admin/curriculum/{id}", ID)
                        .bodyValue(createUpdateCurriculumByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "커리큘럼 수정 실패(404)",
                            requestFields(updateCurriculumByIdRequestFields),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("deleteCurriculumById()는") {
            context("어드민이 챕터를 삭제하는 경우") {
                every { curriculumService.deleteCurriculumById(any()) } returns empty()
                withMockAdmin()

                it("상태 코드 200을 반환한다.") {
                    webClient
                        .delete()
                        .uri("/admin/curriculum/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody()
                        .document(
                            "커리큘럼 삭제 성공(200)",
                            pathParameters("id" paramDesc "식별자")
                        )
                }
            }

            context("어드민이 챕터를 삭제하는 경우") {
                every { curriculumService.deleteCurriculumById(any()) } throws CurriculumNotFoundException()
                withMockAdmin()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .delete()
                        .uri("/admin/curriculum/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "커리큘럼 삭제 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }
    }
}