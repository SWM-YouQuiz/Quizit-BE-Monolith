package com.quizit.backend.service

import com.quizit.backend.domain.chapter.dto.response.ChapterResponse
import com.quizit.backend.domain.chapter.repository.ChapterRepository
import com.quizit.backend.domain.chapter.service.ChapterService
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

class ChapterServiceTest : BehaviorSpec() {
    private val chapterRepository = mockk<ChapterRepository>()

    private val quizRepository = mockk<QuizRepository>()

    private val userRepository = mockk<UserRepository>()

    private val chapterService = ChapterService(
        chapterRepository = chapterRepository,
        quizRepository = quizRepository,
        userRepository = userRepository
    )

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    init {
        Given("챕터들이 존재하는 경우") {
            val chapter = createChapter()
                .also {
                    every { chapterRepository.findAllByCourseIdOrderByIndex(any()) } returns listOf(it)
                    every { chapterRepository.findById(any<String>()) } returns it
                    every { chapterRepository.deleteById(any<String>()) } returns empty()
                    every { quizRepository.findAllByChapterId(any()) } returns listOf(createQuiz())
                    every { userRepository.findById(any<String>()) } returns createUser()
                }
            val chapterResponse = ChapterResponse(chapter)

            When("유저가 메인 화면에 들어가면") {
                val results = listOf(
                    chapterService.getChaptersByCourseId(ID)
                        .getResult(),
                    chapterService.getChapterById(ID)
                        .getResult()
                )
                val result = chapterService.getProgressById(ID, ID)
                    .getResult()

                Then("챕터가 주어진다.") {
                    results.map {
                        it.expectSubscription()
                            .expectNext(chapterResponse)
                            .verifyComplete()
                    }
                }

                Then("챕터의 진척도가 주어진다.") {
                    result.expectSubscription()
                        .expectNext(createGetProgressByIdResponse())
                        .verifyComplete()
                }
            }

            When("어드민이 특정 챕터를 수정하면") {
                val updateChapterByIdRequest = createUpdateChapterByIdRequest(description = "updated_description")
                    .also {
                        every { chapterRepository.save(any()) } returns createChapter(description = it.description)
                    }
                val result =
                    chapterService.updateChapterById(ID, updateChapterByIdRequest)
                        .getResult()

                Then("해당 챕터가 수정된다.") {
                    result.expectSubscription()
                        .assertNext { it shouldNotBeEqual chapterResponse }
                        .verifyComplete()
                }
            }

            When("어드민이 특정 챕터를 삭제하면") {
                val result = chapterService.deleteChapterById(ID)
                    .getResult()

                Then("해당 챕터가 삭제된다.") {
                    result.expectSubscription()
                        .verifyComplete()
                }
            }
        }

        Given("어드민이 챕터를 작성 중인 경우") {
            val chapter = createChapter()
                .also {
                    every { chapterRepository.save(any()) } returns it
                }
            val chapterResponse = ChapterResponse(chapter)

            When("어드민이 챕터를 제출하면") {
                val result = chapterService.createChapter(createCreateChapterRequest())
                    .getResult()

                Then("챕터가 생성된다.") {
                    result.expectSubscription()
                        .expectNext(chapterResponse)
                        .verifyComplete()
                }
            }
        }
    }
}