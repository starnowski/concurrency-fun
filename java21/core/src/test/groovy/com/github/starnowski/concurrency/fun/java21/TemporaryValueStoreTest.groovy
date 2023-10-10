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

    def "should return temporary value when there is two invocations" () {
        given:
            def clock = Mock(Clock)
            def instant = Instant.now()
            clock.instant() >> instant
            def expectedValue = new TemporaryValueStore.TemporaryValue("XXX", Duration.ofSeconds(300))
            java.util.function.Supplier< TemporaryValueStore.TemporaryValue> valueSupplier = Mock(java.util.function.Supplier)
            def tested = new TemporaryValueStore(valueSupplier, clock)
            tested.get()

        when:
            def result = tested.get()

        then:
            result == expectedValue

        and: "should invoke internal supplier only once"
            1 * valueSupplier >> expectedValue
    }
}
