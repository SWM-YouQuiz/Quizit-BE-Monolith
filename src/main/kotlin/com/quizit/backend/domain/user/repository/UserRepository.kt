package com.quizit.backend.domain.user.repository

import com.quizit.backend.domain.user.model.User
import com.quizit.backend.domain.user.model.enum.Provider
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveMongoRepository<User, String> {
    fun findByEmail(email: String): Mono<User>

    fun findByEmailAndProvider(email: String, provider: Provider): Mono<User>

    @Aggregation(
        pipeline = [
            "{ \$addFields: { quizCount: { \$size: '\$correctQuizIds' } }}",
            "{ \$sort: { quizCount: -1 } }"
        ]
    )
    fun findAllOrderByCorrectQuizIdsSize(): Flux<User>

    @Aggregation(
        pipeline = [
            "{ \$addFields: { quizCount: { \$size: { \$setIntersection: [\$correctQuizIds, ?0] } } } }",
            "{ \$sort: { quizCount: -1 } }"
        ]
    )
    fun findAllOrderByCorrectQuizIdsSizeInQuizIds(quizIds: List<String>): Flux<User>
}