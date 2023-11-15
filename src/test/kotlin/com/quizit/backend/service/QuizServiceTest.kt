package com.quizit.backend.service

import com.quizit.backend.domain.chapter.repository.ChapterRepository
import com.quizit.backend.domain.course.repository.CourseRepository
import com.quizit.backend.domain.quiz.dto.response.QuizResponse
import com.quizit.backend.domain.quiz.repository.QuizRepository
import com.quizit.backend.domain.quiz.service.QuizService
import com.quizit.backend.domain.user.model.User
import com.quizit.backend.domain.user.repository.UserRepository
import com.quizit.backend.fixture.*
import com.quizit.backend.util.empty
import com.quizit.backend.util.getResult
import com.quizit.backend.util.returns
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class QuizServiceTest : BehaviorSpec() {
    private val quizRepository = mockk<QuizRepository>()

    private val chapterRepository = mockk<ChapterRepository>()

    private val courseRepository = mockk<CourseRepository>()

    private val userRepository = mockk<UserRepository>()

    private val quizService = QuizService(
        quizRepository = quizRepository,
        chapterRepository = chapterRepository,
        courseRepository = courseRepository,
        userRepository = userRepository,
    )

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    init {
        Given("챕터와 각각의 챕터에 속하는 퀴즈들이 존재하는 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findAllByCourseId(any()) } returns listOf(it)
                    every { quizRepository.findAllByChapterId(any()) } returns listOf(it)
                    every { quizRepository.findAllByIdIn(any()) } returns listOf(it)
                    every { quizRepository.findAllByQuestionContains(any()) } returns listOf(it)
                    every {
                        quizRepository.findAllByChapterIdAndAnswerRateBetween(any(), any(), any(), any())
                    } returns listOf(it)
                    every { quizRepository.findById(any<String>()) } returns it
                    every { quizRepository.deleteById(any<String>()) } returns empty()
                }
            createUser()
                .also {
                    every { userRepository.findAll() } returns listOf(it)
                    every { userRepository.saveAll(any<List<User>>()) } returns listOf(it)
                }
            val quizResponse = QuizResponse(quiz)

            When("유저가 특정 퀴즈를 조회하면") {
                val result = quizService.getQuizById(ID)
                    .getResult()

                Then("해당 퀴즈가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(quizResponse)
                        .verifyComplete()
                }
            }

            When("어드민이 특정 퀴즈의 정답을 조회하면") {
                val result = quizService.getAnswerById(ID)
                    .getResult()

                Then("해당 퀴즈의 정답이 조회된다.") {
                    result.expectSubscription()
                        .expectNext(createGetAnswerByIdResponse())
                        .verifyComplete()
                }
            }

            When("특정 코스에 속한 퀴즈들을 조회하면") {
                val result = quizService.getQuizzesByCourseId(ID)
                    .getResult()

                Then("해당 코스에 속하는 퀴즈들이 조회된다.") {
                    result.expectSubscription()
                        .expectNext(quizResponse)
                        .verifyComplete()
                }
            }

            When("유저가 특정 퀴즈를 문제 지문을 통해 검색하면") {
                val result = quizService.getQuizzesByQuestionContains(QUESTION)
                    .getResult()

                Then("해당 키워드가 들어간 문제 지문을 가진 퀴즈가 조회된다.") {
                    result.expectSubscription()
                        .expectNext(quizResponse)
                        .verifyComplete()
                }
            }

            When("유저가 챕터를 들어가면") {
                val results = listOf(
                    quizService.getQuizzesByChapterId(ID)
                        .getResult(),
                    quizService.getQuizzesByChapterIdAndAnswerRateRange(ID, setOf(0.0, 100.0), PAGEABLE)
                        .getResult()
                )

                Then("해당 챕터에 속하는 퀴즈들이 주어진다.") {
                    results.map {
                        it.expectSubscription()
                            .expectNext(quizResponse)
                            .verifyComplete()
                    }
                }
            }

            When("유저가 특정 퀴즈를 수정하면") {
                val updateQuizByIdRequest = createUpdateQuizByIdRequest(question = "updated_question")
                    .also {
                        every { quizRepository.save(any()) } returns createQuiz(question = it.question)
                    }
                val result = quizService.updateQuizById(ID, createJwtAuthentication(), updateQuizByIdRequest)
                    .getResult()

                Then("해당 퀴즈가 수정된다.") {
                    result.expectSubscription()
                        .assertNext { it shouldNotBeEqual quizResponse }
                        .verifyComplete()
                }
            }

            When("유저가 특정 퀴즈를 삭제하면") {
                val result = quizService.deleteQuizById(ID, createJwtAuthentication())
                    .getResult()

                Then("해당 퀴즈가 삭제된다.") {
                    result.expectSubscription()
                        .verifyComplete()
                }
            }
        }

        Given("유저가 저장한 퀴즈가 존재하는 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findAllByIdIn(any()) } returns listOf(it)
                }
            createUser()
                .also {
                    every { userRepository.findById(any<String>()) } returns it
                }

            val quizResponse = QuizResponse(quiz)

            When("유저가 본인이 저장한 퀴즈 보관함에 들어가면") {
                val result = quizService.getMarkedQuizzes(ID)
                    .getResult()

                Then("유저가 저장한 퀴즈들이 주어진다.") {
                    result.expectSubscription()
                        .expectNext(quizResponse)
                        .verifyComplete()
                }
            }
        }

        Given("유저가 작성한 퀴즈가 존재하는 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findAllByWriterId(any()) } returns listOf(it)
                }
            val quizResponse = QuizResponse(quiz)

            When("유저가 본인이 작성한 퀴즈 보관함에 들어가면") {
                val result = quizService.getQuizzesByWriterId(ID)
                    .getResult()

                Then("본인이 작성한 퀴즈들이 주어진다.") {
                    result.expectSubscription()
                        .expectNext(quizResponse)
                        .verifyComplete()
                }
            }
        }

        Given("유저가 퀴즈를 푼 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findById(any<String>()) } returns it
                    every { quizRepository.save(any()) } returns it
                }
            val user = createUser()
                .also {
                    every { userRepository.findById(any<String>()) } returns it
                    every { userRepository.save(any()) } returns it
                }

            When("옳은 답을 제출하면") {
                val result = quizService.checkAnswer(ID, ID, createCheckAnswerRequest())
                    .getResult()

                Then("정답으로 처리되어 정답률이 변경된다.") {
                    result.expectSubscription()
                        .assertNext { verify { quizRepository.save(any()) } }
                        .verifyComplete()
                }
            }

            When("틀린 답을 제출하면") {
                val result = quizService.checkAnswer(ID, ID, createCheckAnswerRequest(answer = -1))
                    .getResult()

                Then("오답으로 처리되어 정답률이 변경된다.") {
                    result.expectSubscription()
                        .assertNext { verify { quizRepository.save(any()) } }
                        .verifyComplete()
                }
            }
        }

        Given("유저가 퀴즈를 작성하는 중인 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.save(any()) } returns it
                }
            val chapter = createChapter()
                .also {
                    every { chapterRepository.findById(any<String>()) } returns it
                }
            val course = createCourse()
                .also {
                    every { courseRepository.findById(any<String>()) } returns it
                }
            val quizResponse = QuizResponse(quiz)

            When("유저가 퀴즈를 제출하면") {
                val result = quizService.createQuiz(ID, createCreateQuizRequest())
                    .getResult()

                Then("퀴즈가 생성된다.") {
                    result.expectSubscription()
                        .expectNext(quizResponse)
                        .verifyComplete()
                }
            }
        }

        Given("유저가 퀴즈를 저장하는 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findById(any<String>()) } returns it
                    every { quizRepository.save(any()) } returns it
                }
            createUser()
                .also {
                    every { userRepository.findById(any<String>()) } returns createUser()
                    every { userRepository.save(any()) } returns createUser()
                }

            When("유저가 해당 퀴즈를 처음 저장한다면") {
                val result = quizService.markQuiz(ID, ID)
                    .getResult()

                Then("퀴즈가 저장된다.") {
                    result.expectSubscription()
                        .assertNext {
                            quiz.markedUserIds.size shouldBeGreaterThan createQuiz().markedUserIds.size
                            verify { quizRepository.save(any()) }
                        }
                        .verifyComplete()
                }
            }

            When("이미 유저가 해당 퀴즈를 저장한 상태라면") {
                val result = quizService.markQuiz(ID, quiz.markedUserIds.random())
                    .getResult()

                Then("퀴즈가 저장 취소된다.") {
                    result.expectSubscription()
                        .assertNext {
                            quiz.markedUserIds.size shouldBeLessThan createQuiz().markedUserIds.size
                            verify { quizRepository.save(any()) }
                        }
                        .verifyComplete()
                }
            }
        }

        Given("유저가 퀴즈를 좋아요로 평가하는 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findById(any<String>()) } returns it
                    every { quizRepository.save(any()) } returns it
                }

            When("유저가 해당 퀴즈를 처음 좋아요로 평가한다면") {
                val result = quizService.evaluateQuiz(ID, ID, true)
                    .getResult()

                Then("퀴즈가 평가된다.") {
                    result.expectSubscription()
                        .assertNext {
                            quiz.likedUserIds.size shouldBeGreaterThan createQuiz().likedUserIds.size
                            verify { quizRepository.save(any()) }
                        }
                        .verifyComplete()
                }
            }

            When("유저가 이미 해당 퀴즈를 좋아요로 평가한 상태라면") {
                val result = quizService.evaluateQuiz(ID, quiz.likedUserIds.random(), true)
                    .getResult()

                Then("퀴즈 평가가 취소된다.") {
                    result.expectSubscription()
                        .assertNext {
                            quiz.likedUserIds.size shouldBeLessThan createQuiz().likedUserIds.size
                            verify { quizRepository.save(any()) }
                        }
                        .verifyComplete()
                }
            }
        }

        Given("유저가 퀴즈를 싫어요로 평가하는 경우") {
            val quiz = createQuiz()
                .also {
                    every { quizRepository.findById(any<String>()) } returns it
                    every { quizRepository.save(any()) } returns it
                }

            When("유저가 해당 퀴즈를 처음 싫어요로 평가한다면") {
                val result = quizService.evaluateQuiz(ID, ID, false)
                    .getResult()

                Then("퀴즈가 평가된다.") {
                    result.expectSubscription()
                        .assertNext {
                            quiz.unlikedUserIds.size shouldBeGreaterThan createQuiz().unlikedUserIds.size
                            verify { quizRepository.save(any()) }
                        }
                        .verifyComplete()
                }
            }

            When("유저가 이미 해당 퀴즈를 좋아요로 평가한 상태라면") {
                val result = quizService.evaluateQuiz(ID, quiz.unlikedUserIds.random(), false)
                    .getResult()

                Then("퀴즈 평가가 취소된다.") {
                    result.expectSubscription()
                        .assertNext {
                            quiz.unlikedUserIds.size shouldBeLessThan createQuiz().unlikedUserIds.size
                            verify { quizRepository.save(any()) }
                        }
                        .verifyComplete()
                }
            }
        }
    }
}