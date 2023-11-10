package com.quizit.backend.controller

import com.ninjasquad.springmockk.MockkBean
import com.quizit.backend.domain.course.dto.response.CourseResponse
import com.quizit.backend.domain.course.exception.CourseNotFoundException
import com.quizit.backend.domain.course.handler.CourseHandler
import com.quizit.backend.domain.course.router.CourseRouter
import com.quizit.backend.domain.course.service.CourseService
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

@WebFluxTest(CourseRouter::class, CourseHandler::class)
class CourseControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var courseService: CourseService

    private val createCourseRequestFields = listOf(
        "title" desc "제목",
        "image" desc "이미지",
        "curriculumId" desc "커리큘럼 식별자"
    )

    private val updateCourseByIdRequestFields = listOf(
        "title" desc "제목",
        "image" desc "이미지",
    )

    private val courseResponseFields = listOf(
        "id" desc "식별자",
        "title" desc "제목",
        "image" desc "이미지",
        "curriculumId" desc "커리큘럼 식별자"
    )

    private val getProgressByIdResponseFields = listOf(
        "total" desc "총 퀴즈 수",
        "solved" desc "푼 퀴즈 수"
    )

    init {
        describe("getCourseById()는") {
            context("코스가 존재하는 경우") {
                every { courseService.getCourseById(any()) } returns createCourseResponse()
                withMockUser()

                it("상태 코드 200과 courseResponse를 반환한다.") {
                    webClient
                        .get()
                        .uri("/course/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CourseResponse>()
                        .document(
                            "식별자를 통한 코스 단일 조회 성공(200)",
                            responseFields(courseResponseFields)
                        )
                }
            }

            context("코스가 존재하지 않는 경우") {
                every { courseService.getCourseById(any()) } throws CourseNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/course/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 코스 단일 조회 실패(404)",
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("getCoursesByCurriculumId()는") {
            context("커리큘럼과 각각의 커리큘럼에 속하는 코스들이 존재하는 경우") {
                every { courseService.getCoursesByCurriculumId(any()) } returns listOf(createCourseResponse())
                withMockUser()

                it("상태 코드 200과 courseResponse들을 반환한다.") {
                    webClient
                        .get()
                        .uri("/course/curriculum/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<List<CourseResponse>>()
                        .document(
                            "커리큘럼 식별자를 통한 코스 전체 조회 성공(200)",
                            responseFields(courseResponseFields.toListFields())
                        )
                }
            }
        }

        describe("getProgressById()는") {
            context("코스가 존재하는 경우") {
                every { courseService.getProgressById(ID, ID) } returns createGetProgressByIdResponse()
                withMockUser()

                it("상태 코드 200과 getProgressByIdResponse를 반환한다.") {
                    webClient
                        .get()
                        .uri("/course/{id}/progress", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<GetProgressByIdResponse>()
                        .document(
                            "식별자를 통한 코스 진척도 조회 성공(200)",
                            responseFields(getProgressByIdResponseFields)
                        )
                }
            }

            context("코스가 존재하지 않는 경우") {
                every { courseService.getProgressById(ID, ID) } throws CourseNotFoundException()
                withMockUser()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .get()
                        .uri("/course/{id}/progress", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "식별자를 통한 코스 진척도 조회 실패(404)",
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("createCourse()는") {
            context("어드민이 코스를 작성해서 제출하는 경우") {
                every { courseService.createCourse(any()) } returns createCourseResponse()
                withMockAdmin()

                it("상태 코드 200과 courseResponse를 반환한다.") {
                    webClient
                        .post()
                        .uri("/admin/course")
                        .bodyValue(createCreateCourseRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CourseResponse>()
                        .document(
                            "코스 생성 성공(200)",
                            requestFields(createCourseRequestFields),
                            responseFields(courseResponseFields)
                        )
                }
            }
        }

        describe("updateCourseById()는") {
            context("어드민이 코스를 수정해서 제출하는 경우") {
                every { courseService.updateCourseById(any(), any()) } returns createCourseResponse()
                withMockAdmin()

                it("상태 코드 200과 courseResponse를 반환한다.") {
                    webClient
                        .put()
                        .uri("/admin/course/{id}", ID)
                        .bodyValue(createUpdateCourseByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody<CourseResponse>()
                        .document(
                            "코스 수정 성공(200)",
                            requestFields(updateCourseByIdRequestFields),
                            responseFields(courseResponseFields)
                        )
                }
            }

            context("코스가 존재하지 않는 경우") {
                every { courseService.updateCourseById(any(), any()) } throws CourseNotFoundException()
                withMockAdmin()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .put()
                        .uri("/admin/course/{id}", ID)
                        .bodyValue(createUpdateCourseByIdRequest())
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "코스 수정 실패(404)",
                            requestFields(updateCourseByIdRequestFields),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }

        describe("deleteCourseById()는") {
            context("어드민이 코스를 삭제하는 경우") {
                every { courseService.deleteCourseById(any()) } returns empty()
                withMockAdmin()

                it("상태 코드 200을 반환한다.") {
                    webClient
                        .delete()
                        .uri("/admin/course/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody()
                        .document(
                            "코스 삭제 성공(200)",
                            pathParameters("id" paramDesc "식별자"),
                        )
                }
            }

            context("코스가 존재하지 않는 경우") {
                every { courseService.deleteCourseById(any()) } throws CourseNotFoundException()
                withMockAdmin()

                it("상태 코드 404를 반환한다.") {
                    webClient
                        .delete()
                        .uri("/admin/course/{id}", ID)
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody<ErrorResponse>()
                        .document(
                            "코스 삭제 실패(404)",
                            pathParameters("id" paramDesc "식별자"),
                            responseFields(errorResponseFields)
                        )
                }
            }
        }
    }
}