package com.github.starnowski.concurrency.fun.java21

import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant

class RateLimiterImplTest extends Specification {

    def "should accept request when no pair exists" () {
        given:
        def clock = Mock(Clock)
        def instant = Instant.now()
        clock.instant() >> instant
        def tested = new RateLimiterImpl(clock)

        when:
        def result = tested.canAccept("x1", "0.0.0.0")

        then:
        result
    }

    def "should not accept request when reaching limit" () {
        given:
        def clock = Mock(Clock)
        def instant = Instant.now()
        clock.instant() >> instant
        def tested = new RateLimiterImpl(clock, 2, Duration.ofSeconds(1000))
        tested.canAccept("x1", "0.0.0.0")
        tested.canAccept("x1", "0.0.0.0")

        when:
        def result = tested.canAccept("x1", "0.0.0.0")

        then:
        !result
    }

    def "should accept request when there are no request for current slice period" () {
        given:
        def clock = Mock(Clock)
        def instant = Instant.now()
        def futureInstant = instant.plus(Duration.ofSeconds(1001))
        clock.instant() >>> [instant, instant, futureInstant ]
        def tested = new RateLimiterImpl(clock, 2, Duration.ofSeconds(1000))
        tested.canAccept("x1", "0.0.0.0")
        tested.canAccept("x1", "0.0.0.0")

        when:
        def result = tested.canAccept("x1", "0.0.0.0")

        then:
        result
    }
}
