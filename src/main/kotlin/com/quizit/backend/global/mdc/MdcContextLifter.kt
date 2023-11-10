package com.quizit.backend.global.mdc

import org.reactivestreams.Subscription
import org.slf4j.MDC
import reactor.core.CoreSubscriber
import reactor.util.context.Context
import java.util.stream.Collectors

class MdcContextLifter<T>(private val coreSubscriber: CoreSubscriber<T>) : CoreSubscriber<T> {
    override fun onSubscribe(subscription: Subscription) {
        coreSubscriber.onSubscribe(subscription)
    }

    override fun onNext(t: T) {
        copyToMdc(coreSubscriber.currentContext())
        coreSubscriber.onNext(t)
    }

    override fun onError(throwable: Throwable) {
        coreSubscriber.onError(throwable)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun currentContext(): Context = coreSubscriber.currentContext()

    private fun copyToMdc(context: Context) {
        MDC.setContextMap(
            context.stream()
                .collect(
                    Collectors.toMap(
                        { it.key.toString() },
                        { it.value.toString() })
                )
        )
    }
}