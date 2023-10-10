package com.github.starnowski.concurrency.fun.java21

import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant

class TemporaryValueStoreTest extends Specification {

    def "should return temporary value when there is first invocation" () {
        given:
            def clock = Mock(Clock)
            def instant = Instant.now()
            clock.instant() >> instant
            def expectedValue = new TemporaryValueStore.TemporaryValue("XXX", Duration.ofSeconds(300))
            java.util.function.Supplier< TemporaryValueStore.TemporaryValue> valueSupplier = () -> {
                expectedValue
            }
            def tested = new TemporaryValueStore(valueSupplier, clock)

        when:
            def result = tested.get()

        then:
            result == expectedValue
    }
}
