package com.quizit.backend.controller

import com.ninjasquad.springmockk.MockkBean
import com.quizit.backend.domain.chapter.dto.response.ChapterResponse
import com.quizit.backend.domain.chapter.exception.ChapterNotFoundException
import com.quizit.backend.domain.chapter.handler.ChapterHandler
import com.quizit.backend.domain.chapter.router.ChapterRouter
import com.quizit.backend.domain.chapter.service.ChapterService
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

@WebFluxTest(ChapterRouter::class, ChapterHandler::class)
class ChapterControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var chapterService: ChapterService

    private val createChapterRequestFields = listOf(
        "description" desc "설명",
        "document" desc "공식 문서",
        "courseId" desc "코스 식별자",
        "image" desc "이미지",
        "index" desc "순서"
    )

    private val updateChapterByIdRequestFields = listOf(
        "description" desc "설명",
        "document" desc "공식 문서",
        "image" desc "이미지",
        "index" desc "순서"
    )

    private val chapterResponseFields = listOf(
        "id" desc "식별자",
        "description" desc "설명",
        "document" desc "공식 문서",
        "courseId" desc "코스 식별자",
        "image" desc "이미지",
        "index" desc "순서"
    )

    private val getProgressByIdResponseFields = listOf(
        "total" desc "총 퀴즈 수",
        "solved" desc "푼 퀴즈 수"
    )

    init {
        describe("getChapterById()는") {
            context("챕터가 존재하는 경우") {
                every { chapterService.getChapterById(any()) } returns createChapterResponse()
                withMockUser()

                it("상태 코드 200과 chapterResponse를 반환한다.") {
                    webClient.get()
                        .uri("/chapter/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<ChapterResponse>()
                        .document(
                            "식별자를 통한 챕터 단일 조회 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(chapterResponseFields)
                        )
                }
            }

            context("챕터가 존재하지 않는 경우") {
                every { chapterService.getChapterById(any()) } throws ChapterNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/chapter/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 챕터 단일 조회 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("getChaptersByCourseId()는") {
            context("코스와 각각의 코스에 속하는 챕터들이 존재하는 경우") {
                every { chapterService.getChaptersByCourseId(any()) } returns listOf(createChapterResponse())
                withMockUser()

                it("상태 코드 200과 chapterResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/chapter/course/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<ChapterResponse>>()
                        .document(
                            "코스 식별자를 통한 챕터 전체 조회 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(chapterResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getProgressById()는") {
            context("챕터가 존재하는 경우") {
                every { chapterService.getProgressById(any(), any()) } returns createGetProgressByIdResponse()
                withMockUser()

                it("상태 코드 200과 getProgressByIdResponse를 반환한다.") {
                    webClient
                        .get()
                        .uri("/chapter/{id}/progress", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<GetProgressByIdResponse>()
                        .document(
                            "식별자를 통한 챕터 진척도 조회 성공(200)",
                            responseFields(getProgressByIdResponseFields)
                        )
                }
            }

            context("챕터가 존재하지 않는 경우") {
                every { chapterService.getProgressById(any(), any()) } throws ChapterNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/chapter/{id}/progress", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 챕터 진척도 조회 실패(404)",
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("createChapter()는") {
            context("어드민이 챕터를 작성해서 제출하는 경우") {
                every { chapterService.createChapter(any()) } returns createChapterResponse()
                withMockAdmin()

                it("상태 코드 200과 chapterResponse를 반환한다.") {
                    webClient
                        .post()
                        .uri("/admin/chapter")
                        .bodyValue(createCreateChapterRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<ChapterResponse>()
                        .document(
                            "챕터 생성 성공(200)",
                            requestFields(createChapterRequestFields),
                            responseFields(chapterResponseFields)
                        )
                }
            }
        }

        describe("updateChapterById()는") {
            context("어드민이 챕터를 수정해서 제출하는 경우") {
                every { chapterService.updateChapterById(any(), any()) } returns createChapterResponse()
                withMockAdmin()

                it("상태 코드 200과 chapterResponse를 반환한다.") {
                    webClient
                        .put()
                        .uri("/admin/chapter/{id}", ID)
                        .bodyValue(createUpdateChapterByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<ChapterResponse>()
                        .document(
                            "챕터 수정 성공(200)",
                            requestFields(updateChapterByIdRequestFields),
                            responseFields(chapterResponseFields)
                        )
                }
            }

            context("챕터가 존재하지 않는 경우") {
                every { chapterService.updateChapterById(any(), any()) } throws ChapterNotFoundException()
                withMockAdmin()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .put()
                        .uri("/admin/chapter/{id}", ID)
                        .bodyValue(createUpdateChapterByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "챕터 수정 실패(404)",
                            requestFields(updateChapterByIdRequestFields),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("deleteChapterById()는") {
            context("어드민이 챕터를 삭제하는 경우") {
                every { chapterService.deleteChapterById(any()) } returns empty()
                withMockAdmin()

                it("상태 코드 200을 반환한다.") {
                    webClient
                        .delete()
                        .uri("/admin/chapter/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody()
                        .document(
                            "챕터 삭제 성공(200)",
                            pathParameters("id" paramDesc "식별자")
                        )
                }
            }

            context("챕터가 존재하지 않는 경우") {
                every { chapterService.deleteChapterById(any()) } throws ChapterNotFoundException()
                withMockAdmin()

                it("상태 코드 404을 반환한다.") {
                    webClient
                        .delete()
                        .uri("/admin/chapter/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "챕터 삭제 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }
    }
}