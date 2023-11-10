package com.quizit.backend.fixture

import com.quizit.backend.domain.course.dto.request.CreateCourseRequest
import com.quizit.backend.domain.course.dto.request.UpdateCourseByIdRequest
import com.quizit.backend.domain.course.dto.response.CourseResponse
import com.quizit.backend.domain.course.model.Course

const val TITLE = "title"

fun createCreateCourseRequest(
    title: String = TITLE,
    image: String = IMAGE,
    curriculumId: String = ID
): CreateCourseRequest =
    CreateCourseRequest(
        title = title,
        image = image,
        curriculumId = curriculumId
    )

fun createUpdateCourseByIdRequest(
    title: String = TITLE,
    image: String = IMAGE,
): UpdateCourseByIdRequest =
    UpdateCourseByIdRequest(
        title = title,
        image = image,
    )

fun createCourseResponse(
    id: String = ID,
    title: String = TITLE,
    image: String = IMAGE,
    curriculumId: String = ID
): CourseResponse =
    CourseResponse(
        id = id,
        title = title,
        image = image,
        curriculumId = curriculumId
    )

fun createCourse(
    id: String = ID,
    title: String = TITLE,
    image: String = IMAGE,
    curriculumId: String = ID
): Course =
    Course(
        id = id,
        title = title,
        image = image,
        curriculumId = curriculumId
    )