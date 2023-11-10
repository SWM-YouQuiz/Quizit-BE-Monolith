package com.quizit.backend.domain.curriculum.repository

import com.quizit.backend.domain.curriculum.model.Curriculum
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CurriculumRepository : ReactiveMongoRepository<Curriculum, String>