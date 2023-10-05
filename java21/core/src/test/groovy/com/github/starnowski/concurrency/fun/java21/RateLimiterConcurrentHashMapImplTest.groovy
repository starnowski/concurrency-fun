package com.github.starnowski.concurrency.fun.java21

import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock
import java.time.Duration
import java.time.Instant

class RateLimiterConcurrentHashMapImplTest extends Specification {

    def "should accept request when no pair exists" () {
        given:
            def clock = Mock(Clock)
            def instant = Instant.now()
            clock.instant() >> instant
            def tested = new RateLimiterConcurrentHashMapImpl(clock)

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
            def tested = new RateLimiterConcurrentHashMapImpl(clock, 2, Duration.ofSeconds(1000))
            tested.canAccept("x1", "0.0.0.0")
            tested.canAccept("x1", "0.0.0.0")

        when:
            def result = tested.canAccept("x1", "0.0.0.0")

        then:
            !result
    }

    @Unroll
    def "should accept request when reaching limit for one user (#multiRequestUserAgent) but for other user limit is not reached yet (#singleRequestUserAgent)" () {
        given:
            def clock = Mock(Clock)
            def instant = Instant.now()
            clock.instant() >> instant
            def tested = new RateLimiterConcurrentHashMapImpl(clock, 2, Duration.ofSeconds(1000))
            tested.canAccept(multiRequestUserAgent, "0.0.0.0")
            tested.canAccept(multiRequestUserAgent, "0.0.0.0")

        when:
            def result = tested.canAccept(singleRequestUserAgent, "0.0.0.0")

        then:
            result

        where:
            multiRequestUserAgent       | singleRequestUserAgent
            "da"                        |   "y1"
    }

    def "should accept request when there are no request for current slice period" () {
        given:
            def clock = Mock(Clock)
            def instant = Instant.now()
            def futureInstant = instant.plus(Duration.ofSeconds(1001))
            clock.instant() >>> [instant, instant, futureInstant ]
            def tested = new RateLimiterConcurrentHashMapImpl(clock, 2, Duration.ofSeconds(1000))
            tested.canAccept("x1", "0.0.0.0")
            tested.canAccept("x1", "0.0.0.0")

        when:
            def result = tested.canAccept("x1", "0.0.0.0")

        then:
            result
    }

    def "should clean old request instants when work unit contains only old instants" () {
        given:
            def clock = Mock(Clock)
            def instant = Instant.now()
            def oldInstant = instant.minus(Duration.ofSeconds(1001))
            clock.instant() >>> [instant]
            def tested = new RateLimiterConcurrentHashMapImpl(clock, 2, Duration.ofSeconds(1000))
            RateLimiterConcurrentHashMapImpl.WorkUnit workUnit = new RateLimiterConcurrentHashMapImpl.WorkUnit()
            workUnit.getRequestInstants().add(oldInstant)
            tested.getMap().put(RateLimiterImpl.prepareKey("x1", "0.0.0.0"), workUnit)

        when:
            tested.cleanOldWorkUnits()

        then:
            tested.getMap().isEmpty()
    }
}
