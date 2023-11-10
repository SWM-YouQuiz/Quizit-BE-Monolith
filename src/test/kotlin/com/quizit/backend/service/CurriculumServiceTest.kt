package com.quizit.backend.service

import com.quizit.backend.domain.curriculum.dto.response.CurriculumResponse
import com.quizit.backend.domain.curriculum.repository.CurriculumRepository
import com.quizit.backend.domain.curriculum.service.CurriculumService
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.fixture.*
import com.quizit.backend.util.empty
import com.quizit.backend.util.getResult
import com.quizit.backend.util.returns
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldNotBeEqual
import io.mockk.every
import io.mockk.mockk

class CurriculumServiceTest : BehaviorSpec() {
    private val curriculumRepository = mockk<CurriculumRepository>()

    private val quizRepository = mockk<QuizRepository>()

    private val userRepository = mockk<UserRepository>()

    private val curriculumService = CurriculumService(
        curriculumRepository = curriculumRepository,
        quizRepository = quizRepository,
        userRepository = userRepository
    )

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    init {
        Given("커리큘럼들이 존재하는 경우") {
            val curriculum = createCurriculum()
                .also {
                    every { curriculumRepository.findAll() } returns listOf(it)
                    every { curriculumRepository.findById(any<String>()) } returns it
                    every { curriculumRepository.deleteById(any<String>()) } returns empty()
                    every { quizRepository.findAllByCurriculumId(any()) } returns listOf(createQuiz())
                    every { userRepository.findById(any<String>()) } returns createUser()
                }
            val curriculumResponse = CurriculumResponse(curriculum)

            When("유저가 메인 화면에 들어가면") {
                val results = listOf(
                    curriculumService.getCurriculums()
                        .getResult(),
                    curriculumService.getCurriculumById(ID)
                        .getResult()
                )
                val result = curriculumService.getProgressById(ID, ID)
                    .getResult()

                Then("커리큘럼이 주어진다.") {
                    results.map {
                        it.expectSubscription()
                            .expectNext(curriculumResponse)
                            .verifyComplete()
                    }
                }

                Then("커리큘럼의 진척도가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(createGetProgressByIdResponse())
                        .verifyComplete()
                }
            }

            When("어드민이 특정 커리큘럼을 수정하면") {
                val updateCurriculumByIdRequest = createUpdateCurriculumByIdRequest(title = "updated_title")
                    .also {
                        every { curriculumRepository.save(any()) } returns createCurriculum(title = it.title)
                    }
                val result = curriculumService.updateCurriculumById(ID, updateCurriculumByIdRequest)
                    .getResult()

                Then("해당 커리큘럼이 수정된다.") {
                    result.expectSubscription()
                        .assertNext { it shouldNotBeEqual curriculumResponse }
                        .verifyComplete()
                }
            }

            When("어드민이 특정 커리큘럼을 삭제하면") {
                val result = curriculumService.deleteCurriculumById(ID)
                    .getResult()

                Then("해당 커리큘럼이 삭제된다.") {
                    result.expectSubscription()
                        .verifyComplete()
                }
            }
        }

        Given("어드민이 커리큘럼을 작성 중인 경우") {
            val curriculum = createCurriculum()
                .also {
                    every { curriculumRepository.save(any()) } returns it
                }
            val curriculumResponse = CurriculumResponse(curriculum)

            When("어드민이 커리큘럼을 제출하면") {
                val result = curriculumService.createCurriculum(createCreateCurriculumRequest())
                    .getResult()

                Then("커리큘럼이 생성된다.") {
                    result.expectSubscription()
                        .expectNext(curriculumResponse)
                        .verifyComplete()
                }
            }
        }
    }
}